package com.quiz.generator.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Permite CORS para todos os endpoints
                .allowedOrigins("http://localhost:5500",
                        "http://localhost:63342",
                        "https://*.netlify.app",
                        "http://localhost:8080", // Para testes locais do frontend (se você abre diretamente o HTML)
                        "http://localhost:3000") // Ajuste conforme seu frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}