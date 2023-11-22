package ru.sanctio.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Сервис, считывающий ответы из Node
 */
public interface AnswerConsumer {
    void consume(SendMessage sendMessage);
}
