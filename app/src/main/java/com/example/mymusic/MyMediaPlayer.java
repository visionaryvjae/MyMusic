package com.example.mymusic;

import android.media.MediaPlayer;

import java.io.Serializable;

public class MyMediaPlayer implements Serializable {
    static MediaPlayer instance;

    public static MediaPlayer getInstance(){
        if(instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static int currentindex = -1;
}
