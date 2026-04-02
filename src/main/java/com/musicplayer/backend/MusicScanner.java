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

            Statement stmt = connectionObj.createStatement();

            int artistId = getOrCreateArtist(stmt, tag.getFirst(FieldKey.ARTIST));
            int albumId = getOrCreateAlbum(stmt, tag.getFirst(FieldKey.ALBUM), artistId);
            
            String linuxPath = file.getAbsolutePath().replace("X:", "/mnt/HDD1TB").replace("\\", "/");
            stmt.execute("INSERT INTO Songs (title, album_id, file_path) VALUES ('" + tag.getFirst(FieldKey.TITLE) + "', " + albumId + ", '" + linuxPath + "')");

            System.out.println("debug CHECKED " + tag.getFirst(FieldKey.ARTIST) + " - " + tag.getFirst(FieldKey.TITLE));

        } catch (Exception e) {
            System.err.println("error compiling " + file.getName() + ": " + e.getMessage());
        }
    }

    private static int getOrCreateArtist(Statement stmt, String name) throws SQLException {
        stmt.execute("MERGE INTO Artists (name) KEY(name) VALUES ('" + name + "')");
        ResultSet rs = stmt.executeQuery("SELECT id FROM Artists WHERE name = '" + name + "'");
        rs.next();
        return rs.getInt(1);
    }

    private static int getOrCreateAlbum(Statement stmt, String title, int artistId) throws SQLException {
        stmt.execute("MERGE INTO Albums (title, artist_id) KEY(title, artist_id) VALUES ('" + title + "', " + artistId + ")");
        ResultSet rs = stmt.executeQuery("SELECT id FROM Albums WHERE title = '" + title + "' AND artist_id = " + artistId);
        rs.next();
        return rs.getInt(1);
    }
}