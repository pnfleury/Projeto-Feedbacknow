package br.com.feedbacknow.api_feedbacknow.service;

import br.com.feedbacknow.api_feedbacknow.dto.AlertaDTO;
import br.com.feedbacknow.api_feedbacknow.dto.MessageAlertDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AlertService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerEmitter() {
        // 1. Timeout de 30 minutos
        SseEmitter emitter = new SseEmitter(1800000L);

        // 2. O PULO DO GATO: Envie um evento de conexão bem-sucedida IMEDIATAMENTE
        try {
            emitter.send(SseEmitter.event()
                    .name("connected") // Nome do evento
                    .data("Conectado com sucesso!")); // Conteúdo
        } catch (IOException e) {
            return emitter; // Se falhar aqui, o cliente já caiu
        }

        // 3. Gerenciamento de ciclo de vida
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete(); // Fecha formalmente antes de remover
            emitters.remove(emitter);
        });
        emitter.onError((e) -> emitters.remove(emitter));

        emitters.add(emitter);
        return emitter;
    }

    public void sendToAll(MessageAlertDTO alert) {
        // Com CopyOnWriteArrayList, você pode iterar e remover direto com segurança
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(alert));
            } catch (IOException e) {
                // Se falhar o envio, o cliente não existe mais
                emitters.remove(emitter);
            }
        }
    }

    // Unificando a lógica: recomendo usar apenas um método de envio
    public void enviarAlerta(AlertaDTO alerta) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(alerta));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
