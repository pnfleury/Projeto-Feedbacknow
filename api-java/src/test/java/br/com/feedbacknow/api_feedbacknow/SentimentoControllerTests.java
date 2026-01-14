package br.com.feedbacknow.api_feedbacknow;

import br.com.feedbacknow.api_feedbacknow.controller.SentimentoController;
import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentUpdateDTO;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoRequest;
import br.com.feedbacknow.api_feedbacknow.dto.SentimentoResponse;

import br.com.feedbacknow.api_feedbacknow.dto.StatsResponse;
import br.com.feedbacknow.api_feedbacknow.service.BatchService;
import br.com.feedbacknow.api_feedbacknow.service.SentimentService;
import br.com.feedbacknow.api_feedbacknow.service.SentimentoAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(SentimentoController.class)// só carrega o controller
@AutoConfigureMockMvc(addFilters = false)
public class SentimentoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SentimentService sentimentService;

    @MockitoBean
    private SentimentoAnalyzer sentimentoAnalyzer;

    @MockitoBean
    private BatchService batchService;

    @MockitoBean // Adicione este aqui para resolver o erro atual!
    private br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository sentimentRepository;

//    private SentimentoResponse positivo;
//    private SentimentoResponse negativo;
//
//    @BeforeEach
//    void setup() {
//        positivo = new SentimentoResponse(
//                1L,
//                "Muito bom!",
//                SentimentType.POSITIVO.name(),
//                0.95,
//                List.of("feature1"),
//                LocalDateTime.now(),
//                "Instagram"
//        );
//
//        negativo = new SentimentoResponse(
//                2L,
//                "Não gostei.",
//                SentimentType.NEGATIVO.name(),
//                0.85,
//                List.of("feature2"),
//                LocalDateTime.now(),
//                "Facebook"
//        );
//    }
//
//    @Test
//    void deveRetornarTodosOsFeedbacks() throws Exception {
//        List<SentimentoResponse> feedbacks = Arrays.asList(positivo, negativo);
//
//        when(sentimentService.listarTodosSemPagina()).thenReturn(feedbacks);
//
//        mockMvc.perform(get("/sentiments/all")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].sentimento").value("POSITIVO"))
//                .andExpect(jsonPath("$[1].sentimento").value("NEGATIVO"));
//    }
//
//    @Test
//    void deveRetornarFeedbackPositivo() throws Exception {
//        when(sentimentService.buscarPorId(1L)).thenReturn(Optional.of(positivo));
//
//        mockMvc.perform(get("/sentiment/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.comentario").value("Muito bom!"))
//                .andExpect(jsonPath("$.sentimento").value("POSITIVO"));
//    }
//
//    @Test
//    void deveCriarFeedback() throws Exception {
//        when(sentimentService.saveSentiment(any(), any()))
//                .thenReturn(positivo);
//
//        String requestJson = """
//                {
//                    "id": null,
//                    "comentario": "Muito bom!",
//                    "sentimento": "POSITIVO",
//                    "probabilidade": 0.95,
//                    "topFeatures": ["feature1"],
//                    "criadoEm": null,
//                    "origem": "Instagram"
//                }
//                """;
//
//        mockMvc.perform(post("/sentiment")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.comentario").value("Muito bom!"))
//                .andExpect(jsonPath("$.sentimento").value("POSITIVO"));
//    }
//
//    @Test
//    void deveFiltrarPorSentimento() throws Exception {
//        List<SentimentoResponse> feedbacks = List.of(positivo);
//
//        when(sentimentService.buscarPorSentimento(eq(SentimentType.POSITIVO), any()))
//                .thenReturn(new org.springframework.data.domain.PageImpl<>(feedbacks));
//
//        mockMvc.perform(get("/estado/POSITIVO")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                // MUDANÇA AQUI: de $.content para $.conteudo
//                .andExpect(jsonPath("$.conteudo[0].sentimento").value("POSITIVO"));
//    }

    private SentimentoResponse positivo;
    private SentimentoResponse negativo;

