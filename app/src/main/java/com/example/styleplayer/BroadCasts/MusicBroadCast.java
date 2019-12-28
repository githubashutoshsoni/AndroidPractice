package com.example.styleplayer.BroadCasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class MusicBroadCast extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Timber.d("music broadcast and things are fine");


    }
}
