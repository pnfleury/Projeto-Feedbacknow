package br.com.feedbacknow.api_feedbacknow.repository;

import br.com.feedbacknow.api_feedbacknow.domain.SentimentEntity;
import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SentimentRepository extends JpaRepository<SentimentEntity, Long> {

    // Contagem do banco todo
    long countBySentimento(SentimentType sentimento);

    // Contagem dos registros por dias separados por sentimento ou não
    long countBySentimentoAndCriadoEmAfter(SentimentType sentimento, LocalDateTime data);
    long countByCriadoEmAfter(LocalDateTime data);

    // Busca todos os registros que possuem o sentimento x
    Page<SentimentEntity> findBySentimento(SentimentType sentimento, Pageable pageable);

    Page<SentimentEntity> findByVisibleTrueAndDeletedFalse(Pageable pageable);

    List<SentimentEntity> findByVisibleTrueAndDeletedFalse();

    Optional<SentimentEntity> findByIdAndDeletedFalse(Long id);

    Page<SentimentEntity> findBySentimentoAndVisibleTrueAndDeletedFalse(
            SentimentType sentimento,
            Pageable pageable
    );


    List<SentimentEntity> findBySentimentoAndCriadoEmAfter(SentimentType sentimento, LocalDateTime data);
}