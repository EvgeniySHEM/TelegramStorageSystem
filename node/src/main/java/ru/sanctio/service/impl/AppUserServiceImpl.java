package ru.sanctio.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sanctio.CryptoTool;
import ru.sanctio.dao.AppUserDAO;
import ru.sanctio.dto.MailParams;
import ru.sanctio.entity.AppUser;
import ru.sanctio.service.AppUserService;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;

import static ru.sanctio.entity.enums.UserState.BASIC_STATE;
import static ru.sanctio.entity.enums.UserState.WAIT_FOR_MAIL_STATE;

@Log4j
@RequiredArgsConstructor
@Service
public class AppUserServiceImpl implements AppUserService {

    @Value("${spring.rabbitmq.queues.registration-mail}")
    private String registrationMailQueue;

    private final RabbitTemplate rabbitTemplate;

    private final AppUserDAO appUserDAO;

    private final CryptoTool cryptoTool;

    @Override
    public String registerUser(AppUser appUser) {

        if (appUser.getIsActive()) {
            return "Вы уже зарегистрированы";
        } else if (appUser.getEmail() != null) {
            return "Вам на почту уже было отправлено письмо. " +
                    "Перейдите по ссылке в письме для подтверждения регистрации";
        }

        appUser.setState(WAIT_FOR_MAIL_STATE);
        appUserDAO.save(appUser);

        return "Введите, пожалуйста, ваш email:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {

        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            return "Введите, пожалуйста, корректный email. Для отмены команды введите /cancel";
        }

        Optional<AppUser> optionalEmail = appUserDAO.findByEmail(email);

        if (optionalEmail.isEmpty()) {
            appUser.setEmail(email);
            appUser.setState(BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            String hashId = cryptoTool.hashOf(appUser.getId());

            sendRegistrationMail(hashId, email);
            return "Вам на почту было отправлено письмо. Перейдите по ссылке для подтверждения регистрации.";
        } else {
            return "Этот email уже используется. Введите корректный email. " +
                    "Для отмены команды ведите /cancel";
        }
    }

    private void sendRegistrationMail(String hashId, String email) {

        MailParams mailParams = MailParams.builder()
                .id(hashId)
                .emailTo(email)
                .build();

        rabbitTemplate.convertAndSend(registrationMailQueue, mailParams);
    }
}