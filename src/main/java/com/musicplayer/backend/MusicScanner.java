package com.musicplayer.backend;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.sql.*;

public class MusicScanner {

    public static void startScan() {
        File root = new File("X:/10_MUSIC/ALBUMS/");

        if (!root.exists()) {
            System.err.println("ERROR root folder doesn't exist (check tailscale/database) " + root.getAbsolutePath());
            return;
        }

        System.out.println("Starting full scan: " + root.getAbsolutePath());
        scanFolder(root);
        System.out.println("Scan complete!");
    }

    private static void scanFolder(File folder) {
        File[] items = folder.listFiles();
        if (items == null)
            return;

        for (File item : items) {
            if (item.isDirectory()) {
                scanFolder(item);
            } else if (item.getName().toLowerCase().endsWith(".mp3")
                    || (item.getName().toLowerCase().endsWith(".wav"))) {
                saveToDatabase(item);
            }
        }
    }

    private static void saveToDatabase(File file) {
        try (Connection connectionObj = DatabaseManager.getConnection()) {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            String artist = tag.getFirst(FieldKey.ARTIST);
            String album = tag.getFirst(FieldKey.ALBUM);
            String title = tag.getFirst(FieldKey.TITLE);

            String linuxPath = file.getAbsolutePath().replace("X:", "/mnt/HDD1TB").replace("\\", "/");

            Statement newStatement = connectionObj.createStatement();

            newStatement.execute("MERGE INTO Artists (name) KEY(name) VALUES ('" + artist + "')");
            ResultSet rs1 = newStatement.executeQuery("SELECT id FROM Artists WHERE name = '" + artist + "'");
            rs1.next();
            int artistId = rs1.getInt(1);

            newStatement.execute("MERGE INTO Albums (title, artist_id) KEY(title, artist_id) VALUES ('" + album + "', " + artistId + ")");
            ResultSet rs2 = newStatement.executeQuery("SELECT id FROM Albums WHERE title = '" + album + "' AND artist_id = " + artistId);
            rs2.next();
            int albumId = rs2.getInt(1);

            newStatement.execute("INSERT INTO Songs (title, album_id, file_path) VALUES ('" + title + "', " + albumId + ", '" + linuxPath + "')");

            System.out.println("debug CHECKED " + artist + " - " + title);

        } catch (Exception e) {
            System.err.println("error compiling " + file.getName() + ": " + e.getMessage());
        }
    }
}