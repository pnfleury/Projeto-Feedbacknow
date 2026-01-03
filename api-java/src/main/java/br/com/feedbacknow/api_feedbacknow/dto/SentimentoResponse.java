package br.com.feedbacknow.api_feedbacknow.dto;

import java.time.LocalDateTime;
import java.util.List;


public record SentimentoResponse(
        Long id,
        String comentario,
        String sentimento,
        Double probabilidade,
        List<String> topFeatures,
        LocalDateTime criadoEm
) {

}