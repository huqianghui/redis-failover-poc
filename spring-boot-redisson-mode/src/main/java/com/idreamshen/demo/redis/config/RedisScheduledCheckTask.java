package com.idreamshen.demo.redis.config;


import com.idreamshen.demo.redis.property.BackupRedisProperty;
import com.idreamshen.demo.redis.property.MasterRedisProperty;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnection;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Properties;

@Component
public class RedisScheduledCheckTask {

    private final static Logger logger = LoggerFactory.getLogger(RedisScheduledCheckTask.class);


    @Autowired
    @Qualifier("masterRedisTemplate")
    private RedisTemplate masterRedisTemplate;

    public Config getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(Config redisConfig) {
        this.redisConfig = redisConfig;
    }

    @Autowired
    @Qualifier("redisConfig")
    private Config redisConfig;

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Autowired
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;


    @Autowired
    @Qualifier("redissonConnectionFactory")
    private RedissonConnectionFactory redissonConnectionFactory;

    @Autowired
    private MasterRedisProperty masterRedisProperty;

    @Autowired
    private BackupRedisProperty backupRedisProperty;

    private int tryNum =3;


    @Scheduled(fixedRate = 3000)
    public void scheduledTask() throws Exception {
        System.out.println("任务执行时间：" + LocalDateTime.now());

        RedissonConnectionFactory currentRedisConnectionFactory = (RedissonConnectionFactory)masterRedisTemplate.getConnectionFactory();
        RedisConnection connection = RedisConnectionUtils.getConnection(currentRedisConnectionFactory);

        int loopCount;
        for( loopCount=0 ;loopCount < tryNum; loopCount++){
            try {
                if (connection instanceof RedissonConnection) {
                    ClusterInfo clusterInfo = currentRedisConnectionFactory.getClusterConnection().clusterGetClusterInfo();
                    logger.info("****cluster_size " + clusterInfo.getClusterSize());
                } else {
                    Properties info = connection.info();
                    logger.info("****version " + info.getProperty("redis_version"));
                }
                // if the status is ok, then break,wait next time.
                break;
            }catch (Exception e)
            {
                // if the status is NG, then try again.
                logger.error("redis connection exception:" + e.getMessage());
            }
            finally {
                RedisConnectionUtils.releaseConnection(connection, currentRedisConnectionFactory);
            }
        }

        System.out.println("loopCount:" + loopCount);

        // the final status is NG, switch connection string
        if(loopCount == tryNum){
            logger.info("****switch connection...");
            redissonClient.shutdown();
            currentRedisConnectionFactory.destroy();
            Config changedConfig = new Config();
            if(redisConfig.useClusterServers().getNodeAddresses().get(0).toString().equals(masterRedisProperty.getNodes())){
                logger.info("****switch backup connection...");
                changedConfig.useClusterServers()
                        .setPassword(backupRedisProperty.getPassword())
                        .addNodeAddress(backupRedisProperty.getNodes());
            }else{
                logger.info("****switch master connection...");
                changedConfig.useClusterServers()
                        .setPassword(masterRedisProperty.getPassword())
                        .addNodeAddress(masterRedisProperty.getNodes());
            }
            ApplicationContextUtil.replaceBean("redisConfig",changedConfig);
            RedissonClient changedRedissonClient = Redisson.create(changedConfig);
            ApplicationContextUtil.replaceBean("redissonClient", changedRedissonClient);
            setRedisConfig(changedConfig);
            RedissonConnectionFactory connectionFactory = new RedissonConnectionFactory(changedRedissonClient);
            connectionFactory.afterPropertiesSet();
            masterRedisTemplate.setConnectionFactory(connectionFactory);
            masterRedisTemplate.afterPropertiesSet();
        }

        System.out.println("try operation: " + masterRedisTemplate.opsForValue().get("hello").toString());

    }
}
