package com.example.soundsaga.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.soundsaga.holder.AudioBookHolder;
import com.example.soundsaga.model.Book;
import com.example.soundsaga.databinding.AudioBookBinding;
import com.example.soundsaga.util.NetworkUtil;
import com.squareup.picasso.Picasso;
import java.util.List;

public class AudioBookAdapter extends RecyclerView.Adapter<AudioBookHolder> {

    private List<Book> audioBooks;
    private OnItemClickListener listener;

    public AudioBookAdapter(List<Book> audioBooks, OnItemClickListener listener) {
        this.audioBooks = audioBooks;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Book book);
        void onItemLongClick(Book book);
    }

    @NonNull
    @Override
    public AudioBookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AudioBookBinding binding = AudioBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AudioBookHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioBookHolder holder, int position) {
        Book book = audioBooks.get(position);

        holder.binding.booktitle.setText(book.getTitle());
        holder.binding.booktitle.setSelected(true);
        holder.binding.bookauthor.setText(book.getAuthor());
        holder.binding.bookauthor.setSelected(true);

        // Click listener
        holder.binding.audiobookcv.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(book);
            }
        });

        // Long click listener
        holder.binding.audiobookcv.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(book);
                return true;
            }
            return false;
        });

        Picasso.get()
                .load(book.getImage())
                .into(holder.binding.bookcoverimg);
    }

    @Override
    public int getItemCount() {
        return audioBooks.size();
    }
}
