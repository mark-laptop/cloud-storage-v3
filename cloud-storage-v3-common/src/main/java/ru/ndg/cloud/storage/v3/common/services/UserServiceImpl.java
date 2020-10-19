package ru.ndg.cloud.storage.v3.common.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.common.dao.UserDao;
import ru.ndg.cloud.storage.v3.common.dao.UserDaoImpl;
import ru.ndg.cloud.storage.v3.common.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);
    private UserDao userDao;

    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User getUserByLoginPassword(String login, String password) {
        User user = userDao.getUserByLoginAndPassword(login, password);
        createUserDirectory(user);
        return user;
    }

    @Override
    public User createUser(String login, String password) {
        User user = userDao.createUser(login, password);
        createUserDirectory(user);
        return user;
    }

    private void createUserDirectory(User user) {
        if (!Files.exists(Paths.get(user.getLogin()))) {
            try {
                Files.createDirectory(Paths.get(user.getLogin()));
            } catch (IOException | NullPointerException e) {
                logger.debug(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
