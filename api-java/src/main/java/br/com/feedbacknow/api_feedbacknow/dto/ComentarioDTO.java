package br.com.feedbacknow.api_feedbacknow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDTO {

    private String comentario;
    private String sentimento; // positivo, negativo
    private double probabilidade;
    private List<String> topFeatures;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime timestamp;

}
