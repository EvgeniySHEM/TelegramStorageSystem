package ru.sanctio.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanctio.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
