package com.example.soundsaga.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaControlReceiver extends BroadcastReceiver {
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent localIntent = new Intent(action);
        context.sendBroadcast(localIntent);
    }
}
