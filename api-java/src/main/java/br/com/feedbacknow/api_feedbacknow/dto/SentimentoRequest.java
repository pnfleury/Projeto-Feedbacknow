package br.com.feedbacknow.api_feedbacknow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SentimentoRequest(
        @NotBlank(message = "A reclamação, sugestão ou elogios é obrigatória")
        @Size(min = 5, max = 500, message = "A informação deve ter entre 5 e 500 caracteres")
        String comentario,
        @Schema(defaultValue = "0.5") Double threshold) {

        //Construtor compacto para definir um padrão caso o threshold venha nulo
        public SentimentoRequest {
                if (threshold == null) threshold = 0.5;
        }
}