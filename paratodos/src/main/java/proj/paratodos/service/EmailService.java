package proj.paratodos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@rhparatodos.local}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send2FACode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Codigo de Verificacao - Sistema RH Para Todos");
        message.setText(
            "Seu codigo de verificacao e: " + code + "\n\n" +
            "Este codigo expira em 5 minutos.\n" +
            "Se voce nao solicitou este codigo, ignore este email.\n\n" +
            "-- Sistema RH Para Todos"
        );

        mailSender.send(message);
    }
}
