package br.com.feedbacknow.api_feedbacknow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageAlertDTO {

    private String username;    // usuário que enviou
    private String text;        // texto da mensagem/comentário
    private String sentiment;   // positivo / negativo / neutro
    private double score;

}


