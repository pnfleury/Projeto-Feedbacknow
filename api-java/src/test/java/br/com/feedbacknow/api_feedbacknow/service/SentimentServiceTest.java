package br.com.feedbacknow.api_feedbacknow.service;


import br.com.feedbacknow.api_feedbacknow.domain.SentimentType;
import br.com.feedbacknow.api_feedbacknow.dto.StatsResponse;
import br.com.feedbacknow.api_feedbacknow.repository.SentimentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SentimentServiceTest {

    @Mock
    private SentimentRepository repository;

    @InjectMocks
    private SentimentService service;

    @Test
    @DisplayName("Deve calcular estatísticas totais quando dias for nulo")
    void deveCalcularEstatisticasTotais() {
        // GIVEN: Simulamos os retornos do repository para o banco todo
        when(repository.count()).thenReturn(10L);
        when(repository.countBySentimento(SentimentType.POSITIVO)).thenReturn(7L);
        when(repository.countBySentimento(SentimentType.NEGATIVO)).thenReturn(3L);

        // WHEN
        StatsResponse stats = service.obterEstatisticas(null);

        // THEN
        assertNotNull(stats);
        assertEquals(10, stats.total());
        assertEquals(7, stats.positivos());
        assertEquals(3, stats.negativos());
        assertEquals(70.0, stats.percentualPositivos()); // (7*100)/10
        assertEquals(30.0, stats.percentualNegativos()); // (3*100)/10

        // Verifica se chamou os métodos corretos do repositório
        verify(repository).count();
    }

    @Test
    @DisplayName("Deve calcular estatísticas filtradas por dias corretamente")
    void deveCalcularEstatisticasComFiltroDeDias() {
        // GIVEN: Simulamos retornos para o filtro de dias
        // Nota: any() é usado porque o LocalDateTime.now() muda a cada milissegundo
        when(repository.countByCriadoEmAfter(any())).thenReturn(5L);
        when(repository.countBySentimentoAndCriadoEmAfter(eq(SentimentType.POSITIVO), any())).thenReturn(4L);
        when(repository.countBySentimentoAndCriadoEmAfter(eq(SentimentType.NEGATIVO), any())).thenReturn(1L);

        // WHEN
        StatsResponse stats = service.obterEstatisticas(7);

        // THEN
        assertEquals(5, stats.total());
        assertEquals(80.0, stats.percentualPositivos()); // (4*100)/5
        assertEquals(20.0, stats.percentualNegativos()); // (1*100)/5

        verify(repository).countByCriadoEmAfter(any());
    }

    @Test
    @DisplayName("Deve retornar resposta zerada quando o total de registros for zero")
    void deveTratarTotalZero() {
        // GIVEN
        when(repository.count()).thenReturn(0L);

        // WHEN
        StatsResponse stats = service.obterEstatisticas(null);

        // THEN
        assertEquals(0, stats.total());
        assertEquals(0.0, stats.percentualPositivos());
        assertEquals(0.0, stats.percentualNegativos());

        // Garante que o código não tentou dividir por zero
        verify(repository, times(1)).count();
    }

}

