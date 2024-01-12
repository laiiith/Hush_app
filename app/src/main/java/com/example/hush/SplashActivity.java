package com.example.hush;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hush.Admin.AdminMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        new Handler().postDelayed(() -> {

            Log.e("context", "" + user);
            if (user == null) {

                startActivity(new Intent(SplashActivity.this, FragmentReplacerActivity.class));
                finish();

            } else {
                FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();
                                if (snapshot != null && snapshot.exists()) {
                                    boolean isAdmin = snapshot.getBoolean("isAdmin");
                                    if (isAdmin) {
                                        startActivity(new Intent(SplashActivity.this, AdminMainActivity.class));
                                    } else {
                                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                    }
                                } else {
                                    Toast.makeText(SplashActivity.this, "Error: Document doesn't exist", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SplashActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        });
            }
        }, 2000);
    }
}