package ru.sanctio.service;

import ru.sanctio.dto.MailParams;

public interface ConsumerService {

    void consumeRegistrationMail(MailParams mailParams);
}
