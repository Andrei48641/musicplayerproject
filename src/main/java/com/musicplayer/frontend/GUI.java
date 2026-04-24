package com.musicplayer.frontend;

import javax.swing.*;
import java.awt.*;
import com.musicplayer.backend.AudioPlayer;

public class GUI extends JFrame {

    public GUI() {
        setTitle("My Clean Music Player");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
       
        setLayout(new FlowLayout());

        
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> {
            
            new Thread(() -> AudioPlayer.playSong("Six Blade Knife")).start();
        });
        
       
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            
            AudioPlayer.stopSong(); 
        });


        add(playButton);
        add(stopButton);

        setVisible(true);
    }

    //test
    
}