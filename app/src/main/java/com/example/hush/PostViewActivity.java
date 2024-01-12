package com.example.hush;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

public class PostViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        FirebaseStorage.getInstance().getReference().child(uri.getLastPathSegment())
                .getDownloadUrl()
                .addOnSuccessListener(uri1 -> {
                    ImageView imageView = findViewById(R.id.imageView_post_view);

                    Glide.with(PostViewActivity.this)
                            .load(uri1.toString())
                            .timeout(6500)
                            .into(imageView);
                });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(PostViewActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(PostViewActivity.this, FragmentReplacerActivity.class));

        }


    }
}