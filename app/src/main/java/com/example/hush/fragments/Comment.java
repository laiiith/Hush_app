package com.example.hush.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hush.R;
import com.example.hush.adapter.CommentAdapter;
import com.example.hush.model.CommentModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Comment extends Fragment {

    List<CommentModel> list;
    EditText commentEt;
    ImageButton sendBtn;
    RecyclerView recyclerView;
    CommentAdapter adapter;
    FirebaseUser user;
    String id, uid;
    CollectionReference reference;


    public Comment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Post Images")
                .document(id)
                .collection("Comments");

        loadCommentData();

        clickListener();

    }


    private void clickListener() {

        sendBtn.setOnClickListener(view -> {

            String comment = commentEt.getText().toString();

            if (comment.isEmpty()) {
                Toast.makeText(getContext(), "Enter Comment", Toast.LENGTH_SHORT).show();
                return;
            }

            String commentID = reference.document().getId();

            Map<String, Object> map = new HashMap<>();

            map.put("uid", user.getUid());
            map.put("comment", comment);
            map.put("commentID", commentID);
            map.put("postID", id);
            map.put("timeStamp", FieldValue.serverTimestamp());


            reference.document(commentID).set(map).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    commentEt.setText("");

                } else {
                    Log.e("comment" , task.getException().getMessage());

                }
            });

        });
    }

    private void loadCommentData() {

        reference.orderBy("timeStamp", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {

            if (error != null)
                return;

            if (value == null) {
                Toast.makeText(getContext(), "No Comments", Toast.LENGTH_SHORT).show();
                return;
            }

            list.clear();

            for (DocumentSnapshot snapshot : value) {

                CommentModel model = snapshot.toObject(CommentModel.class);
                list.add(model);
            }
            adapter.notifyDataSetChanged();

        });

    }

    private void init(View view) {

        commentEt = view.findViewById(R.id.commentET_comment);
        sendBtn = view.findViewById(R.id.sendBtn_comment);
        recyclerView = view.findViewById(R.id.commentRecylerView_comment);

        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new CommentAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);

        if (getArguments() == null)
            return;

        id = getArguments().getString("id");
        uid = getArguments().getString("uid");

    }
}