package com.example.hush.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hush.Admin.AdminMainActivity;
import com.example.hush.FragmentReplacerActivity;
import com.example.hush.MainActivity;
import com.example.hush.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$";
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";
    private EditText emailEt, passwordEt;
    private TextView forgotPwdTv, signUpTv;
    private Button signInBtn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }

    private void clickListener() {

        signInBtn.setOnClickListener(v -> {

            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();

            if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                emailEt.setError("Please enter a valid email");
                return;
            }

            if (password.isEmpty() || !password.matches(PASSWORD_REGEX)) {
                passwordEt.setError("Please enter a valid password");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (!user.isEmailVerified()) {
                        Toast.makeText(getContext(), "Please verify your email", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        checkUserAdminStatus(user.getUid());
                    }
                } else {
                    String exception = task.getException().getMessage();
                    Toast.makeText(getContext(), "Error : " + exception, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        });

        signUpTv.setOnClickListener(v -> ((FragmentReplacerActivity) getActivity()).setFragment(new CreateAccountFragment()));

        forgotPwdTv.setOnClickListener(v -> ((FragmentReplacerActivity) getActivity()).setFragment(new ForgotPassword()));

    }

    private void checkUserAdminStatus(String uid) {

        FirebaseFirestore.getInstance().collection("Users").document(uid).get()

                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.exists()) {
                            boolean isAdmin = snapshot.getBoolean("isAdmin");
                            if (isAdmin) {
                                sendUserToAdminActivity();
                            } else {
                                sendUserToMainActivity();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();

                    }
                    progressBar.setVisibility(View.GONE);
                });

    }

    private void sendUserToAdminActivity() {
        if (getActivity() == null)
            return;
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), AdminMainActivity.class));
        getActivity().finish();
    }

    private void sendUserToMainActivity() {
        if (getActivity() == null)
            return;
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }


    private void init(View view) {
        emailEt = view.findViewById(R.id.emaiET);
        passwordEt = view.findViewById(R.id.passwordET);
        forgotPwdTv = view.findViewById(R.id.forgot_pwd_txt);
        signUpTv = view.findViewById(R.id.sign_upTv);
        signInBtn = view.findViewById(R.id.sign_in_btn);
        progressBar = view.findViewById(R.id.progressBar2);
        auth = FirebaseAuth.getInstance();

    }
}