package com.example.hush.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hush.R;
import com.example.hush.adapter.ChatAdapter;
import com.example.hush.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser user;
    private CircleImageView profileImage;
    private TextView name;
    private EditText chatET;
    private ImageView sendBtn;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatModel> list;
    private String chatID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();

        loadUserData();

        loadMessages();

        sendBtn.setOnClickListener(view -> {

            String message = chatET.getText().toString().trim();

            if (message.isEmpty()) {
                return;
            }

            CollectionReference reference = FirebaseFirestore.getInstance()
                    .collection("Messages");


            Map<String, Object> map = new HashMap<>();

            map.put("lastMessage", message);
            map.put("time", FieldValue.serverTimestamp());

            reference.document(chatID).update(map);

            String messageID = reference
                    .document(chatID)
                    .collection("Messages")
                    .document()
                    .getId();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", messageID);
            messageMap.put("message", message);
            messageMap.put("senderID", user.getUid());
            messageMap.put("time", FieldValue.serverTimestamp());

            reference.document(chatID).collection("Messages")
                    .document(messageID).set(messageMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            chatET.setText("");
                        } else {
                            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }

    void init() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        profileImage = findViewById(R.id.profileImage_chat_activity);
        name = findViewById(R.id.nameTV_chat_activity);
        chatET = findViewById(R.id.chatET_chat_activity);
        sendBtn = findViewById(R.id.sendBtn_chat_activity);
        recyclerView = findViewById(R.id.recyclerView_chat_activity);

        list = new ArrayList<>();
        adapter = new ChatAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    void loadUserData() {

        String oppositeUID = getIntent().getStringExtra("uid");

        FirebaseFirestore.getInstance().collection("Users")
                .document(oppositeUID).addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;
                    if (!value.exists())
                        return;

                    //

                    Glide.with(getApplicationContext()).load(value.getString("profileImage"))
                            .placeholder(R.drawable.ic_person)
                            .into(profileImage);

                    name.setText(value.getString("uniID"));


                });
    }

    void loadMessages() {

        chatID = getIntent().getStringExtra("id");


        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(chatID)
                .collection("Messages");

        reference.orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {


                    if (error != null) return;
                    if (value == null || value.isEmpty()) return;

                    list.clear();
                    for (QueryDocumentSnapshot snapshot : value) {
                        ChatModel model = snapshot.toObject(ChatModel.class);
                        list.add(model);
                    }
                    adapter.notifyDataSetChanged();
                });


    }

}