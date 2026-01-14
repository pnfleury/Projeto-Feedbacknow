package br.com.feedbacknow.api_feedbacknow.dto;

import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;

public record SentimentUpdateRequest(

        String comentario,
        SentimentType sentimento,
        Boolean visible

) { }
