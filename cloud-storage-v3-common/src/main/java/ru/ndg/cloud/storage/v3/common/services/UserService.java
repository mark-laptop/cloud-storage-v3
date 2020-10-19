package ru.ndg.cloud.storage.v3.common.services;

import ru.ndg.cloud.storage.v3.common.model.User;

public interface UserService {

    User getUserByLoginPassword(String login, String password);
    User createUser(String login, String password);
}
