package ru.sanctio.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanctio.entity.AppUser;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByTelegramUserId(Long id);
}
