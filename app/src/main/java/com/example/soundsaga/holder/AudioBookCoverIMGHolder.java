package com.example.soundsaga.holder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundsaga.databinding.BookCoverImgBinding;

public class AudioBookCoverIMGHolder extends RecyclerView.ViewHolder {
    public BookCoverImgBinding binding;
    public AudioBookCoverIMGHolder(@NonNull BookCoverImgBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
