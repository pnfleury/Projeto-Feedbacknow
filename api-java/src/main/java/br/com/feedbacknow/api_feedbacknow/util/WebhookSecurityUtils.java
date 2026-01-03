package br.com.feedbacknow.api_feedbacknow.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class WebhookSecurityUtils {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Verifica se a assinatura enviada pela Meta é válida usando os bytes brutos do payload.
     */
    public static boolean verifySignature(byte[] payload, String signature, String appSecret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            log.warn("Assinatura ausente ou formato inválido.");
            return false;
        }

        if (appSecret == null || appSecret.isBlank()) {
            log.error("O App Secret está nulo ou vazio no arquivo de propriedades.");
            return false;
        }

        try {
            // 1. Extrai o hash real vindo da Meta (pula o "sha256=")
            String expectedHash = signature.substring(7);

            // 2. Configura o HMAC-SHA256 com o segredo do App
            Mac mac = Mac.getInstance(ALGORITHM);
            // O .trim() é vital para ignorar espaços invisíveis no arquivo de propriedades
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    appSecret.trim().getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            mac.init(secretKeySpec);

            // 3. Calcula o hash DIRETO nos bytes brutos recebidos
            byte[] rawHmac = mac.doFinal(payload);

            // 4. Converte o resultado para Hexadecimal
            String calculatedHash = bytesToHex(rawHmac);

            // 5. Comparação (o log ajudará a ver se agora os valores batem)
            boolean isValid = calculatedHash.equalsIgnoreCase(expectedHash);

            if (!isValid) {
                log.error("Falha Crítica! Calculado: {} | Esperado: {}", calculatedHash, expectedHash);
            }

            return isValid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro técnico ao calcular a assinatura: ", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }
}
