package br.com.feedbacknow.api_feedbacknow.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comentario")
@Getter
@Setter
public class ComentarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private String postId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "texto", columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "sentimento", nullable = false)
    private String sentimento;

    @Column(name = "probabilidade")
    private double probabilidade;

    @ElementCollection
    @CollectionTable(name = "comentario_top_features", joinColumns = @JoinColumn(name = "comentario_id"))
    @Column(name = "top_feature")
    private List<String> topFeatures;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public ComentarioEntity() {}

    public ComentarioEntity(String postId, String userId, String userName, String texto,
                            String sentimento, double probabilidade, List<String> topFeatures) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.texto = texto;
        this.sentimento = sentimento;
        this.probabilidade = probabilidade;
        this.topFeatures = topFeatures;
        this.criadoEm = LocalDateTime.now();
    }

}
