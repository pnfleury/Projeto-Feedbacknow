package br.com.feedbacknow.api_feedbacknow.service;

import br.com.feedbacknow.api_feedbacknow.dto.SentimentoRequest;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SentimentoAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(SentimentoAnalyzer.class);
    private final RestTemplate restTemplate;

    @Value("${api.flask.url}")
    private String pythonServiceUrl;

    public SentimentoAnalyzer(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SentimentoResponse analyzeComment(String comentario) {
        SentimentoRequest requestBody = new SentimentoRequest(comentario);

        try {
            // Simplificado: postForObject já entende que deve enviar como JSON
            // se o RestTemplate estiver configurado corretamente.
            return restTemplate.postForObject(pythonServiceUrl, requestBody, SentimentoResponse.class);

        } catch (Exception e) {
            // Log profissional com SLF4J
            logger.error("Falha na comunicação com o serviço de IA em {}: {}", pythonServiceUrl, e.getMessage());

            // Lançamos a exceção para que o CustomExceptionHandler assuma o controle
            throw e;
        }
    }
}

