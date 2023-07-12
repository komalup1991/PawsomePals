package edu.northeastern.pawsomepals.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.units.qual.C;

import edu.northeastern.pawsomepals.R;
//import edu.northeastern.pawsomepals.adapters.ChatUserRecyclerAdapter;

public class ChatFragment extends Fragment {
    EditText searchInput;
    ImageButton searchButton;
    RecyclerView chatRecyclerview;
//    ChatUserRecyclerAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.search_username_input);
        searchButton = view.findViewById(R.id.search_user_btn);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);

        searchInput.requestFocus();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchInput.getText().toString();

                if (searchTerm.isEmpty() || searchTerm.length() < 3){
                    searchInput.setError("Invalid Username");
                    return;
                }
                
                setupSearchRecyclerView(searchTerm);
            }
        });
    }

    private void setupSearchRecyclerView(String searchTerm) {
//        adapter = new ChatUserRecyclerAdapter(,this.getContext());
//        chatRecyclerview.setLayoutManager(new LinearLayoutManager(this.getContext()));
//        chatRecyclerview.setAdapter(adapter);
//        adapter.startListening();
    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (adapter != null){
//            adapter.startListening();
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (adapter != null){
//            adapter.startListening();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (adapter != null){
//            adapter.stopListening();
//        }
//    }
}
