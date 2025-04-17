package com.example.soundsaga.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.soundsaga.api.BookContents;
import com.example.soundsaga.adapter.AudioBookAdapter;
import com.example.soundsaga.fetcher.GetAudiobook;
import com.example.soundsaga.model.Book;
import com.example.soundsaga.R;
import com.example.soundsaga.databinding.ActivityMainBinding;
import com.example.soundsaga.util.NetworkUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private ActivityMainBinding binding;
    private boolean keepOn = true;
    private static final long minSplashTime = 2000;
    private long startTime;
    private BookContents bookContents;
    private AudioBookAdapter adapter;
    private JSONArray books = new JSONArray();
    private SharedPreferences spf;
    List<Book> filterlist = new ArrayList<>();
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        startTime = System.currentTimeMillis();
        spf  = getSharedPreferences("SoundSaga",MODE_PRIVATE);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        splashScreen.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
                    @Override
                    public boolean shouldKeepOnScreen() {
                        return keepOn || (System.currentTimeMillis() - startTime <= minSplashTime);
                    }
                });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        editor = spf.edit();
        bookContents = new BookContents(this);
        bookContents.getData();

        binding.btnhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spf.getBoolean("isHistory",false)){
                    Intent intent = new Intent(MainActivity.this, MyBooksActivity.class);
                    startActivity(intent);
                }else{
                    DisplayDailofforNoHistory();
                }
            }
        });

        binding.searchinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    List<Book> templist= new ArrayList<>();
                    for (Book b : filterlist) {
                        if (b.getTitle().toLowerCase().contains(s.toString().toLowerCase()) ||
                                b.getAuthor().toLowerCase().contains(s.toString().toLowerCase())) {
                            templist.add(b);
                        }
                    }

                    adapter = new AudioBookAdapter(templist, new AudioBookAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Book book) {
                            if(!NetworkUtil.isInternetAvailable(MainActivity.this)){
                                Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                            }else{
                                Intent intent = new Intent(MainActivity.this, AudioBookActivity.class);
                                intent.putExtra("audiobook", book);
                                intent.putExtra("fromMainActivity",true);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onItemLongClick(Book book) {
                            DisplayDialog(book);
                        }
                    });
                    binding.mainaudiobooklist.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

    }

    public void getBooks(JSONArray audiobooks ) {
        if (audiobooks == null) {
            return;
        }
        books = audiobooks;
        filterlist.clear();
        filterlist.addAll(GetAudiobook.getAudiobooks(audiobooks));
        loaddata(filterlist);
        keepOn = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("bookjsonarray", books != null ? books.toString() : "");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String bookstring = savedInstanceState.getString("bookjsonarray");
        if (bookstring != null) {
            try {
                books = new JSONArray(bookstring);
                filterlist.clear();
                filterlist.addAll(GetAudiobook.getAudiobooks(books));
                loaddata(filterlist);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void loaddata(List<Book> list) {
        int columnCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;

        adapter = new AudioBookAdapter(list, new AudioBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                if(!NetworkUtil.isInternetAvailable(MainActivity.this)){
                    Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(MainActivity.this, AudioBookActivity.class);
                    intent.putExtra("audiobook", book);
                    intent.putExtra("fromMainActivity",true);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(Book book) {
                DisplayDialog(book);
            }
        });

        binding.mainaudiobooklist.setLayoutManager(new GridLayoutManager(this, columnCount));
        binding.mainaudiobooklist.setAdapter(adapter);
        binding.mainaudiobooklist.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(this, R.anim.entry));
        binding.mainaudiobooklist.scheduleLayoutAnimation();
    }

    private void DisplayDailofforNoHistory(){
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.logo)
                .setTitle("My Books shelf is empty")
                .setMessage("You currently have no audiobooks in progress.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                }).setBackground(ContextCompat.getDrawable(this, R.drawable.borderdialogbox));

        final androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.brown));
    }

    @SuppressLint("SetTextI18n")
    private void DisplayDialog(Book book) {
        View customView = getLayoutInflater().inflate(R.layout.dialogbox_layout, null);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setView(customView)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                }).setBackground(ContextCompat.getDrawable(this, R.drawable.borderdialogbox));

        final androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.brown));

        TextView title = customView.findViewById(R.id.title);
        TextView author = customView.findViewById(R.id.author);

        title.setText(book.getTitle());
        author.setText(book.getAuthor());

        TextView numchapter = customView.findViewById(R.id.numchapter);
        TextView duration = customView.findViewById(R.id.duration);
        TextView language = customView.findViewById(R.id.language);

        numchapter.setText(book.getContents().size()==1 ? book.getContents().size() + " Chapter" : book.getContents().size() + " Chapters");
        duration.setText("Duration: " + book.getDuration());
        language.setText("Language: " +book.getLanguage());

        ImageView coverimg = customView.findViewById(R.id.coverimg);
        Picasso.get().load(book.getImage()).into(coverimg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                editor.putBoolean("isGrantedNotificationPermission",true);
                editor.apply();
            } else {
                Toast.makeText(this, "Notification permission denied. Notifications may not work to appear a Audio Notification.", Toast.LENGTH_LONG).show();
            }
        }
    }

}