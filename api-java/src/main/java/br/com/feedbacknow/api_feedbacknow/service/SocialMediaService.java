package br.com.feedbacknow.api_feedbacknow.service;

import br.com.feedbacknow.api_feedbacknow.domain.ComentarioEntity;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;
import br.com.feedbacknow.api_feedbacknow.repository.ComentarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SocialMediaService {

    private final RestTemplate restTemplate;
    private final ComentarioRepository comentarioRepository;

    public SocialMediaService(RestTemplate restTemplate, ComentarioRepository comentarioRepository) {
        this.restTemplate = restTemplate;
        this.comentarioRepository = comentarioRepository;
    }

    public SentimentoResponse analisarSentimento(String texto) {
        return restTemplate.postForObject(
                "http://localhost:5000/sentiment",
                Map.of("texto", texto),
                SentimentoResponse.class
        );
    }

    public void salvarComentario(
            String postId,
            String userId,
            String userName,
            String comentario,
            SentimentoResponse analise
    ) {
        List<String> features = analise.topFeatures() != null
                ? analise.topFeatures()
                : List.of();

        ComentarioEntity entity = new ComentarioEntity(
                postId,
                userId,
                userName,
                comentario,
                analise.sentimento(),
                analise.probabilidade(),
                features
        );

        comentarioRepository.save(entity);
    }
}
