package com.zerobase.cms.order.config;

import com.zerobase.domain.config.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        // 다른 모듈에 존재하므로 자동으로 빈 생성이 안됨.
        return new JwtAuthenticationProvider();
    }
}
