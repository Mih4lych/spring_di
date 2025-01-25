package com.moh4lych.springdi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(httpSecurityCsrfConfigurer -> {
            httpSecurityCsrfConfigurer.ignoringRequestMatchers("/api/**");
        });
        return httpSecurity.build();
    }
}
