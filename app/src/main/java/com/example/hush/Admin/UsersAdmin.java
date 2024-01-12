package com.example.hush.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hush.R;
import com.example.hush.SplashActivity;
import com.example.hush.model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersAdmin extends Fragment {
    private RecyclerView recyclerView;
    private ImageButton signOut;
    private FirebaseAuth auth;
    private List<Users> usersList;
    private UsersAdapterAdmin adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);


        clickListener();

        usersList = new ArrayList<>();
        adapter = new UsersAdapterAdmin(getContext(), usersList);
        recyclerView.setAdapter(adapter);

        loadUsersFromFireStore();
        enableSwipeToDelete();

    }

    private void clickListener() {

        signOut.setOnClickListener(view -> {

            signOutUser();
        });

    }

    private void signOutUser() {

        auth.signOut();
        Intent intent = new Intent(getContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void loadUsersFromFireStore() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users");

        reference.whereEqualTo("isAdmin", false).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }

            if (value == null)
                return;

            usersList.clear();
            for (QueryDocumentSnapshot snapshot : value) {
                if (!snapshot.exists())
                    return;

                Users model = snapshot.toObject(Users.class);
                usersList.add(model);
            }
            adapter.notifyDataSetChanged();

        });


    }

    private void enableSwipeToDelete() {

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                deleteDocument(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void deleteDocument(int position) {
        String documentId = usersList.get(position).getUid();

        CollectionReference postReference = FirebaseFirestore.getInstance()
                .collection("Users").document(documentId).collection("Post Images");

        DocumentReference userReference = FirebaseFirestore.getInstance()
                .collection("Users").document(documentId);

        postReference.get().addOnSuccessListener(queryDocumentSnapshots -> {

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                if (snapshot.exists()) {
                    String postId = snapshot.getId();
                    deleteCommentsAndPosts(documentId, postId);
                }
            }
            userReference.delete().addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e("delete" , e.getMessage());
            });

        }).addOnFailureListener(e -> {
            Log.e("Error:", e.getMessage());
        });

        }

    private void deleteCommentsAndPosts(String documentId, String postId) {

        deleteComments(documentId, postId);

        deletePost(documentId);
    }

    private void deletePost(String documentId) {

        CollectionReference postReference = FirebaseFirestore.getInstance()
                .collection("Users").document(documentId)
                .collection("Post Images");

        postReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean postsDeleted = true;

                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    if (snapshot.exists()) {
                        snapshot.getReference().delete();
                    } else {
                        Log.e("posts", "Post does not exist: " + snapshot.getId());
                        postsDeleted = false;
                    }
                }
                if (postsDeleted) {
                    Log.d("posts", "All posts have been deleted");
                }
            } else {
                Log.e("Error:", task.getException().getMessage());
            }

        });
    }

    private void deleteComments(String documentId, String postId) {

        CollectionReference commentsReference = FirebaseFirestore.getInstance()
                .collection("Users").document(documentId)
                .collection("Post Images").document(postId)
                .collection("Comments");


        commentsReference.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                boolean commentsDeleted = true;
                for (QueryDocumentSnapshot commentSnapshot : task.getResult()) {
                    if (commentSnapshot.exists()) {
                        commentSnapshot.getReference().delete();
                    } else {
                        Log.e("comments", "Comment does not exist: " + commentSnapshot.getId());
                        commentsDeleted = false;
                    }
                }
                if (commentsDeleted) {
                    Log.d("comments", "All comments have been deleted");
                }
            } else {
                Log.e("Error:", task.getException().getMessage());
            }
        });
    }

    private void init(View view) {
        signOut = view.findViewById(R.id.signoutBtn_users_admin);
        recyclerView = view.findViewById(R.id.usersList_users_admin);
        recyclerView.setHasFixedSize(true);
        auth = FirebaseAuth.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Toolbar toolbar = view.findViewById(R.id.toolbar_users_admin);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

    }
}