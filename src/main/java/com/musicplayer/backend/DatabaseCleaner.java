package com.musicplayer.backend;

import java.sql.*;

public class DatabaseCleaner {

    public static void clean() {
        try (Connection conn = DatabaseManager.getConnection(); Statement statement = conn.createStatement()) {

            statement.execute("DELETE FROM Songs");
            statement.execute("DELETE FROM Albums");
            statement.execute("DELETE FROM Artists");

            statement.execute("ALTER TABLE Songs ALTER COLUMN id RESTART WITH 1");
            statement.execute("ALTER TABLE Albums ALTER COLUMN id RESTART WITH 1");
            statement.execute("ALTER TABLE Artists ALTER COLUMN id RESTART WITH 1");

            System.out.println("CONTENTS OF ALL TABLES HAVE BEEN CLEARED");
        } catch (SQLException e) {
            System.err.println("error cleaning " + e.getMessage());
        }
    }
}