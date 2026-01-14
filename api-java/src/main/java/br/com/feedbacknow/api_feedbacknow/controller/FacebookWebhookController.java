package br.com.feedbacknow.api_feedbacknow.controller;

import br.com.feedbacknow.api_feedbacknow.domain.SentimentEntity;
import br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository;
import br.com.feedbacknow.api_feedbacknow.service.AlertService;
import br.com.feedbacknow.api_feedbacknow.service.SentimentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/webhook")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", allowedHeaders = "*")
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
     * Canal de Alertas via SSE
     * MODIFICADO: Adicionado headers manuais para garantir que o navegador não bloqueie a conexão
     */
    @Hidden
    @GetMapping(value = "/alerts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAlerts(HttpServletResponse response) {
        log.info("🔌 Novo cliente se conectando ao canal de alertas...");

        // Headers essenciais para o Firefox e Chrome manterem a conexão viva e sem erro de CORS
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        return alertService.registerEmitter();
    }

    /**
     * Validação do Webhook (GET)
     */
    @Hidden
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
                    .body(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Recebimento de Eventos (POST)
     */
    @Hidden
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("📩 Novo evento recebido.");
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);

            String objectType = root.path("object").asText("");
            String origemFinal = "page".equalsIgnoreCase(objectType) ? "FACEBOOK" :
                    "instagram".equalsIgnoreCase(objectType) ? "INSTAGRAM" : objectType.toUpperCase();

            JsonNode entries = root.path("entry");
            for (JsonNode entry : entries) {
                JsonNode changes = entry.path("changes");
                if (changes.isMissingNode() || !changes.isArray()) continue;

                for (JsonNode change : changes) {
                    JsonNode value = change.path("value");
                    String texto = value.has("text") ? value.get("text").asText() : value.path("message").asText("");

                    if (texto.isEmpty()) continue;

                    log.info("✅ Comentário identificado para {}: {}", origemFinal, texto);

                    JsonNode from = value.path("from");
                    String userId = from.path("id").asText("N/A");
                    String username = from.has("username") ? from.get("username").asText() : from.path("name").asText("N/A");
                    String commentId = value.path("id").asText("N/A");

                    sentimentService.analisarSentimento(texto, commentId, "POST_ID_GENERICO", userId, username, origemFinal);
                }
            }
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("❌ Erro no Webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }
    @Hidden
    @GetMapping("/dados")
    public ResponseEntity<List<SentimentEntity>> visualizarBanco() {
        log.info("📊 Consultando novos dados da Meta...");
        return ResponseEntity.ok(sentimentRepository.findAll());
    }
}
