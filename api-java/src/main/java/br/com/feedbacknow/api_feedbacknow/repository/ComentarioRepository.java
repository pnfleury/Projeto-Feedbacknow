package br.com.feedbacknow.api_feedbacknow.repository;

import br.com.feedbacknow.api_feedbacknow.domain.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<ComentarioEntity, Long> {
}
