package br.com.feedbacknow.api_feedbacknow.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String type
) {
}
