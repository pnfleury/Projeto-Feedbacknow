package br.com.feedbacknow.api_feedbacknow.config;
import br.com.feedbacknow.api_feedbacknow.domain.User;
import br.com.feedbacknow.api_feedbacknow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verifica se o admin já existe para não dar erro de duplicata no Postgres
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");

                // O encoder transforma "admin123" no hash que o Spring Security exige
                admin.setPassword(passwordEncoder.encode("123456"));

                userRepository.save(admin);

                System.out.println("\n================================================");
                System.out.println("✅ BANCO POSTGRES PRONTO: Usuário 'admin' criado!");
                System.out.println("================================================\n");
            }
        };
    }
}