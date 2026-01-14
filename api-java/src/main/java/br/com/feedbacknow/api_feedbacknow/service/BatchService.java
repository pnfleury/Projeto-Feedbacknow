package br.com.feedbacknow.api_feedbacknow.service;

import br.com.feedbacknow.api_feedbacknow.domain.SentimentEntity;
import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
import br.com.feedbacknow.api_feedbacknow.dto.BatchRequestDTO;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;
import br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final RestTemplate restTemplate;
    private final SentimentRepository repository;

    @Value("${api.flask.batch}")
    private String flaskBatchUrl;

    // --- 1. NOVO MÉTODO PARA O REACT (JSON) ---
    @Transactional
    public List<SentimentoResponse> processarListaStrings(List<String> listaTextos) {
        if (listaTextos == null || listaTextos.isEmpty()) {
            throw new RuntimeException("A lista de comentários está vazia.");
        }
        // Chama a lógica centralizada
        return executarAnaliseESalvamento(listaTextos);
    }

    // --- 2. MÉTODO ATUAL (CSV/MULTIPART) ---
    @Transactional
    public List<SentimentoResponse> processarCsv(MultipartFile file) {
        List<String> listaTextos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            boolean primeiraLinha = true;
            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }
                if (!linha.trim().isEmpty()) {
                    listaTextos.add(linha.trim());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo: " + e.getMessage());
        }

        // Chama a mesma lógica centralizada
        return executarAnaliseESalvamento(listaTextos);
    }

    // --- 3. LÓGICA CENTRALIZADA (O "Coração" do Service) ---
    private List<SentimentoResponse> executarAnaliseESalvamento(List<String> listaTextos) {
        try {
            // CHAMADA PARA A IA (Flask)
            BatchRequestDTO payload = new BatchRequestDTO(listaTextos);

            // Importante: Verifique se a URL no seu application.properties está correta
            SentimentoResponse[] responseArray = restTemplate.postForObject(
                    flaskBatchUrl, payload, SentimentoResponse[].class);

            if (responseArray == null) throw new RuntimeException("Sem resposta da IA");

            // CONVERSÃO PARA ENTITY E SALVAMENTO
            List<SentimentEntity> entidadesParaSalvar = Arrays.stream(responseArray)
                    .map(res -> {
                        SentimentEntity entity = new SentimentEntity();
                        entity.setComentario(res.comentario());
                        entity.setSentimento(SentimentType.valueOf(res.sentimento().toUpperCase()));
                        entity.setProbabilidade(res.probabilidade());
                        entity.setOrigem("BATCH_PROCESS"); // Identificador de origem

                        if (res.topFeatures() != null && !res.topFeatures().isEmpty()) {
                            entity.setTopFeatures(String.join(", ", res.topFeatures()));
                        }
                        return entity;
                    })
                    .toList();

            List<SentimentEntity> entidadesSalvas = repository.saveAll(entidadesParaSalvar);

            // RETORNO FORMATADO (DTO)
            return entidadesSalvas.stream()
                    .map(entity -> {
                        List<String> featuresList = null;
                        if (entity.getTopFeatures() != null && !entity.getTopFeatures().isEmpty()) {
                            featuresList = Arrays.asList(entity.getTopFeatures().split(", "));
                        }

                        return new SentimentoResponse(
                                entity.getId(),
                                entity.getComentario(),
                                entity.getSentimento().name(),
                                entity.getProbabilidade(),
                                featuresList,
                                entity.getCriadoEm(),
                                entity.getOrigem()
                        );
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Erro no processamento técnico: " + e.getMessage());
        }
    }
}