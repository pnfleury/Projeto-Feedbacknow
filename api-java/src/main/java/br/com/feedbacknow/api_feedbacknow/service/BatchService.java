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
    private String apiFlaskBatchUrl;

    @Transactional
    public List<SentimentoResponse> processarCsv(MultipartFile file) {
        List<String> listaTextos = new ArrayList<>();


        // 1. LEITURA ROBUSTA DO CSV (Ignora vírgulas internas e aspas ausentes)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) { // Pula o cabeçalho "comentario" ou "texto"
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

        if (listaTextos.isEmpty()) {
            throw new RuntimeException("O arquivo CSV está vazio ou mal formatado.");
        }

        try {
            // 2. CHAMADA PARA A IA (Flask)
            BatchRequestDTO payload = new BatchRequestDTO(listaTextos);

            // Recebe os dados da IA (incluindo topFeatures)
            SentimentoResponse[] responseArray = restTemplate.postForObject(
                    apiFlaskBatchUrl, payload, SentimentoResponse[].class);

            if (responseArray == null) throw new RuntimeException("Sem resposta da IA");

            // 3. CONVERSÃO PARA ENTITY E SALVAMENTO
            List<SentimentEntity> entidadesParaSalvar = Arrays.stream(responseArray)
                    .map(res -> {
                        SentimentEntity entity = new SentimentEntity();
                        entity.setComentario(res.comentario());
                        // Garante que o Enum entenda o que vem do Python
                        entity.setSentimento(SentimentType.valueOf(res.sentimento().toUpperCase()));
                        entity.setProbabilidade(res.probabilidade());

                        // Salva a lista de topFeatures como uma String separada por vírgula
                        if (res.topFeatures() != null && !res.topFeatures().isEmpty()) {
                            entity.setTopFeatures(String.join(", ", res.topFeatures()));
                        }

                        return entity;
                    })
                    .toList();

            List<SentimentEntity> entidadesSalvas = repository.saveAll(entidadesParaSalvar);

            // 4. RETORNO PARA O USUÁRIO (DTO Final)
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
                                featuresList, // Agora retorna as topFeatures corretamente!
                                entity.getCriadoEm()
                        );
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Erro no processamento em lote: " + e.getMessage());
        }
    }
}
