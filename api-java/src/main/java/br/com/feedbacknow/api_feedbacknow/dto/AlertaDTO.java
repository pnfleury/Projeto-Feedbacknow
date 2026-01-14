package br.com.feedbacknow.api_feedbacknow.dto;

import java.time.LocalDateTime;

public record AlertaDTO(
        String comentario,
        String sentimento,
        String origem,
        LocalDateTime criadoEm
) {
}
