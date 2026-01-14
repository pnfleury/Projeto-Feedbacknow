package br.com.feedbacknow.api_feedbacknow.controller;

import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
import br.com.feedbacknow.api_feedbacknow.dto.PaginaResponse;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoRequest;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;
import br.com.feedbacknow.api_feedbacknow.dto.StatsResponse;
import br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository;
import br.com.feedbacknow.api_feedbacknow.service.BatchService;
import br.com.feedbacknow.api_feedbacknow.service.SentimentService;
import br.com.feedbacknow.api_feedbacknow.service.SentimentoAnalyzer;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class SentimentoController {

    private final SentimentoAnalyzer sentimentoAnalyzer;
    private final SentimentService sentimentService;
    private final BatchService batchService;

    // Construtor manual (faz exatamente o que o Lombok faria)

    public SentimentoController(SentimentoAnalyzer sentimentoAnalyzer, SentimentService sentimentService, SentimentRepository repository, BatchService batchService) {
        this.sentimentoAnalyzer = sentimentoAnalyzer;
        this.sentimentService = sentimentService;
        // this.repository = repository;
        this.batchService = batchService;
    }

    //criar
    public ResponseEntity<SentimentoResponse> criar(
            @RequestBody SentimentoResponse request,
            @RequestParam(required = false, defaultValue = "MANUAl") String origem
    ) {
        SentimentoResponse response = sentimentService.saveSentiment(request, origem);
        return ResponseEntity.ok(response);
    }


    // Envia o comentario para a api Flask
    @PostMapping("/sentiment")
    public ResponseEntity<SentimentoResponse> analyze(@Valid @RequestBody SentimentoRequest request) {

        // 1. Recebe a análise da IA (ainda sem ID/Data)
        SentimentoResponse responseIA = sentimentoAnalyzer.analyzeComment(request.comentario());
        if (responseIA == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        // 2. SALVA e CAPTURA o novo objeto que o Service criou com os dados do banco
        SentimentoResponse responseSalva = sentimentService.saveSentiment(responseIA, "Manual");

        // 3. RETORNA o objeto que tem o ID (responseSalva) e não o original (responseIA)
        return ResponseEntity.status(HttpStatus.CREATED).body(responseSalva);
    }

    // Envia lote de comentarios csv para a api Flask
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SentimentoResponse>> processarLote(@RequestParam("file") MultipartFile file) {
        List<SentimentoResponse> resultados = batchService.processarCsv(file);
        return ResponseEntity.ok(resultados);
    }

    // BUSCA ESTATISTICAS NO BANCO
    @GetMapping("sentiments/stats")
    public ResponseEntity<StatsResponse> getStats(
            @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(sentimentService.obterEstatisticas(dias));
    }

    // LISTA REGISTROS DO BANCO
    @GetMapping("/sentiments")
    public ResponseEntity<PaginaResponse<SentimentoResponse>> listar(
            @ParameterObject @PageableDefault(page = 0,
                    size = 20, // aumenta o número de registros por página
                    sort = "criadoEm",
                    direction = Sort.Direction.DESC) Pageable paginacao) {

        Page<SentimentoResponse> paginaResultado = sentimentService.listarTodos(paginacao);

        PaginaResponse<SentimentoResponse> respostaEnxuta = new PaginaResponse<>(
                paginaResultado.getContent(),
                paginaResultado.getNumber(),
                paginaResultado.getTotalElements(),
                paginaResultado.getTotalPages()
        );

        return ResponseEntity.ok(respostaEnxuta);
    }

    // LISTA REGISTRO DO BANCO POR ID
    @GetMapping("sentiment/{id}")
    public ResponseEntity<SentimentoResponse> buscarPorId(@PathVariable Long id) {

        Optional<SentimentoResponse> response = sentimentService.buscarPorId(id);

        return response
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // LISTA TODOS OS REGISTROS DO BANCO COM O SENTIMENTO X
    @GetMapping("/estado/{sentimentoStr}")
    public ResponseEntity<PaginaResponse<SentimentoResponse>> filtrarPorSentimento(
            @PathVariable String sentimentoStr, // Recebe como String
            @ParameterObject @PageableDefault(size = 10,
                    page = 0,
                    sort = "criadoEm",
                    direction = Sort.Direction.DESC) Pageable paginacao) {

        // Converte aqui para maiúsculas para bater com o Enum
        SentimentType tipo = SentimentType.valueOf(sentimentoStr.toUpperCase());

        // Pegamos a página completa do Service
        Page<SentimentoResponse> paginaResultado = sentimentService.buscarPorSentimento(tipo, paginacao);

        // Criamos a resposta enxuta manualmente
        PaginaResponse<SentimentoResponse> respostaEnxuta = new PaginaResponse<>(
                paginaResultado.getContent(),
                paginaResultado.getNumber(),
                paginaResultado.getTotalElements(),
                paginaResultado.getTotalPages()
        );

        return ResponseEntity.ok(respostaEnxuta);
    }

    @Hidden
    @GetMapping("/sentiments/all")
    public ResponseEntity<List<SentimentoResponse>> listarTodosSemPagina() {
        // Busca todos os registros do banco
        List<SentimentoResponse> todos = sentimentService.listarTodosSemPagina();
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    @Hidden
    @PostMapping("/batch/json")
    public ResponseEntity<List<SentimentoResponse>> processarListaJson(@RequestBody List<String> linhas) {
        return ResponseEntity.ok(batchService.processarListaStrings(linhas));
    }
}
