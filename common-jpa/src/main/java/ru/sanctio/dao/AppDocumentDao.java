package ru.sanctio.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanctio.entity.AppDocument;

public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
