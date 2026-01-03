package br.com.feedbacknow.api_feedbacknow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public record StatsResponse (
        long total,
        long positivos,
        long negativos,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
        double percentualPositivos,

        @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
        double percentualNegativos
) {}