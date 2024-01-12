package com.example.hush.fragments;


import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.adapter.GalleryAdapter;
import com.example.hush.model.GalleryImages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add extends Fragment {
    private EditText descET;
    private Dialog dialog;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ImageButton nextBtn;
    private List<GalleryImages> list;
    private FirebaseUser user;
    private Uri imageUri;
    private GalleryAdapter adapter;

    public Add() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);
        recyclerView.setAdapter(adapter);
        clickListener();
    }

    private void clickListener() {

        adapter.sendImage(picUri -> {

            Glide.with(getContext())
                    .load(picUri).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.VISIBLE);

            CropImage.activity(picUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4, 3)
                    .start(getContext(), Add.this);

        });

        nextBtn.setOnClickListener(view -> {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());

            dialog.show();
            storageReference.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> uploadData(uri.toString()));
                        } else {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed To Upload Post", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void uploadData(String imageURL) {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).collection("Post Images");

        String id = reference.document().getId();
        String description = descET.getText().toString();

        List<String> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();

        map.put("description", description);
        map.put("id", id);
        map.put("imageUrl", imageURL);
        map.put("timeStamp", FieldValue.serverTimestamp());
        map.put("likes", list);
        map.put("isApproved", false);
        map.put("uid", user.getUid());


        reference.document(id).set(map).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                System.out.println();
                Toast.makeText(getContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error: " + task1.getException().getMessage()
                        , Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });


    }

    private void init(View view) {
        descET = view.findViewById(R.id.descriptionET_add);
        imageView = view.findViewById(R.id.imageView_add);
        recyclerView = view.findViewById(R.id.recyclerView_add);
        user = FirebaseAuth.getInstance().getCurrentUser();
        nextBtn = view.findViewById(R.id.nextBtn_add);
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.dialog_bg, null));
        dialog.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().runOnUiThread(() -> Dexter.withContext(getContext())
                .withPermissions(Manifest.permission.READ_MEDIA_IMAGES).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {
                            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
                            if (file.exists()) {
                                File[] files = file.listFiles();
                                assert files != null;

                                list.clear();

                                for (File file1 : files) {
                                    if (file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png")) {
                                        list.add(new GalleryImages(Uri.fromFile(file1)));
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();

                Glide.with(getContext())
                        .load(imageUri)
                        .into(imageView);

                imageView.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);
            }
        }
    }
}