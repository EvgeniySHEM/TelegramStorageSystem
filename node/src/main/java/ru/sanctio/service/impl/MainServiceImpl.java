package ru.sanctio.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.sanctio.dao.AppUserDAO;
import ru.sanctio.dao.RawDataDAO;
import ru.sanctio.entity.AppDocument;
import ru.sanctio.entity.AppPhoto;
import ru.sanctio.entity.AppUser;
import ru.sanctio.entity.RawData;
import ru.sanctio.entity.enums.UserState;
import ru.sanctio.exceptions.UploadFileException;
import ru.sanctio.service.AppUserService;
import ru.sanctio.service.FileService;
import ru.sanctio.service.MainService;
import ru.sanctio.service.ProducerService;
import ru.sanctio.service.enums.LinkType;
import ru.sanctio.service.enums.ServiceCommand;

import java.util.Optional;

import static ru.sanctio.entity.enums.UserState.BASIC_STATE;
import static ru.sanctio.entity.enums.UserState.WAIT_FOR_MAIL_STATE;
import static ru.sanctio.service.enums.ServiceCommand.*;

@Log4j
@RequiredArgsConstructor
@Service
public class MainServiceImpl implements MainService {

    static final String COMMANDS = """
            Список доступных команд:"
            /cancel - отмена выполнения текущей команды."
            /registration - регистрация пользователя.
            """;

    private final RawDataDAO rawDataDAO;

    private final ProducerService producerService;

    private final AppUserDAO appUserDao;

    private final FileService fileService;

    private final AppUserService appUserService;

    @Transactional
    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        UserState userState = appUser.getState();
        String text = update.getMessage().getText();
        String output = "";

        ServiceCommand serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);

        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);

        } else if (WAIT_FOR_MAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);

        } else {
            log.error("Unknown user state" + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppDocument document = fileService.processDoc(update.getMessage());

            String link = fileService.generateLink(document.getId(), LinkType.GET_DOC);

            String answer = "Документ успешно загружено! Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);

            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());

            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);

            String answer = "Фото успешно загружено! Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);

            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getState();
        if (!appUser.getIsActive()) {
            String error = "Зарегистрируйтесь или активируйте свою учетную запись " +
                           "для загрузки контента";
            sendAnswer(error, chatId);

            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String error = "Отмените текущую команду с помощью /cancel для отправки файлов.";
            sendAnswer(error, chatId);

            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);

        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        ServiceCommand serviceCommand = ServiceCommand.fromValue(cmd);

        if (REGISTRATION.equals(serviceCommand)) {
            return appUserService.registerUser(appUser);

        } else if (HELP.equals(serviceCommand)) {
            return COMMANDS;

        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";

        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();

        Optional<AppUser> persistentAppUser = appUserDao.findByTelegramUserId(telegramUser.getId());

        if (persistentAppUser.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();

            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
