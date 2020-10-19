package ru.ndg.cloud.storage.v3.common.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.ndg.cloud.storage.v3.common.dbutils.HibernateSessionFactoryUtil;
import ru.ndg.cloud.storage.v3.common.model.User;

public class UserDaoImpl implements UserDao {
    @Override
    public User getUserByLoginAndPassword(String login, String password) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Query<User> query = session.createQuery("From User where login=:login and password=:password", User.class);
        query.setParameter("login", login);
        query.setParameter("password", password);
        return query.getSingleResult();
    }

    @Override
    public User createUser(String login, String password) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.getTransaction().begin();
        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        session.save(user);
        session.getTransaction().commit();
        return user;
    }
}
