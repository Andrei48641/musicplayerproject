package com.musicplayer.backend;

import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.sql.*;

public class AudioPlayer {

    // stop command
    private static Player mp3Player; 

    public static void playSong(String songTitle) {
        
        
        stopSong(); 

        String linuxPath = "";

        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT FILE_PATH FROM SONGS WHERE TITLE = '" + songTitle + "'");

            if (rs.next()) {
                linuxPath = rs.getString("FILE_PATH");
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

        try {
            
            FileInputStream fileToPlay = new FileInputStream(windowsPath);
            
            
            mp3Player = new Player(fileToPlay); 
            mp3Player.play();
            
        } catch (Exception e) {
            System.err.println("ERROR playing: " + e.getMessage());
        }
    }

    public static void stopSong() {
        
        if (mp3Player != null) {
            mp3Player.close(); 
            System.out.println("Music stopped!");
        }
    }
}