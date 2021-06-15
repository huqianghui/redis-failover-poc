package com.idreamshen.demo.redis.config;


import com.idreamshen.demo.redis.property.BackupRedisProperty;
import com.idreamshen.demo.redis.property.MasterRedisProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.LocalDateTime;
import java.util.Properties;

@Component
public class RedisScheduledCheckTask {

    private final static Logger logger = LoggerFactory.getLogger(RedisScheduledCheckTask.class);


    @Autowired
    private RedisTemplate masterRedisTemplate;


    @Autowired
    @Qualifier("masterRedisConnectionFactory")
    private RedisConnectionFactory masterRedisConnectionFactory;

    @Autowired
    private MasterRedisProperty masterRedisProperty;

    @Autowired
    private BackupRedisProperty backupRedisProperty;

    private int tryNum =3;


    @Scheduled(fixedRate = 3000)
    public void scheduledTask() {
        System.out.println("任务执行时间：" + LocalDateTime.now());

        JedisConnectionFactory currentRedisConnectionFactory = (JedisConnectionFactory)masterRedisTemplate.getConnectionFactory();
        RedisConnection connection = RedisConnectionUtils.getConnection(currentRedisConnectionFactory);

        int loopCount;
        for( loopCount=0 ;loopCount < tryNum; loopCount++){
            try {
                if (connection instanceof RedisClusterConnection) {
                    ClusterInfo clusterInfo = ((RedisClusterConnection)connection).clusterGetClusterInfo();
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
                try{
                    RedisConnectionUtils.releaseConnection(connection, currentRedisConnectionFactory);
                }catch (Exception e){
                    logger.error("release Connection failed.");
                }

            }
        }

        // the final status is NG, switch connection string
        if(loopCount == tryNum){
            logger.info("****switch connection...");
            if(currentRedisConnectionFactory.getHostName().equals(masterRedisProperty.getHost())){
                logger.info("****switch backup connection...");
                currentRedisConnectionFactory.setHostName(backupRedisProperty.getHost());
                currentRedisConnectionFactory.setPort(backupRedisProperty.getPort());
                currentRedisConnectionFactory.setPassword(backupRedisProperty.getPassword());
            }else{
                logger.info("****switch master connection...");
                currentRedisConnectionFactory.setHostName(masterRedisProperty.getHost());
                currentRedisConnectionFactory.setPort(masterRedisProperty.getPort());
                currentRedisConnectionFactory.setPassword(masterRedisProperty.getPassword());
            }
            currentRedisConnectionFactory.afterPropertiesSet();

            masterRedisTemplate.setConnectionFactory(currentRedisConnectionFactory);
            masterRedisTemplate.afterPropertiesSet();
        }

    }
}
