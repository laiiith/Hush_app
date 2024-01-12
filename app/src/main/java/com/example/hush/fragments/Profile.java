package com.example.hush.fragments;

import static com.example.hush.MainActivity.IS_SEARCHED_USER;
import static com.example.hush.MainActivity.USER_ID;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.FragmentReplacerActivity;
import com.example.hush.R;
import com.example.hush.SplashActivity;
import com.example.hush.chat.ChatActivity;
import com.example.hush.model.PostModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {
    boolean isFollowed;
    int count;
    private String userUID;
    private FirestoreRecyclerOptions<PostModel> options;
    private FirestoreRecyclerAdapter<PostModel, PostHolder> adapter;
    private TextView nameTv, toolbarNameTv, facultyTv, postCountTv, followingCountTv, followerCountTv;
    private CircleImageView profileImage;
    private RelativeLayout countLayout;
    private RecyclerView recyclerView;
    private ImageButton signOutBtn, editProfileBtn;
    private Toolbar toolbar;
    private Button followBtn, startChatBtn;
    private FirebaseAuth auth;
    private boolean isMyProfile = true;
    private List<Object> followingList, followersList, followingList_2;
    private FirebaseUser user;
    private DocumentReference userRef, myRef;


    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);


        myRef = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        if (IS_SEARCHED_USER) {
            isMyProfile = false;
            userUID = USER_ID;

            loadData();

        } else {
            isMyProfile = true;
            userUID = user.getUid();
        }

        if (isMyProfile) {
            editProfileBtn.setVisibility(View.VISIBLE);
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
            startChatBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);
        } else {
            editProfileBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.VISIBLE);
            countLayout.setVisibility(View.VISIBLE);

        }

        userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(userUID);

        loadBasicData();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        loadPosts();

        recyclerView.setAdapter(adapter);

        clickListener();
    }

    private void loadData() {
        myRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Tag_b", error.getMessage());
                return;
            }
            if (value == null || !value.exists()) {
                return;
            }
            followingList_2 = (List<Object>) value.get("following");
        });
    }

    private void clickListener() {

        followBtn.setOnClickListener(view -> {

            if (isFollowed) {

                followersList.remove(user.getUid()); //Opposite User

                followingList_2.remove(userUID);//user

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);

                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText("Follow");

                        myRef.update(map_2).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getContext(), "UnFollowed", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Tag_3", task1.getException().getMessage());
                            }
                        });

                    } else {
                        Log.e("Tag", "" + task.getException().getMessage());
                    }
                });

            } else {

                createNotification();

                followersList.add(user.getUid()); //Opposite User

                followingList_2.add(userUID);//user

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);

                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText("UnFollow");

                        myRef.update(map_2).addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                Toast.makeText(getContext(), "Followed", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("tag_3_1", task12.getException().getMessage());
                            }
                        });


                    } else {
                        Log.e("Tag", "" + task.getException().getMessage());
                    }
                });

            }

        });

        editProfileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FragmentReplacerActivity.class);
            intent.putExtra("isAvatar", true);

            startActivity(intent);

        });

        signOutBtn.setOnClickListener(view -> {
            signOutUser();
        });

        startChatBtn.setOnClickListener(view -> {
            queryChat();
        });

    }

    void queryChat() {

        StylishAlertDialog alertDialog = new StylishAlertDialog(getContext(), StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Starting Chat...");
        alertDialog.setCancelable(false);
        alertDialog.show();


        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages");

        reference.whereArrayContains("uid", userUID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot.isEmpty()) {
                            startChat(alertDialog);
                        } else {
                            alertDialog.dismissWithAnimation();
                            for (DocumentSnapshot snapshotChat : snapshot) {
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("uid", userUID);
                                intent.putExtra("id", snapshotChat.getId());
                                startActivity(intent);
                            }

                        }


                    } else {
                        alertDialog.dismissWithAnimation();
                    }
                });

    }

    private void startChat(StylishAlertDialog alertDialog) {

        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages");

        List<String> list = new ArrayList<>();

        list.add(0, user.getUid());
        list.add(1, userUID);

        String pushID = reference.document().getId();

        Map<String, Object> map = new HashMap<>();

        map.put("id", pushID);
        map.put("lastMessage", "Hi");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid", list);

        reference.document(pushID).update(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

            } else {
                reference.document(pushID).set(map);
            }
        });


        CollectionReference messageRef = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(pushID)
                .collection("Messages");


        String messageID = messageRef.document().getId();

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("id", messageID);
        messageMap.put("message", "HI");
        messageMap.put("senderID", user.getUid());
        messageMap.put("time", FieldValue.serverTimestamp());

        messageRef.document(messageID).set(messageMap);

        new Handler().postDelayed(() -> {

            alertDialog.dismissWithAnimation();

            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("uid", userUID);
            intent.putExtra("id", pushID);
            startActivity(intent);
        }, 3000);
    }

    private void signOutUser() {

        auth.signOut();

        Intent intent = new Intent(getContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void loadBasicData() {

        userRef.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.e("Tag 0", error.getMessage());
                return;
            }

            assert value != null;

            if (value.exists()) {

                String name = value.getString("uniID");
                String faculty = value.getString("faculty");
                String profileURL = value.getString("profileImage");

                nameTv.setText(name);
                toolbarNameTv.setText(name);
                facultyTv.setText(faculty);

                followersList = (List<Object>) value.get("followers");
                followingList = (List<Object>) value.get("following");


                followerCountTv.setText("" + followersList.size());
                followingCountTv.setText("" + followingList.size());

                try {
                    Glide.with(getContext().getApplicationContext())
                            .load(profileURL)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .timeout(6500)
                            .into(profileImage);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (followersList.contains(user.getUid())) {
                    followBtn.setText("UnFollow");
                    isFollowed = true;
                    startChatBtn.setVisibility(View.VISIBLE);
                } else {
                    followBtn.setText("Follow");
                    isFollowed = false;
                    startChatBtn.setVisibility(View.GONE);
                }

            }

        });

    }


    private void loadPosts() {


        DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("Users").document(userUID);

        Query query = reference.collection("Post Images").whereEqualTo("isApproved", true);

        options = new FirestoreRecyclerOptions.Builder<PostModel>()
                .setQuery(query, PostModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PostModel, PostHolder>(options) {
            @NonNull
            @Override
            public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_posts_items
                        , parent, false);
                return new PostHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostHolder holder, int position, @NonNull PostModel model) {
                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText("" + count);
            }

            @Override
            public int getItemCount() {

                return super.getItemCount();
            }
        };
    }

    private void init(View view) {
        toolbar = view.findViewById(R.id.toolbar_profile);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        nameTv = view.findViewById(R.id.nameTv_profile);
        toolbarNameTv = view.findViewById(R.id.toolbarNameTv_profile);
        facultyTv = view.findViewById(R.id.facultyTv_profile);
        postCountTv = view.findViewById(R.id.noOfPostTv_profile);
        profileImage = view.findViewById(R.id.profileImage_profile);
        startChatBtn = view.findViewById(R.id.startChatBtn_profile);
        recyclerView = view.findViewById(R.id.recyclerView_profile);
        signOutBtn = view.findViewById(R.id.signoutBtn_profile);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        editProfileBtn = view.findViewById(R.id.edit_image_profile);
        countLayout = view.findViewById(R.id.countLayout_profile);
        followBtn = view.findViewById(R.id.followBtn_profile);
        followingCountTv = view.findViewById(R.id.noOfFollowingTv_profile);
        followerCountTv = view.findViewById(R.id.noOfFollowerTv_profile);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }

    void createNotification() {
        CollectionReference reference =
                FirebaseFirestore.getInstance().collection("Notifications");

        String id = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("time", FieldValue.serverTimestamp());
        map.put("notification", user.getDisplayName() + " Followed You");
        map.put("id", id);
        map.put("uid", userUID);

        reference.document(id).set(map);
    }

    private class PostHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_profile_posts_image);
        }
    }


}