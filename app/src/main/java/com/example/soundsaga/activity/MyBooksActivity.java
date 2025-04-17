package com.example.soundsaga.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.soundsaga.R;
import com.example.soundsaga.adapter.MyAudioBookAdapter;
import com.example.soundsaga.databinding.ActivityMyBooksBinding;
import com.example.soundsaga.model.Book;
import com.example.soundsaga.util.NetworkUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyBooksActivity extends AppCompatActivity {
    private ActivityMyBooksBinding binding;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;
    private List<Book> list = new ArrayList<Book>();
    private MyAudioBookAdapter adapter;
    private int itemTaped = -1;
    private String json;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spf = getSharedPreferences("SoundSaga",MODE_PRIVATE);
        editor = spf.edit();

        json = spf.getString("firstItem","");
        gson = new Gson();
        list.add(gson.fromJson(spf.getString(json,""),Book.class));

          while(spf.contains(json+"_nextItem")){
                json = spf.getString(json+"_nextItem","");
                list.add(gson.fromJson(spf.getString(json,""),Book.class));
            }
        Book.sortBooksByLastdatetime(list);

        adapter = new MyAudioBookAdapter(list, new MyAudioBookAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position,Book book) {
                    if(!NetworkUtil.isInternetAvailable(MyBooksActivity.this)){
                        Toast.makeText(MyBooksActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(MyBooksActivity.this, AudioBookActivity.class);
                        intent.putExtra("audiobook", book);
                        intent.putExtra("fromMainActivity",false);
                        startActivity(intent);
                        itemTaped = position;
                    }
                }

                @Override
                public void onItemLongClick(int position) {
                    DisplayRemoveItem(position);
                }
            });
        int columnCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
        binding.mainaudiobooklist.setLayoutManager(new GridLayoutManager(this, columnCount));
        binding.mainaudiobooklist.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void DisplayRemoveItem(int position){
        View customView = getLayoutInflater().inflate(R.layout.dialogbox_book_remove, null);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setView(customView)
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    removeItem(list.get(position).getTitle());
                    list.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, list.size());
                    if(list.isEmpty()){
                        editor.putBoolean("isHistory",false);
                        editor.apply();
                    }
                }).setBackground(ContextCompat.getDrawable(this, R.drawable.borderdialogbox));

        final androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        TextView msg = customView.findViewById(R.id.msg);
        msg.setText(Html.fromHtml("Remove your book history for <b><i>"
                + list.get(position).getTitle()
                + "</i></b>?"));
        Picasso.get().load(list.get(position).getImage()).into(
                (ImageView) customView.findViewById(R.id.coverimg));

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.brown));
        Button NegativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        NegativeButton.setTextColor(ContextCompat.getColor(this, R.color.brown));
    }

    private void removeItem(String title) {
        if(spf.getString("firstItem","").equals(title)){
            editor.putString("firstItem",spf.getString(title+"_nextItem",""));
            editor.remove((spf.getString(title+"_nextItem","")) + "_prevItem");
            editor.remove(title + "_nextItem");
        }else if(spf.getString("lastItem","").equals(title)){
            editor.putString("lastItem",spf.getString(title+"_prevItem",""));
            editor.remove((spf.getString(title+"_prevItem","")) + "_nextItem");
            editor.remove(title + "_prevItem");
        }else{
            editor.putString(spf.getString(title+"_prevItem","") + "_nextItem",spf.getString(title+"_nextItem",""));
            editor.putString(spf.getString(title+"_nextItem","") + "_prevItem",spf.getString(title+"_prevItem",""));
            editor.remove(title + "_nextItem");
            editor.remove(title + "_prevItem");
        }
        editor.remove(title);
        editor.apply();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        if(itemTaped != -1){
            Book temp = gson.fromJson(spf.getString(list.get(itemTaped).getTitle(),""),Book.class);
            list.set(itemTaped,temp);
            Book.sortBooksByLastdatetime(list);
            adapter.notifyDataSetChanged();
            itemTaped = -1;
        }
    }
}