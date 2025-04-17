package com.example.soundsaga.util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.soundsaga.R;

public class MediaNotificationManager {
    private static final String CHANNEL_ID = "media_controls_channel";
    private static final int NOTIFICATION_ID = 101;
    private final Context context;
    private final MediaSessionCompat mediaSession;

    public MediaNotificationManager(Context context) {
        this.context = context;
        this.mediaSession = new MediaSessionCompat(context, "SoundSagaMediaSession");
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Media Controls",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Controls media playback");
        channel.setImportance(NotificationManager.IMPORTANCE_LOW); // Add this line
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @SuppressLint("MissingPermission")
    public void showMediaNotification(String title, String content, String imageUrl, boolean isPlaying) {
        try {
            // Create explicit intents for media controls
            Intent playPauseIntent = new Intent(context, MediaControlReceiver.class);
            playPauseIntent.setAction(MediaControlReceiver.ACTION_PLAY_PAUSE);

            Intent nextIntent = new Intent(context, MediaControlReceiver.class);
            nextIntent.setAction(MediaControlReceiver.ACTION_NEXT);

            Intent previousIntent = new Intent(context, MediaControlReceiver.class);
            previousIntent.setAction(MediaControlReceiver.ACTION_PREVIOUS);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            flags |= PendingIntent.FLAG_IMMUTABLE;

            PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
                    context, 0, playPauseIntent, flags);

            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                    context, 1, nextIntent, flags);

            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(
                    context, 2, previousIntent, flags);

            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onResourceReady(@NonNull Bitmap albumArt, @Nullable Transition<? super Bitmap> transition) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.logo)
                                    .setContentTitle(title)
                                    .setContentText(content)
                                    .setLargeIcon(albumArt)
                                    .setOngoing(true)
                                    .setAutoCancel(false)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .addAction(R.drawable.round_skip_previous_24, "Previous", previousPendingIntent)
                                    .addAction(isPlaying ? R.drawable.round_pause_24 : R.drawable.round_play_arrow_24,
                                            isPlaying ? "Pause" : "Play",
                                            playPausePendingIntent)
                                    .addAction(R.drawable.round_skip_next_24, "Next", nextPendingIntent)
                                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                            .setShowActionsInCompactView(0, 1, 2))
                                    .setPriority(NotificationCompat.PRIORITY_LOW);

                            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());

                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

}