package com.example.hush;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.hush.fragments.AvatarSelectionFragment;
import com.example.hush.fragments.Comment;
import com.example.hush.fragments.CreateAccountFragment;
import com.example.hush.fragments.LoginFragment;

public class FragmentReplacerActivity extends AppCompatActivity {
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_replacer);


        frameLayout = findViewById(R.id.frameLayout);

        boolean isComment = getIntent().getBooleanExtra("isComment", false);
        boolean isAvatar = getIntent().getBooleanExtra("isAvatar", false);

        if (isComment)
            setFragment(new Comment());
        else if (isAvatar) {
            setFragment(new AvatarSelectionFragment());
        } else {
            setFragment(new LoginFragment());
        }
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment instanceof CreateAccountFragment) {
            fragmentTransaction.addToBackStack(null);
        }

        if (fragment instanceof Comment) {
            String id = getIntent().getStringExtra("id");
            String uid = getIntent().getStringExtra("uid");
            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("uid", uid);
            fragment.setArguments(bundle);

        }
        if (fragment instanceof AvatarSelectionFragment) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}