package com.musicplayer.frontend;

import javax.swing.*;
import java.awt.*;
import com.musicplayer.backend.AudioPlayer;

public class GUI extends JFrame {

    public GUI() {
        setTitle("My Clean Music Player");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // FlowLayout puts buttons side-by-side in a row
        setLayout(new FlowLayout());

        // 1. THE PLAY BUTTON
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> {
            // Your perfect background thread!
            new Thread(() -> AudioPlayer.playSong("Six Blade Knife")).start();
        });
        
        // 2. THE STOP BUTTON
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            // Stopping is instant, so we don't need a background thread here
            AudioPlayer.stopSong(); 
        });

        // Add them to the screen
        add(playButton);
        add(stopButton);

        setVisible(true);
    }
}