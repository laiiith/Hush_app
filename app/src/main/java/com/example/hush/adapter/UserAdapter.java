package com.example.hush.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    onUserClicked onUserClicked;
    private List<Users> list;

    public UserAdapter() {
    }

    public UserAdapter(List<Users> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_items,
                parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, @SuppressLint("RecyclerView") int position) {

        if (list.get(position).getUid().equals(user.getUid())) {
            holder.layout.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            holder.layout.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        }

        holder.nameTv.setText(list.get(position).getUniID());

        holder.facultyTv.setText(list.get(position).getFaculty());

        Glide.with(holder.itemView.getContext().getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(R.drawable.ic_person)
                .timeout(6500)
                .into(holder.profileImage);

        holder.itemView.setOnClickListener(view -> onUserClicked.onClicked(list.get(position).getUid()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnUserClicked(onUserClicked onUserClicked) {
        this.onUserClicked = onUserClicked;
    }

    public interface onUserClicked {
        void onClicked(String uid);
    }

    static class UserHolder extends RecyclerView.ViewHolder {

        private final CircleImageView profileImage;
        private final TextView nameTv;
        private final TextView facultyTv;
        private final RelativeLayout layout;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage_user_items);
            layout = itemView.findViewById(R.id.layout_user_items);
            nameTv = itemView.findViewById(R.id.nameTv_user_items);
            facultyTv = itemView.findViewById(R.id.facultyTv_user_items);
        }


    }
}
