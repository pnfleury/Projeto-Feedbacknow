package br.com.feedbacknow.api_feedbacknow.dto;

public record SentimentUpdateDTO(
        String comentario,
        Boolean visible
) {
}
