package com.example.hush.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.model.NotificationModel;
import com.example.hush.model.Users;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {

    Context context;
    List<NotificationModel> list;

    public NotificationAdapter() {
    }

    public NotificationAdapter(Context context, List<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override

    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.notification_items,
                parent,
                false
        );
        return new NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        NotificationModel model = list.get(position);

        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(model.getUid());

        holder.notification.setText(model.getNotification());
        holder.time.setText(formatTimestamp(model.getTime()));


        reference.get().addOnSuccessListener(documentSnapshot -> {
            Users usersModel = documentSnapshot.toObject(Users.class);

            Glide.with(context.getApplicationContext())
                    .load(usersModel.getProfileImage())
                    .placeholder(R.drawable.ic_person)
                    .timeout(6500)
                    .into(holder.profileImage);
        });


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

    static class NotificationHolder extends RecyclerView.ViewHolder {

        private final CircleImageView profileImage;
        private final TextView notification;
        private final TextView time;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            notification = itemView.findViewById(R.id.notification_notification);
            time = itemView.findViewById(R.id.timeTv_notification);
            profileImage = itemView.findViewById(R.id.circleImageView_notification);
        }
    }
}
