package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Serializable {
    RecyclerView recyclerView;
    TextView txtNoMusic, SongPlaying;
    ImageView searchButton, pause, previous, next;
    EditText searchBar;
    View view;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    ArrayList<AudioModel> alteredList;
    AudioModel currenSong;
    int counter=0;
    MediaPlayer mPlayer = MyMediaPlayer.instance;
    boolean search= false;
    //Bundle bundle = getIntent().getExtras();

    String currentSongName = "Song Name";

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER)
        {
            SearchItem(search, alteredList);
            search = false;
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        recyclerView = findViewById(R.id.recycler);
        txtNoMusic = findViewById(R.id.no_songs);
        SongPlaying = findViewById(R.id.song_playing);
        SongPlaying.setSelected(true);
        searchBar = findViewById(R.id.search);
        searchButton = findViewById(R.id.btn_search);
        pause = findViewById(R.id.pause);
        previous = findViewById(R.id.mini_previous);
        next = findViewById(R.id.mini_next);

        /*pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Button pressed", Toast.LENGTH_SHORT).show();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PlayNext();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PlayPrevious();
            }
        });*/

        if(bundle != null){
            currentSongName = bundle.getString("songName");
            //mPlayer = (MediaPlayer) getIntent().getSerializableExtra("Media player");
        }
        else{
            currentSongName = "Song Name";
        }
        SongPlaying.setText(currentSongName);

        if (CheckPerm() == false){
            requestPerm();
            return;
        }

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC +" !=0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED);
        while (cursor.moveToNext()){
            AudioModel songData = new AudioModel(cursor.getString(1),cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists()){
                songsList.add(songData);
            }
            counter++;

        }
        //ArrayList<AudioModel> place = new ArrayList<>();
        songsList = Reverse(songsList);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                alteredList = filter(s.toString());
                search = true;
            }
        });

        for (int i = counter-1; i >= 0; i--){
            if (songsList.size() == 0){
                txtNoMusic.setVisibility(View.VISIBLE);
            }
            else{
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(new MusicListAdapter(songsList, getApplicationContext()));
                    txtNoMusic.setVisibility(View.GONE);
            }
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchItem(search, alteredList);
                search = false;
            }
        });


    }

    boolean CheckPerm(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    private void SearchItem(boolean searching, ArrayList<AudioModel> finalList){
        if (searching){
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicListAdapter(finalList, getApplicationContext()));
            txtNoMusic.setVisibility(View.GONE);
        }
    }

    private ArrayList<AudioModel> filter(String text){
        ArrayList<AudioModel> filteredList = new ArrayList<>();

        for(AudioModel item: songsList){
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }

        return filteredList;

    }

    void requestPerm(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this, "READ PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
        }
    }

    public ArrayList Reverse(ArrayList<AudioModel> mySongs){
        ArrayList<AudioModel> newSongs = new ArrayList<>();
        for (int i = mySongs.size()-1; i >= 0; i--){
            mySongs.toArray();
            newSongs.add(mySongs.get(i));
        }
        return newSongs;
    }


    void Playing(View view){
        recyclerView = findViewById(R.id.recycler);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_main,container,false);
        return view;
    }

    void setResourcesWithMusic(){
        currenSong = songsList.get(MyMediaPlayer.currentindex);
        SongPlaying.setText(currenSong.getTitle());
        mPlayer.reset();
        try {
            mPlayer.setDataSource(currenSong.getPath());
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();

    }

    public void PauseMusic(View view){
        pause = findViewById(R.id.mini_pause);
        if(mPlayer.isPlaying()){
            mPlayer.pause();
            pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
        else{
            mPlayer.start();
            pause.setImageResource(R.drawable.ic_baseline_pause_24);
        }
    }

    public void PlayNext(View view){

        if(MyMediaPlayer.currentindex == songsList.size()-1){
                pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                return;
            }
            MyMediaPlayer.currentindex +=1;
            mPlayer.reset();
            setResourcesWithMusic();
    }

    public void PlayPrevious(View view){
        if(MyMediaPlayer.currentindex == 0)
            return;
        MyMediaPlayer.currentindex -=1;
        mPlayer.reset();
        setResourcesWithMusic();
    }
}