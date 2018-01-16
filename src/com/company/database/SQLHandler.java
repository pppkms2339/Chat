package com.company.database;

import java.sql.*;

public class SQLHandler {
    private static Connection conn;
    private static PreparedStatement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:ClientsDB.db");
        } catch (Exception c) {
            c.printStackTrace();
            System.out.println("Database connection error");
        }
    }

    public static void disconnect() {
        try {
            conn.close();
        } catch (Exception c) {
            c.printStackTrace();
            System.out.println("Database connection error");
        }
    }

    public static String getNickByLoginPassword(String login, String password) {
        String w = null;
        try {
            stmt = conn.prepareStatement("SELECT Nickname FROM main WHERE Login = ? AND Password = ?; ");
            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                w = rs.getString("Nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Query Error");
        }
        return w;
    }

    public static boolean isWrongPassword(String login, String password) {
        try {
            stmt = conn.prepareStatement("SELECT Login FROM main WHERE Login = ? AND Password <> ?; ");
            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Query Error");
        }
        return false;
    }

    public static boolean isLoginExist(String login) {
        try {
            stmt = conn.prepareStatement("SELECT Login FROM main WHERE Login = ?");
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Query Error");
        }
        return false;
    }

    public static void addUser(String nickname, String login, String password) {
        try {
            stmt = conn.prepareStatement("INSERT INTO main (Nickname, Login, Password) VALUES (?, ?, ?)");
            stmt.setString(1, nickname);
            stmt.setString(2, login);
            stmt.setString(3, password);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Query Error");
        }
    }
}
