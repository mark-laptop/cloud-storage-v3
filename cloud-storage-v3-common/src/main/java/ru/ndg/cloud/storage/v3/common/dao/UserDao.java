package ru.ndg.cloud.storage.v3.common.dao;

import ru.ndg.cloud.storage.v3.common.model.User;

public interface UserDao {

    User getUserByLoginAndPassword(String login, String password);
    User createUser(String login, String password);
}
