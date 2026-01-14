package br.com.feedbacknow.api_feedbacknow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera todos os endpoints da API
                .allowedOrigins(
                        "http://localhost:5173", // React
                        "http://127.0.0.1:5175" // Streamlit IP
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*") // Permite todos os headers (Authorization, Content-Type, etc)
                .allowCredentials(true) // Necessário se você enviar Cookies ou Auth Basic
                .maxAge(3600); // Cache da configuração CORS por 1 hora
    }
}

