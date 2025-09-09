package com.netkrow.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${frontend.prod-url:https://netkrow-fe.vercel.app}")
    private String frontendProdUrl;

    @Value("${frontend.extra-url:https://netkrow.onrender.com}")
    private String frontendExtraUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Usamos patterns para permitir comodines (*.vercel.app)
                        .allowedOriginPatterns(
                                frontendUrl,
                                frontendProdUrl,
                                frontendExtraUrl,
                                "https://*.vercel.app"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }
}
