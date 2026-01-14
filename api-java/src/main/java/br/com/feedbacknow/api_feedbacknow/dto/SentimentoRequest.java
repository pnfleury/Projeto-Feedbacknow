package br.com.feedbacknow.api_feedbacknow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SentimentoRequest(
        @NotBlank(message = "A reclamação, sugestão ou elogios é obrigatória")
        @Size(min = 4, max = 500, message = "A informação deve ter entre 4 e 500 caracteres")
        String comentario
) {}