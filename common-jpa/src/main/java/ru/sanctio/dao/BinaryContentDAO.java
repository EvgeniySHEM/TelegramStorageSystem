package ru.sanctio.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanctio.entity.BinaryContent;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {
}
