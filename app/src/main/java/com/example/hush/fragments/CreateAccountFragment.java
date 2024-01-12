package com.example.hush.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hush.FragmentReplacerActivity;
import com.example.hush.MainActivity;
import com.example.hush.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountFragment extends Fragment {


    public static final String UNI_ID_REGEX = "\\d{7}";
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$";
    private EditText emailET, uniIDEt, passwordEt, passwordConfirmEt;
    private Button signupBtn;
    private TextView loginTv;
    private FirebaseAuth auth;
    private Spinner facultySpinner;
    private ProgressBar progressBar;

    public CreateAccountFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        clickListener();
    }

    private void clickListener() {
        loginTv.setOnClickListener(v -> ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment()));

        signupBtn.setOnClickListener(v -> {

            String email = emailET.getText().toString();
            String uniID = uniIDEt.getText().toString();
            String faculty = facultySpinner.getSelectedItem().toString();
            String password = passwordEt.getText().toString();
            String confirmPassword = passwordConfirmEt.getText().toString();

            if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                emailET.setError("Please enter a valid email address");
                return;
            }
            if (uniID.isEmpty() || !uniID.matches(UNI_ID_REGEX)) {
                uniIDEt.setError("Please enter a valid university id");
                return;
            }
            if (password.isEmpty() || !password.matches(PASSWORD_REGEX)) {
                passwordEt.setError("Password must contain at least 8 characters,\n" +
                        "including at least 1 uppercase letter,\n" +
                        "1 lowercase letter,\n" +
                        "1 digit, and 1 special character\n");
                return;
            }
            if (!password.equals(confirmPassword)) {
                passwordConfirmEt.setError("Password not match");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            createAccount(email, uniID, faculty, password);
        });
    }

    private void createAccount(final String email, final String uniID, final String faculty, final String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(uniID)
                                .build();

                        user.updateProfile(profileUpdates);

                        String defaultProfileImageUri = "https://cdn1.vectorstock.com/i/1000x1000/31/95/user-sign-icon-person-symbol-human-avatar-vector-12693195.jpg";
                        UserProfileChangeRequest profileImageUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(defaultProfileImageUri))
                                .build();

                        user.updateProfile(profileImageUpdates);

                        user.sendEmailVerification().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                if (isAdded()){

                                    Log.e("context", "" + getContext());
                                    Toast.makeText(getContext(), "Email verification link send", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("Error11:" , task1.getException().getMessage());
                            }
                        });
                        uploadUser(user, email, uniID, faculty);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        String exception = task.getException().getMessage();
                        Toast.makeText(getContext(), "Error: " + exception, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadUser(FirebaseUser user, String email, String uniID, String faculty) {
        List<String> listFollowers = new ArrayList<>();
        List<String> listFollowing = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();

        map.put("uniID", uniID);
        map.put("email", email);
        map.put("faculty", faculty);
        map.put("profileImage", "");
        map.put("uid", user.getUid());
        map.put("isAdmin", false);
        map.put("search", uniID);

        map.put("followers", listFollowers);
        map.put("following", listFollowing);

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        assert getActivity() != null;
                        progressBar.setVisibility(View.GONE);
//                        ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
                        startActivity(new Intent(getActivity() , MainActivity.class));
                        getActivity().finish();

                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void init(View view) {
        emailET = view.findViewById(R.id.email_edit_text);
        uniIDEt = view.findViewById(R.id.university_id_edit_text);
        passwordEt = view.findViewById(R.id.password_edit_text);
        passwordConfirmEt = view.findViewById(R.id.confirm_password_edit_text);
        signupBtn = view.findViewById(R.id.sign_up_btn);
        loginTv = view.findViewById(R.id.loginTv);
        auth = FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        facultySpinner = view.findViewById(R.id.facultySpinner);
        facultySpinner.setSelection(0);
    }
}