package br.com.feedbacknow.api_feedbacknow.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "feedbacks")
public class SentimentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comentario", columnDefinition = "TEXT", nullable = false)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SentimentType sentimento;

    @Column(nullable = false)
    private Double probabilidade;

    @Column(nullable = true)
    private String topFeatures;

    // ADICIONE ESTA LINHA AQUI:
    @Column(name = "origem")
    private String origem;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    @UpdateTimestamp
    @Column(name = "updated_at") //Hibernate cuidar disso automaticamente.
    private LocalDateTime updatedAt;
}
