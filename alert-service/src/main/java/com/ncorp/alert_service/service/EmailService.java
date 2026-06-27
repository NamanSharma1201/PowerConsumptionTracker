package com.ncorp.alert_service.service;

import com.ncorp.alert_service.entity.Alert;
import com.ncorp.alert_service.repository.AlertRepository;
import com.ncorp.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, AlertRepository alertRepository) {
        this.mailSender = mailSender;
        this.alertRepository = alertRepository;
    }

    public void sendEmail(String to, String subject,  String body, Long userId) {
        log.info("Sending email to {}, subject {}, ", to, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("naman.sharma.122003@gmail.com");
        message.setSubject(subject);
        message.setText(body);


        try {
            mailSender.send(message);

            final Alert alertSent = Alert.builder()
                    .sent(true)
                    .createdAt(LocalDateTime.now())
                    .userId(userId)
                    .build();
            alertRepository.saveAndFlush(alertSent);
        }catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            final Alert alertSent = Alert.builder()
                    .sent(false)
                    .createdAt(LocalDateTime.now())
                    .userId(userId)
                    .build();
            alertRepository.saveAndFlush(alertSent);
        }

        log.info("Email sent to {}", to);
    }
}
