package com.musicplayer.backend;

import java.sql.*;

public class DatabaseCleaner {

    public static void clean() {
        try (Connection conn = DatabaseManager.getConnection(); Statement statement = conn.createStatement()) {

            statement.execute("DELETE FROM SONGS");
            statement.execute("DELETE FROM ALBUMS");
            statement.execute("DELETE FROM ARTISTS");

            statement.execute("ALTER TABLE SONGS ALTER COLUMN ID RESTART WITH 1");
            statement.execute("ALTER TABLE ALBUMS ALTER COLUMN ID RESTART WITH 1");
            statement.execute("ALTER TABLE ARITSTS ALTER COLUMN ID RESTART WITH 1");

            System.out.println("CONTENTS OF ALL TABLES HAVE BEEN CLEARED");
        } catch (SQLException e) {
            System.err.println("error cleaning " + e.getMessage());
        }
    }
}