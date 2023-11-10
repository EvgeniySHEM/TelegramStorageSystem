package ru.sanctio.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.sanctio.entity.AppDocument;
import ru.sanctio.entity.AppPhoto;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
}
