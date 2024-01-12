package com.example.hush.adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.model.GalleryImages;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryHolder> {

    SendImage onSendImage;
    private List<GalleryImages> list;

    public GalleryAdapter() {

    }

    public GalleryAdapter(List<GalleryImages> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public GalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_items,
                parent, false);
        return new GalleryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryHolder holder, @SuppressLint("RecyclerView") final int position) {

        Glide.with(holder.itemView.getContext().getApplicationContext())
                .load(list.get(position).getPicUri())
                .into(holder.imageView);

        holder.imageView.setOnClickListener(view -> {
            chooseImage(list.get(position).getPicUri());
            list.clear();
        });

    }

    private void chooseImage(Uri picUri) {
        onSendImage.onSend(picUri);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void sendImage(SendImage sendImage) {
        this.onSendImage = sendImage;
    }

    public interface SendImage {
        void onSend(Uri picUri);
    }

    class GalleryHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_Gallery_items);
        }
    }
}
