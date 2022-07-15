package com.example.mymusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    ArrayList<AudioModel> songsList;
    Context context;
    Integer row_index;
    View recycler;

    public MusicListAdapter(ArrayList<AudioModel> songsList, Context context) {
        this.songsList = songsList;
        this.context = context;
    }

    public MusicListAdapter(){

    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate((R.layout.recycler_item), parent, false);
        return new MusicListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AudioModel songData = songsList.get(position);
        holder.titletxt.setText(songData.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to another activity we need position and songs list
                row_index = position;

                MyMediaPlayer.getInstance().reset();
                MyMediaPlayer.currentindex = position;
                Intent intent = new Intent(context, MusicPlayer.class);
                intent.putExtra("LIST", songsList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        });

    }



    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public void filterList(ArrayList<AudioModel> filteredList){
        songsList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView titletxt;
        ImageView iconImage;

        public ViewHolder(View itemView) {
            super(itemView);
            titletxt = itemView.findViewById(R.id.music_title_text);
            iconImage = itemView.findViewById(R.id.icon_view);
        }

    }


}
