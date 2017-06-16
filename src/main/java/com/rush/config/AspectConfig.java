package com.rush.config;

import com.rush.service.LoggingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created by aomine on 6/16/17.
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {


    @Bean
    public LoggingService loggingService() {
        return new LoggingService();
    }
}
