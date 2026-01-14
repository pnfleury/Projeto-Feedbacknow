package br.com.feedbacknow.api_feedbacknow.controller;

import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;
import br.com.feedbacknow.api_feedbacknow.service.SocialMediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final SocialMediaService socialMediaService;

    public DebugController(SocialMediaService socialMediaService){
        this.socialMediaService = socialMediaService;
    }
    @PostMapping("/comentario")
    public ResponseEntity<String> testarComentario(@RequestBody Map<String, String> body) {

        String comentario = body.get("texto");

        SentimentoResponse analise =
                socialMediaService.analisarSentimento(comentario);

        socialMediaService.salvarComentario(
                "post_debug",
                "user_debug",
                "Everton",
                comentario,
                analise
        );

        return ResponseEntity.ok("Salvo com sucesso");
    }

}
