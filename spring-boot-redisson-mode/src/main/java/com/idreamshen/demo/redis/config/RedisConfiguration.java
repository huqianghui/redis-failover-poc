package com.idreamshen.demo.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idreamshen.demo.redis.property.MasterRedisProperty;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.io.IOException;

@Configuration
public class RedisConfiguration {

    @Autowired
    MasterRedisProperty masterRedisProperty;

    @Bean("redisConfig")
    public Config redisConfig()throws  IOException{
        Config config = new Config();
        config.useClusterServers()
                .setScanInterval(2000)
                .setPassword(masterRedisProperty.getPassword())
                .addNodeAddress(masterRedisProperty.getNodes());
        return config;
    }

    @Bean("redissonClient")
    public RedissonClient redissonClient(Config config) throws IOException {
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    @Bean("redissonConnectionFactory")
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    @Bean("masterRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedissonConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        setSerializer(template);//设置序列化工具
        template.afterPropertiesSet();
        return template;
    }

    private void setSerializer(StringRedisTemplate template) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
    }

}
