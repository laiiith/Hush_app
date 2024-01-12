package com.example.hush.Admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.model.HomeModel;
import com.example.hush.model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapterAdmin extends RecyclerView.Adapter<PostAdapterAdmin.PostViewHolder> {
    private static final int MAX_LINES_COLLAPSED = 1;
    private Context context;
    private int color;
    private List<HomeModel> list;
    private boolean isExpanded = false;
    private OnPressed onPressed;

    public PostAdapterAdmin() {
    }

    public PostAdapterAdmin(Context context, List<HomeModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_items_admin, parent, false);
        return new PostViewHolder(view);
    }


    @Override
    public void onBindViewHolder(PostViewHolder holder, @SuppressLint("RecyclerView") int position) {

        HomeModel model = list.get(position);

        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(list.get(position).getUid());

        holder.timeTv.setText(formatTimestamp(model.getTimeStamp()));

        holder.descriptionTv.setText(model.getDescription());

        holder.descriptionTv.post(() -> {
            if (holder.descriptionTv.getLineCount() > MAX_LINES_COLLAPSED) {
                holder.descriptionTv.setMaxLines(MAX_LINES_COLLAPSED);
                holder.descriptionTv.setEllipsize(TextUtils.TruncateAt.END);
                holder.descriptionTv.setOnClickListener(view -> {
                    if (isExpanded) {
                        holder.descriptionTv.setMaxLines(MAX_LINES_COLLAPSED);
                        holder.descriptionTv.setEllipsize(TextUtils.TruncateAt.END);
                    } else {
                        holder.descriptionTv.setMaxLines(Integer.MAX_VALUE);
                        holder.descriptionTv.setEllipsize(null);
                    }
                    isExpanded = !isExpanded;
                });
            } else {
                holder.descriptionTv.setOnClickListener(null);
            }

        });

        Random random = new Random();
        color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        try {
            Glide.with(context.getApplicationContext())
                    .load(model.getImageUrl())
                    .placeholder(new ColorDrawable(color))
                    .timeout(7000)
                    .into(holder.imageView);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        reference.get().addOnSuccessListener(documentSnapshot -> {
            Users usersModel = documentSnapshot.toObject(Users.class);

            try {
                Glide.with(context.getApplicationContext())
                        .load(usersModel.getProfileImage())
                        .placeholder(R.drawable.ic_person)
                        .timeout(6500)
                        .into(holder.profileImage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            holder.usernameTv.setText(usersModel.getUniID());
        });

        holder.clickListener(model);
    }

    private String formatTimestamp(Date timestamp) {

        long currentTime = System.currentTimeMillis();

        CharSequence relativeTimeSpan = DateUtils.getRelativeTimeSpanString(
                timestamp.getTime(),
                currentTime,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
        );

        return relativeTimeSpan.toString();
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnPressed(OnPressed onPressed) {
        this.onPressed = onPressed;

    }

    public interface OnPressed {
        void onApprove(HomeModel model);

        void onReject(HomeModel model);

    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImage;
        private final TextView usernameTv;
        private final TextView timeTv;
        private final TextView descriptionTv;
        private final ImageView imageView;
        private final Button approve;
        private final Button reject;

        public PostViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage_admin);
            usernameTv = itemView.findViewById(R.id.nameTv_admin);
            timeTv = itemView.findViewById(R.id.timeTv_admin);
            descriptionTv = itemView.findViewById(R.id.descTv_admin);
            imageView = itemView.findViewById(R.id.imageView_admin);
            approve = itemView.findViewById(R.id.approve_admin);
            reject = itemView.findViewById(R.id.reject_admin);
        }
        public void clickListener(final HomeModel model) {

            approve.setOnClickListener(view -> {
                onPressed.onApprove(model);

            });
            reject.setOnClickListener(view -> {
                onPressed.onReject(model);

            });
        }

    }
}

