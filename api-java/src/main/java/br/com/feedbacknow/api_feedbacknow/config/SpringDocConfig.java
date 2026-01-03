package br.com.feedbacknow.api_feedbacknow.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FeedbackNow API")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipe FeedbackNow")
                                .email("email@gmail.com")))
                // Adiciona a definição de segurança
                .components(new Components()
                        .addSecuritySchemes("basicScheme",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                // Aplica a segurança globalmente no Swagger
                .addSecurityItem(new SecurityRequirement().addList("basicScheme"));
    }
}