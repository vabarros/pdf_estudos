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
                        "https://quiz-backend-api-a23j.onrender.com", // Adicionar o próprio domínio do backend (às vezes necessário para alguns cenários)
                        "https://quiz-frontend-app.onrender.com" ) // Ajuste conforme seu frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}