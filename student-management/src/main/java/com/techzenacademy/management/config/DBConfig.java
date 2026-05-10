//package com.techzenacademy.management.config;
//
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.util.Properties;
//
//@Component
//public class DBConfig {
//    private static final Properties props = new Properties();
//    private static final String url;
//    private static final String username;
//    private static final String password;
//
//    static {
//        try (InputStream input = DBConfig.class.getClassLoader()
//                                               .getResourceAsStream("db.properties")) {
//            if (input == null) throw new IllegalStateException("Không tìm thấy file db.properties");
//            props.load(input);
//            url = props.getProperty("db.url");
//            username = props.getProperty("db.username");
//            password = props.getProperty("db.password");
//
//            Class.forName("org.postgresql.Driver");
//        } catch (IOException e) {
//            throw new RuntimeException("Lỗi đọc db.properties: " + e.getMessage(),
//                                       e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("Không tìm thấy PostgreSQL JDBC Driver",
//                                       e);
//        }
//    }
//
//    private DBConfig() {
//    }
//
//    public static Connection getConnection() {
//        try {
//            return DriverManager.getConnection(url,
//                                               username,
//                                               password);
//        } catch (Exception e) {
//            throw new RuntimeException("Không kết nối được DB: " + e.getMessage(),
//                                       e);
//        }
//    }
//}