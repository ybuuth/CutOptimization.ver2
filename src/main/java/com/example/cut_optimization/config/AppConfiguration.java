package com.example.cut_optimization.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class AppConfiguration {

    @Bean
    public Random random() {
        return new Random();
    }

}
