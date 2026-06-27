package com.ncorp.alert_service.service;

import com.ncorp.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlertService {
    @KafkaListener(topics = "energy-alert", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
        log.info("Received alertingEvent {}", alertingEvent);

    }
}
