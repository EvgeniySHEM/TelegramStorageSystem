package ru.sanctio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.sanctio.dao.AppUserDAO;
import ru.sanctio.dao.RawDataDAO;
import ru.sanctio.entity.AppUser;
import ru.sanctio.entity.RawData;
import ru.sanctio.service.MainService;
import ru.sanctio.service.ProducerService;

import static ru.sanctio.entity.enums.UserState.BASIC_STATE;

@Service
public class MainServiceImpl implements MainService {

    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDao;

    @Autowired
    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDao) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDao = appUserDao;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        Message textMessage = update.getMessage();
        User telegramUser = textMessage.getFrom();
        AppUser appUser = findOrSaveAppUser(telegramUser);

        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE!");
        producerService.producerAnswer(sendMessage);
    }

    private AppUser findOrSaveAppUser(User telegremUser) {
        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegremUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegremUser.getId())
                    .userName(telegremUser.getUserName())
                    .firstName(telegremUser.getFirstName())
                    .lastName(telegremUser.getLastName())
                    //todo изменить значение по умолчанию после регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
