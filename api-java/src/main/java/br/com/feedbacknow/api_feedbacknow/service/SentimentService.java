    package br.com.feedbacknow.api_feedbacknow.service;

    import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
    import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;
    import br.com.feedbacknow.api_feedbacknow.dto.StatsResponse;
    import br.com.feedbacknow.api_feedbacknow.domain.SentimentEntity;
    import br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class SentimentService {

        private final SentimentRepository repository;
        private AlertService alertService;


        public SentimentService(SentimentRepository repository, AlertService alertService) {
            this.repository = repository;
            this.alertService = alertService;

        }

        // ==========================
        // MÉTODOS EXISTENTES
        // ==========================
        public SentimentoResponse saveSentiment(SentimentoResponse response) {

            SentimentType tipo = SentimentType.valueOf(response.sentimento().toUpperCase());

            String topFeaturesString = "";
            if (response.topFeatures() != null && !response.topFeatures().isEmpty()) {
                topFeaturesString = String.join(", ", response.topFeatures());
            }

            SentimentEntity entity = new SentimentEntity();
            entity.setComentario(response.comentario());
            entity.setSentimento(tipo);
            entity.setProbabilidade(response.probabilidade());
            entity.setCriadoEm(LocalDateTime.now());
            entity.setTopFeatures(topFeaturesString);

            SentimentEntity salva = repository.save(entity);

            // 🔥 CRIA UM NOVO DTO (imutável)
            return new SentimentoResponse(
                    salva.getId(),
                    salva.getComentario(),
                    salva.getSentimento().name(),
                    salva.getProbabilidade(),
                    response.topFeatures(),
                    salva.getCriadoEm()
            );

        }

        public StatsResponse obterEstatisticas(Integer dias) {
            long total;
            long pos;
            long neg;

            if (dias == null || dias <= 0) {
                total = repository.count();
                pos = repository.countBySentimento(SentimentType.POSITIVO);
                neg = repository.countBySentimento(SentimentType.NEGATIVO);
            } else {
                LocalDateTime dataCorte = LocalDateTime.now().minusDays(dias);
                total = repository.countByCriadoEmAfter(dataCorte);
                pos = repository.countBySentimentoAndCriadoEmAfter(SentimentType.POSITIVO, dataCorte);
                neg = repository.countBySentimentoAndCriadoEmAfter(SentimentType.NEGATIVO, dataCorte);
            }

            if (total == 0) return new StatsResponse(0, 0, 0, 0.0, 0.0);

            double percPos = BigDecimal.valueOf((pos * 100.0) / total)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            double percNeg = BigDecimal.valueOf((neg * 100.0) / total)
                    .setScale(2,RoundingMode.HALF_UP)
                    .doubleValue();

            return new StatsResponse(total, pos, neg, percPos, percNeg);
        }

        public Page<SentimentoResponse> listarTodos(Pageable paginacao) {
            return repository.findAll(paginacao).map(this::mapToResponse);
        }

        public Optional<SentimentoResponse> buscarPorId(Long id) {
            return repository.findById(id).map(this::mapToResponse);
        }

        public Page<SentimentoResponse> buscarPorSentimento(SentimentType sentimento, Pageable pageable) {
            return repository.findBySentimento(sentimento, pageable).map(this::mapToResponse);
        }

        private SentimentoResponse mapToResponse(SentimentEntity entity) {
            return new SentimentoResponse(
                    entity.getId(),
                    entity.getComentario(),
                    entity.getSentimento().name(),
                    entity.getProbabilidade(),
                    entity.getTopFeatures() != null ? List.of(entity.getTopFeatures().split(",")) : null,
                    entity.getCriadoEm()
            );
        }


        // NOVO MÉTODO PARA WEBHOOK

        public void analisarSentimento(
                String comentario,
                String commentId,
                String postId,
                String userId,
                String username
        ) {
            String texto = comentario.toLowerCase();

            String sentimento;
            double probabilidade;
            List<String> features;

            if (texto.contains("lixo") || texto.contains("ruim") || texto.contains("pessimo") || texto.contains("odiei")) {
                sentimento = "NEGATIVO";
                probabilidade = 0.99;
                features = List.of("critica", "reclamacao");
            } else if (texto.contains("excelente") || texto.contains("nota 10") || texto.contains("amei") || texto.contains("bom")) {
                sentimento = "POSITIVO";
                probabilidade = 0.98;
                features = List.of("elogio", "satisfacao");
            } else {
                sentimento = "NEUTRO";
                probabilidade = 0.50;
                features = List.of("geral");
            }

            SentimentoResponse response = new SentimentoResponse(
                    null,
                    comentario,
                    sentimento,
                    probabilidade,
                    features,
                    LocalDateTime.now()
            );

            saveSentiment(response);

            System.out.println("💬 Comentário analisado e salvo: " + comentario);
        }

    }
