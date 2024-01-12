package com.example.hush.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hush.R;
import com.example.hush.adapter.UserAdapter;
import com.example.hush.model.Users;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class Search extends Fragment {

    OnDataPass onDataPass;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private CollectionReference reference;
    private UserAdapter adapter;
    private List<Users> list;

    public Search() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        onDataPass = (OnDataPass) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        reference = FirebaseFirestore.getInstance().collection("Users");

        loadUserData();

        searchUser();
        clickListener();
    }

    private void clickListener() {
        adapter.OnUserClicked(uid -> onDataPass.onChange(uid));
    }

    private void searchUser() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                reference.whereEqualTo("isAdmin", false).orderBy("search").startAt(query).endAt(query + "\uf8ff")
                        .get().addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                list.clear();
                                for (DocumentSnapshot snapshot : task.getResult()) {
                                    if (snapshot.exists()) {
                                        Users users = snapshot.toObject(Users.class);
                                        list.add(users);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                // Handle the error, e.g., log or show a message
                                Log.e("SearchUser", "Error getting documents: ", task.getException());
                            }
                        });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(""))
                    loadUserData();
                return false;
            }
        });
    }

    private void loadUserData() {

        reference.whereEqualTo("isAdmin", false).addSnapshotListener((value, error) -> {

            if (value == null)
                return;

            if (error != null) {
                Log.e("LoadUserData", "Error getting documents: ", error);
                return;
            }

            list.clear();

            for (QueryDocumentSnapshot snapshot : value) {
                Users users = snapshot.toObject(Users.class);
                list.add(users);

            }
            adapter.notifyDataSetChanged();
        });
    }

    private void init(View view) {
        searchView = view.findViewById(R.id.searchView_search);
        recyclerView = view.findViewById(R.id.recyclerView_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();
        adapter = new UserAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    public interface OnDataPass {
        void onChange(String uid);
    }

}