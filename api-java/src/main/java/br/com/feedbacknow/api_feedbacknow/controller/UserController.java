package br.com.feedbacknow.api_feedbacknow.controller;
import br.com.feedbacknow.api_feedbacknow.repository.UserRepository;
import br.com.feedbacknow.api_feedbacknow.domain.User;
import br.com.feedbacknow.api_feedbacknow.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/feedbacknow/usuarios")
public class UserController {

    @Autowired // Isso diz ao Spring para "instanciar" o repositório automaticamente
    private UserRepository userRepository;

    @Autowired // Isso diz ao Spring para buscar o codificador de senhas configurado
    private PasswordEncoder passwordEncoder;

    @PostMapping("/cadastrar")
    public ResponseEntity<String> cadastrar(@RequestBody @Valid LoginRequest dados) {

        // 1. Verificamos se o usuário já existe usando o seu novo método do repositório

        if (userRepository.findByUsername(dados.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já existe!");
        }

        // 2. Usamos 'User' (que é o nome da sua classe)
        User novoUsuario = new User();

        // 3. Setamos os dados (Certifique-se que sua classe User tem esses métodos ou @Data do Lombok)
        novoUsuario.setUsername(dados.username());
        novoUsuario.setPassword(passwordEncoder.encode(dados.password()));

        // 4. Salva no Postgres
        userRepository.save(novoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso!");
    }
}
