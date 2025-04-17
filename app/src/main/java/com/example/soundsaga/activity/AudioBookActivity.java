package com.example.soundsaga.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.soundsaga.adapter.AudioBookCoverImagePager;
import com.example.soundsaga.model.Book;
import com.example.soundsaga.R;
import com.example.soundsaga.databinding.ActivityAudioBookBinding;
import com.example.soundsaga.util.MediaControlReceiver;
import com.example.soundsaga.util.MediaNotificationManager;
import com.example.soundsaga.util.NetworkUtil;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AudioBookActivity extends AppCompatActivity {
    private ActivityAudioBookBinding binding;
    private Book book;
    private int page = 0;
    private AudioBookCoverImagePager viewPageItem;
    private MediaPlayer player;
    private String url;
    private float speed = 1.0f;
    private Timer timer;
    private int startTime;
    private PopupMenu popup, popup2;
    private boolean isRestoring = false;
    private boolean isPlaying = true;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;
    private Timer sleepTimer;
    private TimerTask sleepTimerTask;
    private MediaNotificationManager mMediaNotificationManager;
    private final BroadcastReceiver mediaControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case MediaControlReceiver.ACTION_PLAY_PAUSE:
                    doPlayPause();
                    break;
                case MediaControlReceiver.ACTION_NEXT:
                    page = (page == book.getContents().size() - 1) ? 0 : page + 1;
                    onpageUIupdate(false);
                    break;
                case MediaControlReceiver.ACTION_PREVIOUS:
                    page = Math.max(page - 1, 0);
                    onpageUIupdate(false);
                    break;
            }
        }
    };


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        spf = getSharedPreferences("SoundSaga", MODE_PRIVATE);
        editor = spf.edit();

        Book book = (Book) getIntent().getSerializableExtra("audiobook");

        assert book != null;
        if (!book.getContents().isEmpty()) {
            this.book = book;
            binding.booktitle.setText(book.getTitle());
            viewPageItem = new AudioBookCoverImagePager(book.getContents(), book.getImage());
            binding.bookcover.setAdapter(viewPageItem);

            if (getIntent().getBooleanExtra("fromMainActivity", false)) {
                binding.bookcover.setCurrentItem(page);
            } else {
                isRestoring = true;
                binding.bookcover.setCurrentItem(spf.getInt(book.getTitle() + "_PageIndex", 0));
                page = spf.getInt(book.getTitle() + "_PageIndex", 0);
                String temp = book.getLasttime().split(" ")[0];
                if (temp.split(":").length == 3) {
                    int hours = Integer.parseInt(temp.split(":")[0]);
                    int minutes = Integer.parseInt(temp.split(":")[1]);
                    int seconds = Integer.parseInt(temp.split(":")[2]);
                    startTime = (hours * 3600 + minutes * 60 + seconds) * 1000;
                    binding.starttime.setText(temp);
                } else if (temp.split(":").length == 2) {
                    int minutes = Integer.parseInt(temp.split(":")[0]);
                    int seconds = Integer.parseInt(temp.split(":")[1]);
                    startTime = (minutes * 60 + seconds) * 1000;
                    binding.starttime.setText("00:" + temp);
                }
            }
        } else {
            Toast.makeText(this, "Can't find audiobook", Toast.LENGTH_SHORT).show();
            finish();
        }
        speed = spf.getFloat("speed", 1.0f);
        binding.speed.setText(String.valueOf(speed) + "x");
        CreateSpeedMenu(binding.speed);
        CreateTimerMenu(binding.timer);
        binding.booktitle.setSelected(true);
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (page == book.getContents().size() - 1) {
                    page = 0;
                    onpageUIupdate(false);
                } else {
                    page++;
                    onpageUIupdate(false);
                }

            }
        });
        setupSeekBar();

        presetVars();

        if (savedInstanceState != null) {
            updateFromBundle(savedInstanceState);
        }
        playIt();

        binding.speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        binding.timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup2.show();
            }
        });

        binding.prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page--;
                onpageUIupdate(false);
            }
        });

        binding.nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page == book.getContents().size() - 1) {
                    page = 0;
                    onpageUIupdate(false);
                } else {
                    page++;
                    onpageUIupdate(false);
                }
            }
        });

        binding.bookcover.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                page = position;
                onpageUIupdate(true);
            }
        });

        binding.next15btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goForward();
            }
        });

        binding.back15btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        binding.playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPlayPause();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                editor.putBoolean("isGrantedNotificationPermission",false);
                editor.apply();
                Toast.makeText(this, "Notification permission is not granted. Cannot display media controls.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        mMediaNotificationManager = new MediaNotificationManager(this);

        String songTitle = book.getTitle();
        String songContent = book.getAuthor();
        String albumArtUrl = book.getImage();

        mMediaNotificationManager.showMediaNotification(songTitle, songContent, book.getContents().get(page).getTitle(),true);

    }

    private void CreateSpeedMenu(View view) {
        popup = new PopupMenu(this, view);
        popup.getMenu().add("0.75x");
        popup.getMenu().add("1.0x");
        popup.getMenu().add("1.1x");
        popup.getMenu().add("1.25x");
        popup.getMenu().add("1.5x");
        popup.getMenu().add("1.75x");
        popup.getMenu().add("2.0x");

        popup.setOnMenuItemClickListener(item -> {
            binding.speed.setText(item.getTitle());
            speed = Float.parseFloat(Objects.requireNonNull(item.getTitle()).toString().replace("x", ""));
            player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
            editor.putFloat("speed", speed);
            editor.apply();
            return true;
        });
    }

    private void CreateTimerMenu(View view) {
        popup2 = new PopupMenu(this, view);
        popup2.getMenu().add("SleepTimer");
        popup2.getMenu().add("0.2min");
        popup2.getMenu().add("15min");
        popup2.getMenu().add("30min");
        popup2.getMenu().add("45min");
        popup2.getMenu().add("60min");

        popup2.setOnMenuItemClickListener(item -> {
            if(Objects.requireNonNull(item.getTitle()).toString().equals("SleepTimer")){
                binding.timer.setText("0min");
            }else{
                binding.timer.setText(item.getTitle().toString());
                scheduleStopPlayback(Double.parseDouble(String.valueOf(item.getTitle().toString().split("min")[0])));
            }
            return true;
        });
    }

    private void onpageUIupdate(boolean ispageChanged) {
        if (page == 0) {
            binding.prevbtn.setVisibility(View.INVISIBLE);
        } else {
            binding.prevbtn.setVisibility(View.VISIBLE);
        }
        if (!ispageChanged) {
            binding.bookcover.setCurrentItem(page, true);
        }
        timer.cancel();
        if (!isRestoring) {
            startTime = 0;
            binding.audioseekbar.setProgress(0);
            binding.starttime.setText("00:00:00");
        } else {
            isRestoring = false;
        }
        url = book.getContents().get(page).getUrl();
        playIt();
        mMediaNotificationManager.showMediaNotification(book.getTitle(), book.getContents().get(page).getTitle(), book.getImage(), isPlaying);

    }


    public void playIt() {
        try {
            player.stop();
            player.reset();
            player.setDataSource(url);
            player.prepare();
            int dur = player.getDuration();
            binding.audioseekbar.setMax(dur);
            player.seekTo(startTime);
            player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
            timerCounter();
            if (player.isPlaying() && isPlaying) {
                player.start();
                binding.playpauseimg.setImageResource(R.drawable.pause);
            } else {
                player.pause();
                binding.playpauseimg.setImageResource(R.drawable.play);
            }
        } catch (Exception e) {
            Log.d("AudioBookActivity", "playIt: " + e.getMessage());
        }
    }

    private void timerCounter() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (player != null && player.isPlaying()) {
                        binding.audioseekbar.setProgress(player.getCurrentPosition());

                        binding.starttime.setText(String.format("%s",
                                getTimeStamp(player.getCurrentPosition())));
                        binding.endtime.setText(String.format("%s",
                                getTimeStamp(player.getDuration())));
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private String getTimeStamp(int ms) {
        int t = ms;
        int h = ms / 3600000;
        t -= (h * 3600000);
        int m = t / 60000;
        t -= (m * 60000);
        int s = t / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    private void setupSeekBar() {
        binding.audioseekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        player.seekTo(progress);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mediaControlReceiver);
        book.saveInhistory(binding.starttime.getText().toString(), binding.endtime.getText().toString(),
                book.getContents().get(page).getTitle());
        savehistoryAudioBooklist();
        timer.cancel();
        player.release();
        player = null;
        if (mMediaNotificationManager != null) {
            mMediaNotificationManager.cancelNotification();
        }
        super.onDestroy();
    }

    private void presetVars() {
        url = book.getContents().get(page).getUrl();
    }

    @SuppressLint("SetTextI18n")
    private void updateFromBundle(Bundle bundle) {
        url = bundle.getString("URL");
        startTime = bundle.getInt("TIME");
        speed = bundle.getFloat("SPEED");
        isRestoring = true;
        isPlaying = bundle.getBoolean("isplaying");
        binding.speed.setText(String.valueOf(speed) + "x");
        binding.starttime.setText(bundle.getString("Strattime"));
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("URL", url);
        outState.putInt("TIME", player.getCurrentPosition());
        outState.putFloat("SPEED", speed);
        outState.putString("Strattime", binding.starttime.getText().toString());
        outState.putBoolean("isplaying", isPlaying);
        super.onSaveInstanceState(outState);
    }

    public void goBack() {
        if (player != null && player.isPlaying()) {
            int pos = player.getCurrentPosition();
            pos -= 15000;
            if (pos < 0)
                pos = 0;
            player.seekTo(pos);
        }
    }

    public void goForward() {
        if (player != null && player.isPlaying()) {
            int pos = player.getCurrentPosition();
            pos += 15000;
            if (pos > player.getDuration())
                pos = player.getDuration();
            player.seekTo(pos);
        }
    }

    public void doPlayPause() {
        if (player.isPlaying()) {
            player.pause();
            Objects.requireNonNull(binding.playpauseimg).setImageResource(R.drawable.play);
            isPlaying = false;
        } else {
            player.start();
            Objects.requireNonNull(binding.playpauseimg).setImageResource(R.drawable.pause);
            isPlaying = true;
        }
        mMediaNotificationManager.showMediaNotification(book.getTitle(), book.getContents().get(page).getTitle(), book.getImage(), isPlaying);

    }


    @Override
    protected void onPause() {
        book.saveInhistory(binding.starttime.getText().toString(), binding.endtime.getText().toString(),
                book.getContents().get(page).getTitle());
        savehistoryAudioBooklist();
        super.onPause();
    }



    private void savehistoryAudioBooklist() {
        if (spf.getBoolean("isHistory", false)) {
            if (!spf.contains(book.getTitle())) {
                String currentItem = spf.getString("lastItem", "");
                editor.putString(book.getTitle() + "_prevItem", currentItem);
                editor.putString(currentItem + "_nextItem", book.getTitle());
                editor.putString("lastItem", book.getTitle());
            }
        } else {
            editor.putBoolean("isHistory", true);
            editor.putString("lastItem", book.getTitle());
            editor.putString("firstItem", book.getTitle());
        }
        editor.putInt(book.getTitle() + "_PageIndex", binding.bookcover.getCurrentItem());
        editor.putString(book.getTitle(), new Gson().toJson(book));
        editor.apply();
    }

    private void scheduleStopPlayback(Double minutes) {
        if (sleepTimer != null) {
            sleepTimer.cancel();
        }
        sleepTimer = new Timer();
        sleepTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (player != null && player.isPlaying()) {
                        player.pause();
                        binding.playpauseimg.setImageResource(R.drawable.play);
                        isPlaying = false;
                        Toast.makeText(AudioBookActivity.this,
                                "Sleep timer expired, audio stopped.",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        };
        long delay = (long) (minutes * 60000L);
        sleepTimer.schedule(sleepTimerTask, delay);
        Toast.makeText(this, "Sleep timer set for " + minutes + " minutes", Toast.LENGTH_SHORT).show();

    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaControlReceiver.ACTION_PLAY_PAUSE);
        filter.addAction(MediaControlReceiver.ACTION_NEXT);
        filter.addAction(MediaControlReceiver.ACTION_PREVIOUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mediaControlReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mediaControlReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        book.saveInhistory(binding.starttime.getText().toString(), binding.endtime.getText().toString(),
                book.getContents().get(page).getTitle());
        savehistoryAudioBooklist();
        super.onStop();
    }


}