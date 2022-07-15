package com.example.mymusic;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.session.MediaSession;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MusicPlayer extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    TextView title, currentTime, totalTime;
    SeekBar seekbar;
    ImageView pause, next, previous, musicIcon, backButton, repeatOne, shuffle, options;
    RelativeLayout miniPlayer;
    ArrayList<AudioModel> songsList;
    AudioModel currenSong;
    int counter = 0;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    boolean stop = false;
    boolean shuffleOn = false;
        @Override
    public void onBackPressed() {
        super.onBackPressed();
        stop=true;
        shuffleOn = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        options = findViewById(R.id.more_options);
        title = findViewById(R.id.song_title);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekbar = findViewById(R.id.seekBar);
        pause = findViewById(R.id.pause);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        musicIcon = findViewById(R.id.musicIcon);
        backButton = findViewById(R.id.back_button);
        repeatOne = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu myPopup = new PopupMenu(MusicPlayer.this, v);
                myPopup.setOnMenuItemClickListener(MusicPlayer.this);
                myPopup.inflate(R.menu.musicplayer_menu);
                myPopup.show();
            }
        });

        repeatOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;

                if(counter == 1){
                    repeatOne.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                }
                if(counter == 2){
                    repeatOne.setImageResource(R.drawable.ic_baseline_looks_one_24);
                }
                else if(counter == 3){
                    repeatOne.setImageResource(R.drawable.ic_baseline_repeat_24);
                    counter = 0;
                }
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shuffleOn){
                    shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24_on);
                    shuffleOn = true;
                    Toast.makeText(MusicPlayer.this, "Shuffle ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    shuffleOn = false;
                    shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24);
                    Toast.makeText(MusicPlayer.this, "Shuffle OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToActivity();

            }
        });
        boolean isPlaying = true;

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();
        MusicPlayer.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    seekbar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTime.setText(ConvertToMMS(mediaPlayer.getCurrentPosition()+""));

                }
                new Handler().postDelayed(this,100);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(stop == false){
                    if(counter == 0){
                        PlayNext();
                    }
                    else if(counter == 1){
                        setResourcesWithMusic();
                    }
                    else if(counter == 2){
                        pause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    }
                }
                else {

                }

            }
        });



        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void setResourcesWithMusic(){
        currenSong = songsList.get(MyMediaPlayer.currentindex);
        stop = false;
        title.setText(currenSong.getTitle());
        totalTime.setText(ConvertToMMS(currenSong.getDuration()));
        pause.setOnClickListener(v-> PauseMusic());
        next.setOnClickListener(v-> PlayNext());
        previous.setOnClickListener(v-> PlayPrevious());
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currenSong.getPath());
            mediaPlayer.prepare();
            seekbar.setProgress(0);
            seekbar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
        PlayMusic();



        title.setSelected(true);

    }
    

    private void PlayMusic(){

        mediaPlayer.start();
    }

    private void PauseMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            pause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
        }
        else{
            mediaPlayer.start();
            pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
        }
    }

    private void PlayNext(){

        if(shuffleOn == true){
            int num = new Random().nextInt(songsList.size()-1);
            MyMediaPlayer.currentindex = num;
            mediaPlayer.reset();
            setResourcesWithMusic();
        }
        else{
            if(MyMediaPlayer.currentindex == songsList.size()-1){
                pause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                return;
            }
            MyMediaPlayer.currentindex +=1;
            mediaPlayer.reset();
            setResourcesWithMusic();
        }
    }

    private void PlayPrevious(){
        if(MyMediaPlayer.currentindex == 0)
            return;
        MyMediaPlayer.currentindex -=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    public static String ConvertToMMS(String duration){
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private void backToActivity(){
        stop=true;
        shuffleOn = false;
        Intent goBack = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("songName", currenSong.getTitle());
        //goBack.putExtra("Media player", (Serializable) mediaPlayer);
        goBack.putExtras(bundle);
        startActivity(goBack);
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:
                Toast.makeText(this, "item 1 clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item2:
                Toast.makeText(this, "item 2 clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item3:
                Toast.makeText(this, "item 3 clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item4:
                Toast.makeText(this, "item 4 clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}