//    @BeforeEach
//    void setup() {
//        positivo = new SentimentoResponse(
//                1L,
//                "Muito bom!",
//                SentimentType.POSITIVO.name(),
//                0.95,
//                List.of("feature1"),
//                LocalDateTime.now(),
//                "Instagram"
//        );
//
//        negativo = new SentimentoResponse(
//                2L,
//                "Não gostei.",
//                SentimentType.NEGATIVO.name(),
//                0.85,
//                List.of("feature2"),
//                LocalDateTime.now(),
//                "Facebook"
//        );
//
//        // CORREÇÃO AQUI: O método correto é analyzeComment, não analyze
//        // O método retorna SentimentoResponse, não SentimentType
//        when(sentimentoAnalyzer.analyzeComment(any(String.class)))
//                .thenReturn(positivo);
//    }
//
//    @Test
//    void deveRetornarTodosOsFeedbacks() throws Exception {
//        List<SentimentoResponse> feedbacks = Arrays.asList(positivo, negativo);
//
//        when(sentimentService.listarTodosSemPagina()).thenReturn(feedbacks);
//
//        mockMvc.perform(get("/sentiments/all")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].sentimento").value("POSITIVO"))
//                .andExpect(jsonPath("$[1].sentimento").value("NEGATIVO"));
//    }
//
//    @Test
//    void deveRetornarFeedbackPositivo() throws Exception {
//        when(sentimentService.buscarPorId(1L)).thenReturn(Optional.of(positivo));
//
//        mockMvc.perform(get("/sentiment/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.comentario").value("Muito bom!"))
//                .andExpect(jsonPath("$.sentimento").value("POSITIVO"));
//    }
//
//    @Test
//    void deveCriarFeedback() throws Exception {
//        when(sentimentService.saveSentiment(any(), any()))
//                .thenReturn(positivo);
//
//        // CORREÇÃO: Use "comentario" (não "texto") - porque é o nome no record
//        String requestJson = """
//            {
//                "comentario": "Muito bom!",
//                "origem": "Instagram"
//            }
//            """;
//
//        mockMvc.perform(post("/sentiment")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.comentario").value("Muito bom!"))
//                .andExpect(jsonPath("$.sentimento").value("POSITIVO"));
//    }
//
//    @Test
//    void deveFiltrarPorSentimento() throws Exception {
//        List<SentimentoResponse> feedbacks = List.of(positivo);
//
//        when(sentimentService.buscarPorSentimento(eq(SentimentType.POSITIVO), any()))
//                .thenReturn(new org.springframework.data.domain.PageImpl<>(feedbacks));
//
//        mockMvc.perform(get("/estado/POSITIVO")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.conteudo[0].sentimento").value("POSITIVO"));
//    }

    @BeforeEach
    void setup() {
        positivo = new SentimentoResponse(
                1L,
                "Muito bom! Adorei o produto, funcionou perfeitamente.",
                SentimentType.POSITIVO.name(),
                0.95,
                List.of("qualidade", "usabilidade"),
                LocalDateTime.now(),
                "Instagram"
        );

        negativo = new SentimentoResponse(
                2L,
                "Não gostei do produto, chegou com defeito.",
                SentimentType.NEGATIVO.name(),
                0.85,
                List.of("defeito", "entrega"),
                LocalDateTime.now(),
                "Facebook"
        );

        when(sentimentoAnalyzer.analyzeComment(any()))
                .thenReturn(positivo);
    }

    // ========== TESTES QUE EXISTEM NA SUA API ==========

    // ✅ 1. POST /sentiment (CREATE)
    @Test
    void deveCriarFeedback() throws Exception {
        // saveSentiment espera (SentimentoResponse, String origem)
        when(sentimentService.saveSentiment(any(SentimentoResponse.class), anyString()))
                .thenReturn(positivo);

        // CORREÇÃO: Seu SentimentoRequest espera campo "comentario" (não "texto")
        // E precisa ter pelo menos 5 caracteres (@Size(min=5))
        String requestJson = """
                {
                    "comentario": "Produto excelente, recomendo!"
                }
                """;

        mockMvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comentario").value("Muito bom! Adorei o produto, funcionou perfeitamente."));
    }

    @Test
    void deveRetornar400ParaComentarioVazio() throws Exception {
        String requestJson = """
                {
                    "comentario": ""
                }
                """;

        mockMvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400ParaComentarioMuitoCurto() throws Exception {
        String requestJson = """
                {
                    "comentario": "Oi"
                }
                """;

        mockMvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ✅ 2. GET /sentiment/{id} (READ by ID)
    @Test
    void deveRetornarFeedbackPorId() throws Exception {
        when(sentimentService.buscarPorId(1L))
                .thenReturn(Optional.of(positivo));

        mockMvc.perform(get("/sentiment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comentario").value("Muito bom! Adorei o produto, funcionou perfeitamente."))
                .andExpect(jsonPath("$.sentimento").value("POSITIVO"));
    }

    @Test
    void deveRetornar404ParaFeedbackInexistente() throws Exception {
        when(sentimentService.buscarPorId(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/sentiment/999"))
                .andExpect(status().isNotFound());
    }

    // ✅ 3. GET /sentiments (READ all paginated)
    @Test
    void deveRetornarFeedbacksPaginados() throws Exception {
        List<SentimentoResponse> feedbacks = Arrays.asList(positivo, negativo);
        Page<SentimentoResponse> page = new PageImpl<>(feedbacks, PageRequest.of(0, 10), 2);

        when(sentimentService.listarTodos(any()))
                .thenReturn(page);

        // CORREÇÃO: Seu JSON retorna "conteudo" (com 'u'), não "content"
        mockMvc.perform(get("/sentiments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(2)) // "conteudo" com 'u'
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    // ✅ 4. GET /sentiments/all (READ all without pagination)
    @Test
    void deveRetornarTodosOsFeedbacksSemPaginacao() throws Exception {
        List<SentimentoResponse> feedbacks = Arrays.asList(positivo, negativo);
        when(sentimentService.listarTodosSemPagina())
                .thenReturn(feedbacks);

        mockMvc.perform(get("/sentiments/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sentimento").value("POSITIVO"))
                .andExpect(jsonPath("$[1].sentimento").value("NEGATIVO"));
    }

    // ✅ 5. GET /sentiments/stats (Statistics)
    @Test
    void deveRetornarEstatisticas() throws Exception {
        // CORREÇÃO: Seu StatsResponse tem campos diferentes:
        // total, positivos, negativos, percentualPositivos, percentualNegativos
        StatsResponse stats = new StatsResponse(
                100L, // total
                70L,  // positivos
                20L,  // negativos
                70.0, // percentualPositivos
                20.0  // percentualNegativos
        );

        when(sentimentService.obterEstatisticas(null))
                .thenReturn(stats);

        mockMvc.perform(get("/sentiments/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.positivos").value(70))
                .andExpect(jsonPath("$.negativos").value(20))
                .andExpect(jsonPath("$.percentualPositivos").value(70.0))
                .andExpect(jsonPath("$.percentualNegativos").value(20.0));
    }

    @Test
    void deveRetornarEstatisticasPorPeriodo() throws Exception {
        StatsResponse stats = new StatsResponse(
                50L,  // total
                35L,  // positivos
                10L,  // negativos
                70.0, // percentualPositivos
                20.0  // percentualNegativos
        );

        when(sentimentService.obterEstatisticas(30))
                .thenReturn(stats);

        mockMvc.perform(get("/sentiments/stats?dias=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(50));
    }

    // ========== TESTES PARA ENDPOINTS QUE VOCÊ PODE CRIAR DEPOIS ==========

    // ❌ ESTES NÃO EXISTEM NA SUA API - REMOVA OU COMENTE

    /*
    @Test
    void deveFiltrarPorSentimento() throws Exception {
        // Se você criar este endpoint depois
        List<SentimentoResponse> feedbacks = List.of(positivo);
        Page<SentimentoResponse> page = new PageImpl<>(feedbacks);

        when(sentimentService.buscarPorSentimento(eq(SentimentType.POSITIVO), any()))
                .thenReturn(page);

        mockMvc.perform(get("/sentiments/estado/POSITIVO"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarParcialmenteFeedback() throws Exception {
        // Se você criar este endpoint depois
        String jsonAtualizacao = """
                {
                    "comentario": "Atualizado"
                }
                """;

        mockMvc.perform(patch("/sentiment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizacao))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarLogicoFeedback() throws Exception {
        // Se você criar este endpoint depois
        when(sentimentService.deletarLogico(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/sentiment/1"))
                .andExpect(status().isNoContent());
    }
    */

    // ========== TESTES DE DIAGNÓSTICO ==========
    @Test
    void testeEndpointsExistentes() throws Exception {
        System.out.println("=== ENDPOINTS EXISTENTES ===");

        String[] endpoints = {
                "POST /sentiment",
                "GET  /sentiment/1",
                "GET  /sentiments",
                "GET  /sentiments/all",
                "GET  /sentiments/stats",
                "GET  /sentiments/stats?dias=30"
        };

        for (String endpoint : endpoints) {
            String[] parts = endpoint.split(" ");
            String method = parts[0];
            String url = parts[1];

            try {
                switch (method) {
                    case "GET":
                        mockMvc.perform(get(url))
                                .andDo(result -> {
                                    System.out.println(method + " " + url + " -> " +
                                            result.getResponse().getStatus());
                                });
                        break;
                    case "POST":
                        mockMvc.perform(post(url)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"comentario\":\"Produto teste com mais de 5 chars\"}"))
                                .andDo(result -> {
                                    System.out.println(method + " " + url + " -> " +
                                            result.getResponse().getStatus());
                                });
                        break;
                }
            } catch (Exception e) {
                System.out.println(method + " " + url + " -> ERRO: " + e.getMessage());
            }
        }
    }
}
