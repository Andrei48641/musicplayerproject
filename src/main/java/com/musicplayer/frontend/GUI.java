package com.musicplayer.frontend;

import javax.swing.*;
import java.awt.*;

import com.musicplayer.backend.AudioPlayer;

public class GUI extends JFrame {

    public GUI() {
        setTitle("test");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton playButton = new JButton("play AC/DC");

        playButton.addActionListener(e -> {
            new Thread(() -> AudioPlayer.playSong("Back In Black")).start();
        });

        add(playButton);

        setVisible(true);
    }
}