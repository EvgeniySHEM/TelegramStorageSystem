package ru.sanctio.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.sanctio.dto.MailParams;
import ru.sanctio.service.MailSenderService;

@RequiredArgsConstructor
@Service
public class MailSenderServiceImpl implements MailSenderService {

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${service.activation.uri}")
    private String activationServiceUri;

    private final JavaMailSender javaMailSender;

    @Override
    public void send(MailParams mailParams) {
        String subject = "Активация учетной записи";
        String messageBody = getActivationMailBody(mailParams.getId());
        String emailTo = mailParams.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        javaMailSender.send(mailMessage);
    }

    private String getActivationMailBody(String id) {
        String msg = String.format("Для завершения регистрации перейдите по ссылке:\n%s",
                activationServiceUri);

        return msg.replace("{id}", id);
    }
}
