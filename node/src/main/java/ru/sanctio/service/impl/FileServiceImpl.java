package ru.sanctio.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.sanctio.dao.AppDocumentDao;
import ru.sanctio.dao.BinaryContentDAO;
import ru.sanctio.entity.AppDocument;
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
    public AppDocument processDoc(Message externalMessage) {
        return null;
    }
}
