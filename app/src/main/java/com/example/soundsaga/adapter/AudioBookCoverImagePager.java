package com.example.soundsaga.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundsaga.holder.AudioBookCoverIMGHolder;
import com.example.soundsaga.model.Book;
import com.example.soundsaga.model.Chapter;
import com.example.soundsaga.databinding.BookCoverImgBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AudioBookCoverImagePager extends RecyclerView.Adapter<AudioBookCoverIMGHolder>{

    private List<Chapter> chapters;
    private String url;
    public AudioBookCoverImagePager(List<Chapter> chapters,String url) {
       this.chapters = chapters;
       this.url = url;
    }
    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    @NonNull
    @Override
    public AudioBookCoverIMGHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BookCoverImgBinding binding = BookCoverImgBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AudioBookCoverIMGHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioBookCoverIMGHolder holder, int position) {
        Picasso.get()
                .load(url)
                .into(holder.binding.img);
        holder.binding.bookauthor.setText(chapters.get(position).getTitle());
        holder.binding.bookauthor.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }
}
