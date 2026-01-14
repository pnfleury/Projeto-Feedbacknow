package br.com.feedbacknow.api_feedbacknow.security;

import br.com.feedbacknow.api_feedbacknow.util.JwUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwUtil jwUtil;

    public JwtAuthenticationFilter(JwUtil jwUtil) {
        this.jwUtil = jwUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // 1. Ignora explicitamente rotas que não devem ter segurança JWT
        if (path.startsWith("/webhook/alerts") || path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // 2. Se não houver token, APENAS segue para o próximo filtro.
        // O SecurityConfig decidirá se a rota é pública ou se barra o acesso.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extrai e valida o token
            String token = authHeader.substring(7);

            if (jwUtil.valido(token)) {
                String username = jwUtil.getSubject(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.emptyList()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Em caso de token malformado ou erro, não quebramos a requisição aqui,
            // apenas deixamos o SecurityContext vazio para o SecurityConfig barrar se necessário.
            logger.error("Erro ao validar token JWT: " + e.getMessage());
        }

        // 4. CONTINUA A CADEIA DE FILTROS (dentro do método!)
        filterChain.doFilter(request, response);
    }
}