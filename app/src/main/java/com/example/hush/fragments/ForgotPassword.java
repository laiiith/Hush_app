package com.example.hush.fragments;

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

import com.example.hush.FragmentReplacerActivity;
import com.example.hush.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends Fragment {

    public static final String EMAIL_REGEX = "^(.+)@(.+)$";
    private EditText emailEt;
    private TextView loginTv;
    private Button recoverBtn;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    public ForgotPassword() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }

    private void clickListener() {

        loginTv.setOnClickListener(v -> ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment()));

        recoverBtn.setOnClickListener(v -> {

            String email = emailEt.getText().toString();
            if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                emailEt.setError("Enter valid email");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Reset password email sent successfully"
                            , Toast.LENGTH_SHORT).show();
                    emailEt.setText("");
                } else {
                    String errMsg = task.getException().getMessage();
                    Toast.makeText(getContext(), "Error: " + errMsg, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    private void init(View view) {
        emailEt = view.findViewById(R.id.emailET_forogt);
        loginTv = view.findViewById(R.id.loginTv_forogt);
        recoverBtn = view.findViewById(R.id.recoverBtn_forogt);
        auth = FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.progressBar_forogt);
    }
}