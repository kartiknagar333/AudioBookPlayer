package com.example.soundsaga.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundsaga.databinding.AudioBookBinding;
import com.example.soundsaga.databinding.MyAudioBookItemBinding;
import com.example.soundsaga.holder.AudioBookHolder;
import com.example.soundsaga.holder.MyAudioBookHolder;
import com.example.soundsaga.model.Book;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyAudioBookAdapter extends RecyclerView.Adapter<MyAudioBookHolder> {

    private List<Book> audioBooks;
    private OnItemClickListener listener;

    public MyAudioBookAdapter(List<Book> audioBooks, OnItemClickListener listener) {
        this.audioBooks = audioBooks;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position,Book book);
        void onItemLongClick(int position);
    }

    @NonNull
    @Override
    public MyAudioBookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyAudioBookItemBinding binding = MyAudioBookItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyAudioBookHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAudioBookHolder holder, int position) {
        holder.binding.booktitle.setText(audioBooks.get(position).getTitle());
        holder.binding.booktitle.setSelected(true);
        holder.binding.bookauthor.setText(audioBooks.get(position).getAuthor());
        holder.binding.bookauthor.setSelected(true);
        Picasso.get()
                .load(audioBooks.get(position).getImage())
                .into(holder.binding.bookcoverimg);
        holder.binding.datetime.setText(audioBooks.get(position).getLastdatetime());
        holder.binding.lastduration.setText(audioBooks.get(position).getLasttime());
        holder.binding.chaptername.setText(audioBooks.get(position).getChapter());
        holder.binding.chaptername.setSelected(true);
        holder.binding.audiobookcv.setOnClickListener(v -> {
            listener.onItemClick(position,audioBooks.get(position));
        });
        holder.binding.audiobookcv.setOnLongClickListener(v -> {
            listener.onItemLongClick(position);
            return true;
        });

    }


    @Override
    public int getItemCount() {
        return audioBooks.size();
    }
}
