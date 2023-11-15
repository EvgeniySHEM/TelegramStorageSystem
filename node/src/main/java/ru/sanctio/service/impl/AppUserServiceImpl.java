package ru.sanctio.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    @Value("${service.mail.uri}")
    private String mailServiceUri;

    @Autowired
    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

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
            ResponseEntity<String> response = sendRequestToMailService(hashId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                String msg = String.format("Отправка эл.письма на почту %s не удалась", email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return msg;
            }
            return "Вам на почту было отправлено письмо. Перейдите по ссылке для подтверждения регистрации.";
        } else {
            return "Этот email уже используется. Введите корректный email. " +
                    "Для отмены команды ведите /cancel";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String hashId, String email) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        MailParams mailParams = MailParams.builder()
                .id(hashId)
                .emailTo(email)
                .build();
        HttpEntity<MailParams> request = new HttpEntity<>(mailParams, httpHeaders);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}