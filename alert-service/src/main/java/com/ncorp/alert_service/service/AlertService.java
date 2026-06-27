package com.ncorp.alert_service.service;

import com.ncorp.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlertService {
    private final EmailService emailService;

    @Autowired
    public AlertService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
        log.info("Received alertingEvent {}", alertingEvent);
        final String subject = "Energy Usage Alert for User " + alertingEvent.userId();

        final String message = "Alert: " + alertingEvent.message() + "\nThreshold: " + alertingEvent.threshold() +
                "\nEnergy Consumed: " + alertingEvent.energyConsumed();

        emailService.sendEmail(alertingEvent.email(), subject, message, alertingEvent.userId());
     }
}
