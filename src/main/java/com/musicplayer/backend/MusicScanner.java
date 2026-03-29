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
            System.err.println(
                    "ERROR root folder doesn't exist (check tailscale/database) " + root.getAbsolutePath());
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
        try (Connection connectionObj = DatabaseManager.getConnection()) { // init and return the connection
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag(); // inside tag obj there is the metadata stored

            String artist = tag.getFirst(FieldKey.ARTIST); // getFirst gets first value from the FieldKey
            String album = tag.getFirst(FieldKey.ALBUM);
            String title = tag.getFirst(FieldKey.TITLE);

            String linuxPath = file.getAbsolutePath().replace("X:", "/mnt/HDD1TB").replace("\\", "/");

            Statement newStatement = connectionObj.createStatement(); // statement creation

            newStatement.execute("MERGE INTO Artists (name) KEY(name) VALUES ('" + artist.replace("'", "''") + "')");
            ResultSet rs1 = newStatement
                    .executeQuery("SELECT id FROM Artists WHERE name = '" + artist.replace("'", "''") + "'");
            rs1.next();
            int artistId = rs1.getInt(1);

            newStatement.execute(
                    "MERGE INTO Albums (title, artist_id) KEY(title, artist_id) VALUES ('" + album.replace("'", "''")
                            + "', " + artistId + ")");
            ResultSet rs2 = newStatement.executeQuery("SELECT id FROM Albums WHERE title = '" + album.replace("'", "''")
                    + "' AND artist_id = " + artistId);
            rs2.next();
            int albumId = rs2.getInt(1);

            String sql = "MERGE INTO Songs (title, album_id, file_path) KEY(file_path) VALUES (?, ?, ?)";
            PreparedStatement ps = connectionObj.prepareStatement(sql);

            ps.setString(1, title);
            ps.setInt(2, albumId);
            ps.setString(3, linuxPath);

            ps.executeUpdate();

            System.out.println("debug CHECKED " + artist + " - " + title);

        } catch (Exception e) {
            System.err.println("error compiling " + file.getName() + ": " + e.getMessage());
        }
    }
}