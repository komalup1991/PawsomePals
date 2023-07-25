package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatUserRecyclerAdapter;
import edu.northeastern.pawsomepals.adapters.RecentChatRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.Users;


public class ChatFragment extends Fragment {
    private EditText searchInput;
    private ImageButton searchButton;
    private Button createNewChatButton;
    private RecyclerView chatRecyclerview;
    private RecentChatRecyclerAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.chat_search_chat);
        searchButton = view.findViewById(R.id.chat_search_chat_btn);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);
        createNewChatButton = view.findViewById(R.id.new_chat_btn);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);
        setupRecyclerView();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchInput.getText().toString();

                if (searchTerm.isEmpty() || searchTerm.length() < 3){
                    searchInput.setError("Invalid Username");
                    return;
                }
                
                setupRecyclerView();
            }
        });

        createNewChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateNewChatActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        Query query = ChatFirebaseUtil.allChatRoomCollectionReference()
                .whereArrayContains("userIds",ChatFirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatRoomModel> options = new FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                .setQuery(query,ChatRoomModel.class).build();
        adapter = new RecentChatRecyclerAdapter(options,getContext());
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }
}
