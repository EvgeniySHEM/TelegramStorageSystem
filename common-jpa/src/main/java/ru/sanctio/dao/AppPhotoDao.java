package ru.sanctio.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanctio.entity.AppPhoto;

public interface AppPhotoDao extends JpaRepository<AppPhoto, Long> {
}
