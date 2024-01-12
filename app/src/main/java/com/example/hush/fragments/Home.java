package com.example.hush.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hush.R;
import com.example.hush.adapter.HomeAdapter;
import com.example.hush.chat.ChatUsersActivity;
import com.example.hush.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Home extends Fragment {
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;
    private ImageButton directBtn;
    private FirebaseAuth auth;

    public Home() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);
        loadDataFromFirestore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likesList, boolean isChecked) {
                DocumentReference reference = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if (likesList.contains(user.getUid()) && !isChecked) {
                    likesList.remove(user.getUid());
                } else {
                    likesList.add(user.getUid());
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likesList);

                reference.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Liked", "Likes updated successfully");
                    } else {
                        Log.e("Liked", "Failed to update likes: " + task.getException());
                    }
                });
            }

            @Override
            public void onDataChanged() {
                adapter.notifyDataSetChanged();
            }
        });

        clickListener();
    }

    private void clickListener() {

        directBtn.setOnClickListener(view -> {
             Intent intent = new Intent(getContext() , ChatUsersActivity.class);
            startActivity(intent);
        });

    }

    private void loadDataFromFirestore() {

        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        CollectionReference collectionReference = FirebaseFirestore.getInstance().
                collection("Users");


        reference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }
            if (value == null)
                return;

            List<String> uidList = (List<String>) value.get("following");



            if (uidList == null || uidList.isEmpty()) {
                return;
            }

            collectionReference.whereIn("uid", uidList).addSnapshotListener((value1, error1) -> {

                if (error1 != null) {
                    Log.d("Error: ", error1.getMessage());
                    return;
                }

                if (value1 == null)
                    return;

                for (QueryDocumentSnapshot snapshot : value1) {
                    snapshot.getReference().collection("Post Images").orderBy("timeStamp", Query.Direction.DESCENDING).whereEqualTo("isApproved", true).addSnapshotListener((value11, error11) -> {

                        if (error11 != null) {
                            Log.d("Error: ", error11.getMessage());
                            return;
                        }

                        if (value11 == null)
                            return;

                        list.clear();

                        for (QueryDocumentSnapshot snapshot1 : value11) {

                            if (!snapshot1.exists())
                                return;

                            HomeModel model = snapshot1.toObject(HomeModel.class);
                            list.add(new HomeModel(
                                    model.getImageUrl(),
                                    model.getUid(),
                                    model.getDescription(),
                                    model.getId(),
                                    model.isApproved(),
                                    model.getTimeStamp(),
                                    model.getLikes()
                            ));
                        }


                        adapter.notifyDataSetChanged();
                    });
                }


            });

        });

    }


    private void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        auth = FirebaseAuth.getInstance();
        directBtn = view.findViewById(R.id.directBtn);
        user = auth.getCurrentUser();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

    }
}