package com.example.soundsaga.holder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundsaga.databinding.AudioBookBinding;
import com.example.soundsaga.databinding.MyAudioBookItemBinding;

public class MyAudioBookHolder extends RecyclerView.ViewHolder {
    public MyAudioBookItemBinding binding;
    public MyAudioBookHolder(@NonNull MyAudioBookItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
