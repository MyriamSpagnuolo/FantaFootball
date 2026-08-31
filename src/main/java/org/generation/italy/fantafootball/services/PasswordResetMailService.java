package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.security.AppPasswordResetProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetMailService {
    private final JavaMailSender mailSender;
    private final AppPasswordResetProperties properties;

    public PasswordResetMailService(JavaMailSender mailSender, AppPasswordResetProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void send(String recipient, String rawToken) {
        String resetUrl = UriComponentsBuilder.fromUriString(properties.frontendUrl())
                .queryParam("token", rawToken)
                .build().encode().toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(recipient);
        message.setSubject("Reimposta la password di FantaFootball");
        message.setText("Per scegliere una nuova password apri questo link:\n\n" + resetUrl
                + "\n\nIl link scadrà tra " + properties.ttl().toMinutes() + " minuti.");
        mailSender.send(message);
    }
}
