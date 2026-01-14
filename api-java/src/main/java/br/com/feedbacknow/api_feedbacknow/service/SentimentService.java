    package br.com.feedbacknow.api_feedbacknow.service;

    import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
    import br.com.feedbacknow.api_feedbacknow.dto.AlertaDTO;
    import br.com.feedbacknow.api_feedbacknow.dto.SentimentUpdateDTO;
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
    import java.util.stream.Collectors;

    @Service
    public class SentimentService {

        private final SentimentRepository repository;
        private AlertService alertService;


        public SentimentService(SentimentRepository repository, AlertService alertService) {
            this.repository = repository;
            this.alertService = alertService;

        }


        public SentimentoResponse saveSentiment(SentimentoResponse response, String origem) {

            SentimentType tipo = SentimentType.valueOf(response.sentimento().toUpperCase());

            String topFeaturesString = "";
            if (response.topFeatures() != null && !response.topFeatures().isEmpty()) {
                topFeaturesString = String.join(", ", response.topFeatures());
            }

            SentimentEntity entity = new SentimentEntity();
            entity.setComentario(response.comentario());
            entity.setSentimento(tipo);

            entity.setOrigem(origem);

            entity.setProbabilidade(response.probabilidade());
            entity.setCriadoEm(LocalDateTime.now());
            entity.setTopFeatures(topFeaturesString);

            SentimentEntity salva = repository.save(entity);

            return new SentimentoResponse(
                    salva.getId(),
                    salva.getComentario(),
                    salva.getSentimento().name(),
                    salva.getProbabilidade(),
                    response.topFeatures(),
                    salva.getCriadoEm(),
                    entity.getOrigem()
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


        // Read
        public Page<SentimentoResponse> listarTodos(Pageable paginacao) {
            return repository
                    .findByVisibleTrueAndDeletedFalse(paginacao)
                    .map(this::mapToResponse);
        }

        //busca nao deletados filtar apenas os visiveis
        public Optional<SentimentoResponse> buscarPorId(Long id) {
            return repository.findByIdAndDeletedFalse(id)
                    .filter(SentimentEntity::getVisible)
                    .map(this::mapToResponse);
        }

        public Page<SentimentoResponse> buscarPorSentimento(
                SentimentType sentimento, Pageable pageable) {
            return repository
                    .findBySentimentoAndVisibleTrueAndDeletedFalse(sentimento, pageable)
                    .map(this::mapToResponse);
        }

        public List<SentimentoResponse> listarTodosSemPagina() {
            return repository
                    .findByVisibleTrueAndDeletedFalse()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }


        // NOVO MÉTODO PARA WEBHOOK

        public void analisarSentimento(
                String comentario,
                String commentId,
                String postId,
                String userId,
                String username,
                String origem

        ) {
            String texto = comentario.toLowerCase();

            String sentimento;
            double probabilidade;
            List<String> features;

            if (texto.contains("lixo") || texto.contains("ruim")
                    || texto.contains("pessimo") || texto.contains("odiei")) {

                sentimento = "NEGATIVO";
                probabilidade = 0.99;
                features = List.of("critica", "reclamacao");

            } else if (texto.contains("excelente") || texto.contains("nota 10")
                    || texto.contains("amei") || texto.contains("bom")) {

                sentimento = "POSITIVO";
                probabilidade = 0.98;
                features = List.of("elogio", "satisfacao");

            } else {
                // fallback obrigatório: tudo que não é negativo vira positivo
                sentimento = "POSITIVO";
                probabilidade = 0.80;
                features = List.of("elogio");
            }

            SentimentoResponse response = new SentimentoResponse(
                    null,
                    comentario,
                    sentimento,
                    probabilidade,
                    features,
                    LocalDateTime.now(),
                    origem
            );

            saveSentiment(response, origem);

            // cria o alerta especial para o react
            AlertaDTO alerta = new AlertaDTO(
                    comentario,
                    sentimento,
                    origem,
                    LocalDateTime.now()
            );

            //envia o alerta
            alertService.enviarAlerta(alerta);

            System.out.println("💬 Comentário analisado e salvo: " + comentario);
        }

        //UPDATE
        public Optional<SentimentoResponse> atualizarParcial (Long id, SentimentUpdateDTO dto){

            return repository.findById(id)
                    .filter(entity -> !entity.getDeleted())
                    .map(entity -> {

                        if (dto.comentario() != null){
                            entity.setComentario(dto.comentario());
                        }

                        if (dto.visible() != null){
                            entity.setVisible(dto.visible());
                        }

                        entity.setUpdatedAt(LocalDateTime.now());

                        SentimentEntity atualizado = repository.save(entity);

                        return mapToResponse(atualizado);
                    });
        }

        // Delete Logico
        public boolean deletarLogico(Long id){

            return repository.findById(id)
                    .filter(entity -> !entity.getDeleted())
                    .map(entity -> {
                        entity.setDeleted(true);
                        entity.setVisible(false);
                        entity.setUpdatedAt(LocalDateTime.now());
                        repository.save(entity);
                        return true;
                    })
                    .orElse(false);

        }


        //Mapper
        private SentimentoResponse mapToResponse(SentimentEntity entity) {
            return new SentimentoResponse(
                    entity.getId(),
                    entity.getComentario(),
                    entity.getSentimento().name(),
                    entity.getProbabilidade(),
                    // Verifica se existem features e transforma a String em Lista
                    entity.getTopFeatures() != null ? List.of(entity.getTopFeatures().split(",")) : null,
                    entity.getCriadoEm(),
                    entity.getOrigem()
            );
        }


        public void ocutarFeedbacksAntigos() {
            //ocultar automaticamente dados antigos
            LocalDateTime limiteNegativos = LocalDateTime.now().minusDays(90); //data de corte de 90 dias

            List<SentimentEntity> negativos = repository
                    .findBySentimentoAndCriadoEmAfter(
                            SentimentType.NEGATIVO, limiteNegativos
                    );

            negativos.forEach(f -> f.setVisible(false));

            repository.saveAll(negativos);

        }
    }
