
package com.techzenacademy.management.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    // Khai báo SessionFactory || buildSessionFactory(); Khi class load → tạo
    // SessionFactory luôn
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // chịu trách nhiệm tạo SessionFactory
    private static SessionFactory buildSessionFactory() {
        try {
            System.out.println(">>> Building SessionFactory...");
            return new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            System.err.println("!!! Initial SessionFactory creation failed." + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    // Tạo method để mọi nơi lấy SessionFactory
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Tạo method để đóng SessionFactory
    public static void closeSession() {
        getSessionFactory().close();
    }
}
