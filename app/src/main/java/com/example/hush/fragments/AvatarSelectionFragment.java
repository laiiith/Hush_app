package com.example.hush.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hush.FragmentReplacerActivity;
import com.example.hush.MainActivity;
import com.example.hush.R;
import com.example.hush.adapter.AvatarSelectionAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AvatarSelectionFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<String> avatarUrls;
    private FirebaseUser user;


    public AvatarSelectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avatar_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avatarUrls = new ArrayList<>();
        fetchAvatarUrlsFromFirebase();
        init(view);

        AvatarSelectionAdapter adapter = new AvatarSelectionAdapter(avatarUrls, getActivity());
        recyclerView.setAdapter(adapter);

        adapter.OnPressed(selectedAvatarUrl -> {

            updateProfileImageInUser(selectedAvatarUrl);
        });
    }

    private void updateProfileImageInUser(String selectedAvatarUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(selectedAvatarUrl))
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateUserData(selectedAvatarUrl);
                        } else {
                            Toast.makeText(getContext(), "Error updating profile image: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUserData(String selectedAvatarUrl) {
        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid());

        Map<String, Object> map = new HashMap<>();
        map.put("profileImage", selectedAvatarUrl);

        reference.update(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                assert getActivity() != null;

                startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                getActivity().finish();
            } else {
                Toast.makeText(getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView_avatar_selection);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    private void fetchAvatarUrlsFromFirebase() {

        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Avatars/");

        reference.listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    avatarUrls.add(uri.toString());
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
            }

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


}