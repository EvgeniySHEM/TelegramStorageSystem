package ru.sanctio.service.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.sanctio.dao.AppDocumentDao;
import ru.sanctio.dao.BinaryContentDAO;
import ru.sanctio.entity.AppDocument;
import ru.sanctio.entity.BinaryContent;
import ru.sanctio.service.FileService;

@Service
@Log4j
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private final AppDocumentDao appDocumentDao;
    private final BinaryContentDAO binaryContentDAO;

    @Autowired
    public FileServiceImpl(AppDocumentDao appDocumentDao, BinaryContentDAO binaryContentDAO) {
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDAO = binaryContentDAO;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String field = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(field);
        if(response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileAsArrayOfBytes(fileInByte)
                    .build();
            BinaryContent persistentBinaryContent = binaryContentDAO.save(transientBinaryContent);
            Document telegramDoc = telegramMessage.getDocument();
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDao.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }
}
