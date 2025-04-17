package com.example.soundsaga.holder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundsaga.databinding.AudioBookBinding;

public class AudioBookHolder extends RecyclerView.ViewHolder {
    public AudioBookBinding binding;
    public AudioBookHolder(@NonNull AudioBookBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
