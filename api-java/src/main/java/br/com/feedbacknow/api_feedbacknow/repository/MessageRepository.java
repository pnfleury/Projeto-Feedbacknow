package br.com.feedbacknow.api_feedbacknow.repository;

import br.com.feedbacknow.api_feedbacknow.domain.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, String> {
}
