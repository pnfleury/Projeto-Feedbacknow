package br.com.feedbacknow.api_feedbacknow.controller;

import br.com.feedbacknow.api_feedbacknow.domain.SentimentEntity;
import br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository;
import br.com.feedbacknow.api_feedbacknow.service.AlertService;
import br.com.feedbacknow.api_feedbacknow.service.SentimentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/webhook")
@CrossOrigin(origins = "*")
public class FacebookWebhookController {

    private final SentimentService sentimentService;
    private final AlertService alertService;
    private final SentimentRepository sentimentRepository;


    @Value("${instagram.webhook.verify-token}")
    private String verifyToken;

    public FacebookWebhookController(SentimentService sentimentService,
                                     AlertService alertService,
                                     SentimentRepository sentimentRepository) {
        this.sentimentService = sentimentService;
        this.alertService = alertService;
        this.sentimentRepository = sentimentRepository;
    }

    /**
     * Validação do Webhook (GET)
     * Responde ao desafio da Meta para validar a URL.
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {
        log.info("🔍 Validação solicitada. Token recebido: {}", token);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .header("ngrok-skip-browser-warning", "true")
                    .body(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Recebimento de Eventos (POST)
     * Processa comentários do Instagram e Facebook.
     */
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("📩 Novo evento recebido.");
        log.info("📦 CONTEÚDO BRUTO: [{}]", payload);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);

            String objectType = root.path("object").asText();
            log.info("ℹ️ Recebido objeto do tipo: {}", objectType);

            JsonNode entries = root.path("entry");
            for (JsonNode entry : entries) {
                JsonNode changes = entry.path("changes");

                if (changes.isMissingNode() || !changes.isArray()) {
                    log.info("pula o resto: Evento de notificação de alteração recebido.");
                    continue;
                }

                for (JsonNode change : changes) {
                    JsonNode value = change.path("value");
                    String texto = value.has("text") ? value.get("text").asText() : value.path("message").asText("");

                    if (texto.isEmpty()) {
                        log.info("pula o resto: Campo de texto está vazio.");
                        continue;
                    }

                    log.info("✅ Comentário real identificado: {}", texto);

                    // Extração de dados para o service
                    JsonNode from = value.path("from");
                    String userId = from.path("id").asText("N/A");
                    String username = from.has("username") ? from.get("username").asText() : from.path("name").asText("N/A");
                    String commentId = value.path("id").asText("N/A");

                    // Chame seus services aqui
                    sentimentService.analisarSentimento(texto, commentId, objectType.toUpperCase(), userId, username);
                }
            }
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("❌ Erro: {}", e.getMessage());
            return ResponseEntity.ok("RECEIVED_WITH_LOG_ERROR");
        }
    } // FECHA O RECEIVEWEBHOOK AQUI

    // AGORA SIM, O MÉTODO DE LISTAGEM FORA DO POST
    @GetMapping("/dados")
    public ResponseEntity<List<SentimentEntity>> visualizarBanco() {
        log.info("📊 Consultando novos dados da Meta...");

        // Busca todos e você pode usar o Postman com ?sort=id,desc
        List<SentimentEntity> sentimentos = sentimentRepository.findAll();
        return ResponseEntity.ok(sentimentos);
    }

}
