package ru.sanctio.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sanctio.CryptoTool;
import ru.sanctio.dao.AppUserDAO;
import ru.sanctio.entity.AppUser;
import ru.sanctio.service.UserActivationService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    @Override
    public boolean activation(String cryptoUserId) {
        Long userId = cryptoTool.idOf(cryptoUserId);
        Optional<AppUser> optionalAppUser = appUserDAO.findById(userId);
        if(optionalAppUser.isPresent()) {
            AppUser user = optionalAppUser.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
}
