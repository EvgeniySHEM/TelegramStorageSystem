package ru.sanctio.service;

import ru.sanctio.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
