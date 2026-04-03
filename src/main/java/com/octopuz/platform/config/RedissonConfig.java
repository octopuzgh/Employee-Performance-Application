package com.octopuz.platform.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    private String redisHost = "192.168.152.131";
    private int redisPort = 6379;
    private String redisUsername = "octopuz_remote";
    private String redisPassword = "Octopuz@123";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setUsername(redisUsername)
                .setPassword(redisPassword)
                .setDatabase(0);
        return Redisson.create(config);
    }
}

