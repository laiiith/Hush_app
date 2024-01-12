package com.example.hush.Admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.model.Users;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapterAdmin extends RecyclerView.Adapter<UsersAdapterAdmin.UsersHolder> {
    private Context context;
    private List<Users> usersList;

    public UsersAdapterAdmin() {
    }

    public UsersAdapterAdmin(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_items_admin
                , parent
                , false);
        return new UsersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersHolder holder, int position) {

        Users model = usersList.get(position);

        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(position).getUid());

        holder.uniID.setText(model.getUniID());
        holder.email.setText("Email: " + model.getEmail());
        holder.faculty.setText("Faculty: " + model.getFaculty());

        reference.get().addOnSuccessListener(documentSnapshot -> {
            Users usersModel = documentSnapshot.toObject(Users.class);

            Glide.with(context.getApplicationContext())
                    .load(usersModel.getProfileImage())
                    .placeholder(R.drawable.ic_person)
                    .timeout(6500)
                    .into(holder.profileImage);

        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UsersHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView faculty, uniID, email;

        public UsersHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.circleImageView_users_items_admin);
            faculty = itemView.findViewById(R.id.facultyTV_users_items_admin);
            uniID = itemView.findViewById(R.id.uniIDTV_users_items_admin);
            email = itemView.findViewById(R.id.emailTV_users_items_admin);
        }
    }
}
