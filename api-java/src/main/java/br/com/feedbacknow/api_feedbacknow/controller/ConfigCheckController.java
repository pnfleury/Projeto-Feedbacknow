package br.com.feedbacknow.api_feedbacknow.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigCheckController {

    @Value("${instagram.api.access-token}")
    private String accessToken;

    @Value("${instagram.webhook.verify-token}")
    private String verifyToken;

    @GetMapping("/api/config/check")
    public Map<String, String> checkConfig(){
        return Map.of(
                "accessToken", accessToken != null ? "***" + accessToken.substring(Math.max(0, accessToken.length() - 4)) : "NULL",
                "verifyToken", verifyToken != null ? "***" + verifyToken.substring(Math.max(0, verifyToken.length() - 4)) : "NULL",
                "message", "Configurações carregadas com sucesso!"
        );
    }

}
