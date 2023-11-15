package ru.sanctio.service;

import ru.sanctio.entity.AppUser;

public interface AppUserService {

    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}
