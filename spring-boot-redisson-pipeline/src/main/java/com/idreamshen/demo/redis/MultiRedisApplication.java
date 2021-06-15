package com.idreamshen.demo.redis;

import com.idreamshen.demo.redis.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class})
@RestController
public class MultiRedisApplication{

    private final static Logger logger = LoggerFactory.getLogger(MultiRedisApplication.class);

    private  RedisTemplate masterRedisTemplate;

    @Autowired
    public MultiRedisApplication(@Qualifier("masterRedisTemplate") RedisTemplate masterRedisTemplate,
                                 UserService userService) {
        this.masterRedisTemplate = masterRedisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(MultiRedisApplication.class, args);
    }

    @GetMapping("/redis")
    public String run(String... strings) throws Exception {

        masterRedisTemplate.opsForValue().set("hello", "world_1");

        String primaryKeyValue = masterRedisTemplate.opsForValue().get("hello").toString();
        String secondaryKeyValue = masterRedisTemplate.opsForValue().get("hello").toString();

        logger.info("==================================================================");
        logger.info(String.format("read the primary redis, key is `hello`, value is %s", primaryKeyValue));
        logger.info(String.format("read the secondary redis, key is `hello`, value is %s", secondaryKeyValue));
        logger.info("==================================================================");

        // you can also check the value with redis-cli
        // you can also check the value with redis-cli

        SessionCallback<List<Object>> sessionCallback1 = new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations s) throws DataAccessException {
               // s.multi();
                // 存放 a token key，r token 并设置24小时过期
                s.opsForValue().set("hello1", "world1111");
                s.expire("hello1", 24L, TimeUnit.HOURS);
                //存放 r token，a token  并设置24小时过期时间
                s.opsForValue().set("hello2", "world2222");
                s.expire("hello2", 24L, TimeUnit.HOURS);
                //存放 14天过期时间，FOURTEEN_TIME 设置过期时间为14天
                s.opsForValue().get("hello1");
                s.opsForValue().get("hello2");
               //return   s.exec();
                return null;
            }
        };


        // masterRedisTemplate.execute(sessionCallback1);
        return "pipeline result: " +  masterRedisTemplate.executePipelined(sessionCallback1);
    }



}
