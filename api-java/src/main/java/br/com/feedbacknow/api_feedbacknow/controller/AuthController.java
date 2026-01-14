package br.com.feedbacknow.api_feedbacknow.controller;

import br.com.feedbacknow.api_feedbacknow.dto.LoginRequest;
import br.com.feedbacknow.api_feedbacknow.repository.UserRepository;
import br.com.feedbacknow.api_feedbacknow.util.JwUtil;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwUtil jwUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwUtil jwUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwUtil = jwUtil;
        this.passwordEncoder = passwordEncoder;
    }
    @Hidden
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        System.out.println("USERNAME RECEBIDO = [" + request.username() + "]");
        System.out.println("PASSWORD RECEBIDO = [" + request.password() + "]");

        // 1️⃣ Busca o usuário no banco
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos")
                );
        // 2️⃣ Verifica a senha
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
        }

        // 3️⃣ Gera JWT com o username ou roles como subject
        String token = jwUtil.gerarToken(user.getUsername());

        // 4️⃣ Retorna o token e tipo
        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "tipo", "Bearer"
                )
        );

    }


}
