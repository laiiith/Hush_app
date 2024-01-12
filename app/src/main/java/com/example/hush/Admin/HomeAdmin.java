package com.example.hush.Admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hush.R;
import com.example.hush.model.HomeModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeAdmin extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapterAdmin adapter;
    private List<HomeModel> list;
    private DocumentReference postRef;

    public HomeAdmin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);


        list = new ArrayList<>();
        adapter = new PostAdapterAdmin(getContext(), list);
        recyclerView.setAdapter(adapter);

        loadPostImagesForUser();

        adapter.OnPressed(new PostAdapterAdmin.OnPressed() {

            @Override
            public void onApprove(HomeModel model) {
                postRef = FirebaseFirestore.getInstance().collection("Users").document(model.getUid())
                        .collection("Post Images")
                        .document(model.getId());

                postRef.update("isApproved", true).addOnSuccessListener(unused -> {
                    list.clear();
                    loadPostImagesForUser();
                    Toast.makeText(getContext(), "Post updated successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onReject(HomeModel model) {
                postRef = FirebaseFirestore.getInstance().collection("Users").document(model.getUid())
                        .collection("Post Images")
                        .document(model.getId());

                postRef.delete().addOnSuccessListener(unused -> {
                    list.clear();
                    loadPostImagesForUser();
                    Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

    private void loadPostImagesForUser() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users");


        reference.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }
            if (value == null)
                return;

            list.clear();

            for (QueryDocumentSnapshot userSnapshot : value) {
                if (!userSnapshot.exists())
                    return;

                userSnapshot.getReference().collection("Post Images").whereEqualTo("isApproved", false).addSnapshotListener((value1, error1) -> {
                    if (error1 != null) {
                        Log.d("Error: ", error1.getMessage());
                        return;
                    }
                    if (value1 == null)
                        return;

                    for (QueryDocumentSnapshot postSnapshot : value1) {
                        if (!postSnapshot.exists())
                            return;

                        HomeModel model = postSnapshot.toObject(HomeModel.class);

                        list.add(model);

                    }

                    adapter.notifyDataSetChanged();
                });
            }
        });

    }


    private void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView_admin);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Toolbar toolbar = view.findViewById(R.id.toolbar_admin);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }


}