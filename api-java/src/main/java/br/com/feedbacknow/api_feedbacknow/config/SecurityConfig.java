package br.com.feedbacknow.api_feedbacknow.config;

import jakarta.servlet.http.HttpServletResponse; // Import necessário para o Response
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Tratador de erros de autenticação
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"erro\": \"Não autorizado\", \"detalhe\": \"Usuário ou senha inválidos ou ausentes.\"}"
            );
        };
    }

    // Security Filter Chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS (mantido como você já tinha)
                .cors(cors -> cors.configure(http))

                // CSRF desabilitado (obrigatório para webhooks)
                .csrf(AbstractHttpConfigurer::disable)

                // Regras de autorização
                .authorizeHttpRequests(req -> req

                        // 🔓 LIBERA WEBHOOKS (ngrok / Facebook / Instagram)
                        .requestMatchers(
                                "/webhook/**",
                                "/health"
                        ).permitAll()

                        // 🔓 LIBERA SWAGGER
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 🔒 TODO O RESTO exige autenticação
                        .anyRequest().authenticated()
                )

                // Autenticação básica com handler customizado
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint()));

        return http.build();
    }
}