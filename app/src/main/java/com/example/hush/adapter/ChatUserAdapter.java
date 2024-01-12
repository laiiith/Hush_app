package com.example.hush.adapter;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.model.ChatUserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ChatUserHolder> {

    public OnStartChat startChat;
    Activity context;
    List<ChatUserModel> list;

    public ChatUserAdapter(Activity context, List<ChatUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.chat_user_items,
                parent,
                false
        );
        return new ChatUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserHolder holder, int position) {

        fetchImageUrl(list.get(position).getUid(), holder);

        holder.time.setText(formatTimestamp(list.get(position).getTime()));

        holder.lastMessage.setText(list.get(position).getLastMessage());

        holder.itemView.setOnClickListener(view -> {

            List<String> uid = list.get(position).getUid();

            startChat.clicked(position, list.get(position).getUid(),
                    list.get(position).getId());

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

    private void fetchImageUrl(List<String> uids, ChatUserHolder holder) {

        String oppositeUID;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!uids.get(0).equalsIgnoreCase(user.getUid())) {
            oppositeUID = uids.get(0);
        } else {
            oppositeUID = uids.get(1);
        }

        FirebaseFirestore.getInstance().collection("Users")
                .document(oppositeUID).get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        DocumentSnapshot snapshot = task.getResult();

                        Glide.with(context.getApplicationContext())
                                .load(snapshot.getString("profileImage"))
                                .into(holder.profileImage);

                        holder.name.setText(snapshot.getString("uniID"));


                    } else {
                        assert task.getException() != null;

                        Toast.makeText(context, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnStartChat(OnStartChat startChat) {
        this.startChat = startChat;

    }

    public interface OnStartChat {
        void clicked(int position, List<String> uids, String chatID);

    }

    static class ChatUserHolder extends RecyclerView.ViewHolder {

        private final CircleImageView profileImage;
        private final TextView name;
        private final TextView lastMessage;
        private final TextView time;
        private final TextView count;


        public ChatUserHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage_chat_items);
            name = itemView.findViewById(R.id.nameTv_user_items);
            lastMessage = itemView.findViewById(R.id.messageTv_user_items);
            time = itemView.findViewById(R.id.timeTv_user_items);
            count = itemView.findViewById(R.id.messageCountTv_user_items);

            count.setVisibility(View.GONE);
        }
    }


}
