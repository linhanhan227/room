package com.chat.room.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        AppProperties.Cors cors = appProperties.getCors();
        registry.addMapping("/**")
                .allowedOriginPatterns(cors.getAllowedOriginPatterns())
                .allowedMethods(cors.getAllowedMethods())
                .allowedHeaders(cors.getAllowedHeaders())
                .allowCredentials(cors.isAllowCredentials())
                .maxAge(cors.getMaxAge());
    }
}
