package com.example.leave.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger("com.example.leave.service.mail");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("[mail] sent -> to={}, subject={}", to, subject);
        } catch (Exception ex) {
            // 失败仅记录日志，不抛出异常，确保不影响主流程
            log.error("[mail] send failed -> to={}, subject={}, error={}", to, subject, ex.getMessage());
        }
    }
}

