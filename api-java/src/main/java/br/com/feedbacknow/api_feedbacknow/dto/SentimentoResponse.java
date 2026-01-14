package br.com.feedbacknow.api_feedbacknow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;


public record SentimentoResponse(
        Long id,
        String comentario,
        String sentimento,
        Double probabilidade,
        List<String> topFeatures,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime criadoEm,

        String origem
) {

}