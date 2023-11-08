package ru.sanctio.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.sanctio.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
