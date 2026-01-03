package br.com.feedbacknow.api_feedbacknow.dto;

import java.util.List;

public record PaginaResponse<T>(
        List<T> conteudo,
        int paginaAtual,
        long totalElementos,
        int totalPaginas
) {}