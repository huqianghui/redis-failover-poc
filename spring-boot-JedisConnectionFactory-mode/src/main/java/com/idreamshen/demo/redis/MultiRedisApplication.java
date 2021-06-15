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
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class})
public class MultiRedisApplication implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(MultiRedisApplication.class);

    private final RedisTemplate masterRedisTemplate;

    @Autowired
    public MultiRedisApplication(@Qualifier("redisTemplate") RedisTemplate masterRedisTemplate,
                                 UserService userService) {
        this.masterRedisTemplate = masterRedisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(MultiRedisApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {

        masterRedisTemplate.opsForValue().set("hello", "world_1");

        String primaryKeyValue = masterRedisTemplate.opsForValue().get("hello").toString();
        String secondaryKeyValue = masterRedisTemplate.opsForValue().get("hello").toString();

        logger.info("==================================================================");
        logger.info(String.format("read the primary redis, key is `hello`, value is %s", primaryKeyValue));
        logger.info(String.format("read the secondary redis, key is `hello`, value is %s", secondaryKeyValue));
        logger.info("==================================================================");

        // you can also check the value with redis-cli
    }

}
