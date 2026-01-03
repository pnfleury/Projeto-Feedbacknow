package br.com.feedbacknow.api_feedbacknow.controller;

import br.com.feedbacknow.api_feedbacknow.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/notifications")
public class AlertControlller {

    private final AlertService alertService;

    public AlertControlller(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    public SseEmitter streamAlerts() {
        return alertService.registerEmitter();
    }

}
