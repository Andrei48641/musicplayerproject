package com.musicplayer.backend;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.sql.*;
import java.util.ArrayList;

public class AudioPlayer {

    private static AdvancedPlayer mp3Player;
    private static Thread playerThread;

    private static String currentPath = "";
    private static int pausedFrame = 0;
    private static boolean isPaused = false;

    public static void playSong(String songTitle) {
        stopSong();
        pausedFrame = 0;
        isPaused = false;

        currentPath = getPathFromDB(songTitle);
        if (currentPath.isEmpty()) {
            System.err.println("ERROR song not found in database");
            return;
        }

        playFromFrame(pausedFrame);
    }

    public static void pauseSong() {
        if (mp3Player != null && !isPaused) {
            isPaused = true;
            mp3Player.stop();
            System.out.println("Paused at frame: " + pausedFrame);
        }
    }

    public static void resumeSong() {
        if (isPaused && !currentPath.isEmpty()) {
            isPaused = false;
            playFromFrame(pausedFrame);
        }
    }

    public static void stopSong() {
        isPaused = false;
        pausedFrame = 0;
        if (mp3Player != null) {
            mp3Player.stop();
            mp3Player = null;
        }
        if (playerThread != null) {
            playerThread.interrupt();
            playerThread = null;
        }
        System.out.println("Music stopped!");
    }

    private static void playFromFrame(int startFrame) {
        playerThread = new Thread(() -> {
            try {
                String windowsPath = currentPath.replace("/mnt/HDD1TB", "X:").replace("/", "\\");
                FileInputStream fis = new FileInputStream(windowsPath);
                BufferedInputStream bis = new BufferedInputStream(fis);

                mp3Player = new AdvancedPlayer(bis);
                mp3Player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackStarted(PlaybackEvent e) {
                        System.out.println("NOW PLAYING from frame: " + startFrame);
                    }

                    @Override
                    public void playbackFinished(PlaybackEvent e) {
                        // Track which frame we stopped at for pause/resume
                        pausedFrame = startFrame + e.getFrame();
                        System.out.println("Stopped at frame: " + pausedFrame);
                    }
                });

                mp3Player.play(startFrame, Integer.MAX_VALUE);

            } catch (Exception e) {
                System.err.println("ERROR playing: " + e.getMessage());
            }
        });
        playerThread.start();
    }

    private static String getPathFromDB(String songTitle) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {

            ResultSet rs = statement.executeQuery(
                "SELECT FILE_PATH FROM SONGS WHERE TITLE = '" + songTitle + "'"
            );

            if (rs.next()) {
                return rs.getString("FILE_PATH");
            }
        } catch (SQLException e) {
            System.err.println("database ERROR " + e.getMessage());
        }
        return "";
    }

    public static String[] getPlaylist() {
        ArrayList<String> songs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {

            ResultSet rs = statement.executeQuery(
                "SELECT TITLE FROM SONGS ORDER BY TITLE"
            );

            while (rs.next()) {
                songs.add(rs.getString("TITLE"));
            }

        } catch (SQLException e) {
            System.err.println("ERROR loading playlist: " + e.getMessage());
        }

        return songs.toArray(new String[0]);
    }
}