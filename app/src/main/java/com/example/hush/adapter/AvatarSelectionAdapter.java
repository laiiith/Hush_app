package com.example.hush.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;

import java.util.List;

public class AvatarSelectionAdapter extends RecyclerView.Adapter<AvatarSelectionAdapter.AvatarSelectionHolder> {

    AvatarSelectionAdapter.OnPressed onPressed;
    private List<String> list;
    private Activity context;

    public AvatarSelectionAdapter() {
    }

    public AvatarSelectionAdapter(List<String> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public AvatarSelectionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.avatar_items, parent, false);

        return new AvatarSelectionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarSelectionHolder holder, int position) {

        String imageUrl = list.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.imageView);

        holder.clickListener(imageUrl);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnPressed(OnPressed onPressed) {
        this.onPressed = onPressed;
    }

    public interface OnPressed {
        void onAvatarSelected(String selectedAvatarUrl);
    }

    class AvatarSelectionHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public AvatarSelectionHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.avatar_items_imageView);
        }

        public void clickListener(String imageUrl) {
            imageView.setOnClickListener(view -> onPressed.onAvatarSelected(imageUrl));
        }
    }
}

