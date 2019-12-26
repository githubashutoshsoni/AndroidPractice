package com.example.styleplayer.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.styleplayer.Song;

import java.io.IOException;
import java.util.ArrayList;

import timber.log.Timber;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Timber.d("inside startPlayingAudio");


        player.start();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();


        AssetFileDescriptor descriptor = null;
        try {
            descriptor = this.getAssets().openFd("daughtry_alive.mp3");
            player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initMusicPlayer();

    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public class MusicBinder extends Binder {


        public MusicService getService() {
            return MusicService.this;
        }
    }


    public void initMusicPlayer() {

        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set player properties
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);


    }


    public void playSong() {
        //play a song
        player.reset();
        Song playSong = songs.get(songPosn);
//get id
        long currSong = playSong.getID();
//set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Timber.e(e, "Error setting data source");
        }

        player.prepareAsync();

    }

    final IBinder musicBind = new MusicBinder();
    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {



        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }
}
