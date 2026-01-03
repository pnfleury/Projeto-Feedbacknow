package br.com.feedbacknow.api_feedbacknow.service;

import br.com.feedbacknow.api_feedbacknow.dto.MessageAlertDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AlertService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    public void sendToAll(MessageAlertDTO alert) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(alert);
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

}
