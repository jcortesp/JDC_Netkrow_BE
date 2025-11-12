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
                        .allowedOriginPatterns(
                                frontendUrl,
                                frontendProdUrl,
                                frontendExtraUrl,
                                "https://*.vercel.app"
                        )
                        .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                        // ✅ añadimos el header de idempotencia y algunos comunes
                        .allowedHeaders(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Idempotency-Key",
                                "idempotency-key",
                                "X-Requested-With"
                        )
                        // (opcional) para leerlos desde el FE si alguna vez los necesitas
                        .exposedHeaders("Authorization", "Location", "Idempotency-Key")
                        .allowCredentials(true);
            }
        };
    }
}
