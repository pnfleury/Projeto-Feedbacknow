package br.com.feedbacknow.api_feedbacknow.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @Id
    private String commentId;
    private String userId;
    private String username;
    private String text;
    private String sentiment;
    private double score;

}
