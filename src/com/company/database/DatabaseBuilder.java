package com.company.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseBuilder {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC"); // регистрация драйвера
        Connection connection = DriverManager.getConnection("jdbc:sqlite:ClientsDB.db");

        PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS main");
        statement.execute();
        statement.close();

        statement = connection.prepareStatement("CREATE TABLE main (Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "   Nickname TEXT NOT NULL,\n" +
                "   Login TEXT NOT NULL,\n" +
                "   Password TEXT NOT NULL\n" + ")");
        statement.execute();
        statement.close();
        connection.close();

        SQLHandler.connect();
        SQLHandler.addUser("AL", "Anton", "1234");
        SQLHandler.disconnect();
    }
}