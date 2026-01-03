package br.com.feedbacknow.api_feedbacknow.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;


@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    // 1. ERROS DE VALIDAÇÃO (Campos inválidos no JSON)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDetalhe> handleValidation(MethodArgumentNotValidException ex) {
        String mensagem = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        String campo = ex.getBindingResult().getFieldError().getField();
        // Usamos WARN porque é um erro de cliente, não do servidor.
        logger.warn("Validação falhou no campo '{}': {}", campo, mensagem);
        return ResponseEntity.badRequest()
                .body(new ErroDetalhe("Dados inválidos", mensagem, 400));
    }

    // 2. ERROS DE CONEXÃO (Flask desligado)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErroDetalhe> handleConnection(ResourceAccessException ex) {
        logger.error("Falha ao conectar no Flask: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroDetalhe("IA Indisponível", "serviço de analise fora do ar.", 503));
    }
    // 3. JSON INVALIDO (ERRO 400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDetalhe> handleInvalidJson(HttpMessageNotReadableException ex) {
        logger.warn("Tentativa de requisição com JSON inválido: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErroDetalhe(
                        "JSON Inválido",
                        "O corpo da requisição está ausente ou contém erros de sintaxe (verifique aspas e chaves).",
                        400
                ));
    }
    // 4. ERRO DE MÉTODO ERRADO (Ex: POST em rota de GET - ERRO 405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroDetalhe> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String mensagem = String.format("Método %s não suportado. Tente %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErroDetalhe("Método Inválido", mensagem, 405));
    }

    // 5. ERRO DE URL DIGITADA ERRADA (404 personalizado)
    // Importante: Requer as 2 linhas no application.properties!
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErroDetalhe> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroDetalhe("Rota não encontrada", "A URL digitada não existe.", 404));
    }
    // 6. TRATA ERRO DE CONVERSÃO DE TIPO DE ARQUIVO (ERRO 400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDetalhe> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String valorEnviado = ex.getValue() != null ? ex.getValue().toString() : "";
        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "";

        String mensagem = String.format("O valor '%s' não é válido. Esperava-se um dado do tipo %s.",
                valorEnviado, tipoEsperado);

        logger.warn("Erro de conversão de tipo: {}", mensagem);

        return ResponseEntity.badRequest()
                .body(new ErroDetalhe("Parâmetro Inválido", mensagem, 400));
    }
    // 7. ERRO DE SENTIMENTO INEXISTENTE (ERRO 400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDetalhe> handleInvalidEnum(IllegalArgumentException ex) {
        logger.warn("Tentativa de filtro com sentimento inexistente.");
        return ResponseEntity.badRequest()
                .body(new ErroDetalhe(
                        "Sentimento Inválido",
                        "Use apenas: POSITIVO ou NEGATIVO.",
                        400));
    }

    // 8. QUALQUER OUTRO ERRO
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErroDetalhe> handleGeneric(Exception ex) {
            logger.error("Erro interno não esperado: ", ex);
            return ResponseEntity.internalServerError()
                    .body(new ErroDetalhe(
                            "Erro Interno",
                            "Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.",
                            500
                    ));
        }
}




