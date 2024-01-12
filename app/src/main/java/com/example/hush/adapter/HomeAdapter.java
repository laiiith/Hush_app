package com.example.hush.adapter;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.FragmentReplacerActivity;
import com.example.hush.R;
import com.example.hush.model.HomeModel;
import com.example.hush.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {

    private static final int MAX_LINES_COLLAPSED = 1;
    OnPressed onPressed;
    private int color;
    private Activity context;
    private List<HomeModel> list;
    private boolean isExpanded = false;

    public HomeAdapter() {
    }

    public HomeAdapter(Activity context, List<HomeModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items,
                parent, false);

        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {

        HomeModel model = list.get(position);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(list.get(position).getUid());

        if (!(model.getTimeStamp() == null)) {
            holder.timeTv.setText(formatTimestamp(model.getTimeStamp()));
        }

        List<String> likesList = model.getLikes();

        int count = likesList.size();

        if (count == 0) {
            holder.likeCountTv.setVisibility(View.GONE);
        } else if (count == 1) {
            holder.likeCountTv.setVisibility(View.VISIBLE);
            holder.likeCountTv.setText(count + " Like");
        } else {
            holder.likeCountTv.setVisibility(View.VISIBLE);
            holder.likeCountTv.setText(count + " Likes");
        }


        holder.likeCheckBox.setChecked(likesList.contains(user.getUid()));

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


        Glide.with(context.getApplicationContext())
                .load(model.getImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

        reference.get().addOnSuccessListener(documentSnapshot -> {
            Users usersModel = documentSnapshot.toObject(Users.class);

            Glide.with(context.getApplicationContext())
                    .load(usersModel.getProfileImage())
                    .placeholder(R.drawable.ic_person)
                    .timeout(6500)
                    .into(holder.profileImage);

            holder.usernameTv.setText(usersModel.getUniID());
        });


        holder.clickListener(position,
                list.get(position).getId(),
                list.get(position).getUid(),
                list.get(position).getLikes(),
                list.get(position).getImageUrl()
        );


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
        void onLiked(int position, String id, String uid, List<String> likesList, boolean isChecked);

        void onDataChanged();
    }

    class HomeHolder extends RecyclerView.ViewHolder {

        private final CircleImageView profileImage;
        private final TextView usernameTv;
        private final TextView timeTv;
        private final TextView likeCountTv;
        private final TextView descriptionTv;
        private final ImageView imageView;
        private final CheckBox likeCheckBox;
        private final ImageButton commentBtn;
        private final ImageButton shareBtn;


        public HomeHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            usernameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likeCountTv = itemView.findViewById(R.id.likeCountTv);
            imageView = itemView.findViewById(R.id.imageView);
            likeCheckBox = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTv = itemView.findViewById(R.id.descTv);

        }

        public void clickListener(final int position, final String id, final String uid, final List<String> likes, String imageUrl) {

            commentBtn.setOnClickListener(view -> {

                Intent intent = new Intent(context, FragmentReplacerActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("uid", uid);
                intent.putExtra("isComment", true);

                context.startActivity(intent);

            });


            likeCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                onPressed.onLiked(position, id, uid, likes, isChecked);
                onPressed.onDataChanged();

            });
            shareBtn.setOnClickListener(view -> {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, imageUrl);
                intent.setType("text/*");
                context.startActivity(Intent.createChooser(intent, "Share link using ..."));

            });

        }
    }
}
