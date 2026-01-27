package com.example.kirana.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Redisson config.
 */
@Configuration
public class RedissonConfig {

    /**
     * Redisson client redisson client.
     *
     * @return the redisson client
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {

        Config config = new Config();


        config.useSingleServer()
                .setAddress("redis://localhost:6379");

        return Redisson.create(config);
    }
}

