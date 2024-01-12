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
import com.example.hush.model.CommentModel;
import com.example.hush.model.Users;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private Context context;
    private List<CommentModel> list;

    public CommentAdapter() {
    }

    public CommentAdapter(Context context, List<CommentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_items
                , parent, false);

        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {

        CommentModel model = list.get(position);

        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(model.getUid());


        if (!(model.getTimeStamp() == null)) {
            holder.timeTv.setText(formatTimestamp(model.getTimeStamp()));
        }

        holder.commentTv.setText(model.getComment());


        reference.get().addOnSuccessListener(documentSnapshot -> {

            Users usersModel = documentSnapshot.toObject(Users.class);
            String profileImage = usersModel.getProfileImage();
            String uniID = usersModel.getUniID();

            Glide.with(context)
                    .load(profileImage)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.profileImage);

            holder.nameTv.setText(uniID);


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

    static class CommentHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView nameTv, commentTv , timeTv;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage_comment_items);
            nameTv = itemView.findViewById(R.id.nameTV_comment_items);
            commentTv = itemView.findViewById(R.id.commentTV_comment_items);
            timeTv = itemView.findViewById(R.id.timeTV_comment_items);

        }
    }
}
