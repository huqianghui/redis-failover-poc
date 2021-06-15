package com.idreamshen.demo.redis.config;

import com.idreamshen.demo.redis.property.MasterRedisProperty;
import com.idreamshen.demo.redis.property.BackupRedisProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Autowired
    private MasterRedisProperty masterRedisProperty;

    @Autowired
    private BackupRedisProperty backupRedisProperty;

    @Bean(name = "masterRedisConnectionFactory")
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory();
        redisConnectionFactory.setHostName(masterRedisProperty.getHost());
        redisConnectionFactory.setPort(masterRedisProperty.getPort());
        redisConnectionFactory.setPassword(masterRedisProperty.getPassword());
        return redisConnectionFactory;
    }


    @Primary
    @Bean(name = "redisTemplate")
    public RedisTemplate userRedisTemplate(@Qualifier("masterRedisConnectionFactory") RedisConnectionFactory cf) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(cf);
        setSerializer(stringRedisTemplate);
        return stringRedisTemplate;
    }

    private void setSerializer(RedisTemplate<String, String> template) {
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    }

}
