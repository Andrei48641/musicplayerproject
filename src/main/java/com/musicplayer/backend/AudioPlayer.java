package com.musicplayer.backend;

import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.sql.*;

public class AudioPlayer {

    public static void playSong(String songTitle) {
        String linuxPath = "";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT file_path FROM Songs WHERE title = ?")) {

            ps.setString(1, songTitle);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                linuxPath = rs.getString("file_path");
            }
        } catch (SQLException e) {
            System.err.println("database ERROR " + e.getMessage());
            return;
        }

        if (linuxPath.isEmpty()) {
            System.err.println("ERROR song not found in database");
            return;
        }

        String windowsPath = linuxPath.replace("/mnt/HDD1TB", "X:").replace("/", "\\");
        System.out.println("NOW PLAYING: " + windowsPath);

        try (FileInputStream fileToPlay = new FileInputStream(windowsPath)) {
            Player mp3Player = new Player(fileToPlay);
            mp3Player.play();
        } catch (Exception e) {
            System.err.println("ERROR playing: " + e.getMessage());
        }
    }
}