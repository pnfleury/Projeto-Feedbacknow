package br.com.feedbacknow.api_feedbacknow.repository;

import br.com.feedbacknow.api_feedbacknow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
