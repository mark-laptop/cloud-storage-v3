package ru.ndg.cloud.storage.v3.common.dbutils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactoryUtil {

    private static final Logger logger = LogManager.getLogger(HibernateSessionFactoryUtil.class);
    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {}

    public static SessionFactory getSessionFactory() {
        SessionFactory localInstance = sessionFactory;
        if ( localInstance == null) {
            synchronized (HibernateSessionFactoryUtil.class) {
                localInstance = sessionFactory;
                if (localInstance == null) {
                    try {
                        sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                    }
                }
            }
        }
        return sessionFactory;
    }
}
