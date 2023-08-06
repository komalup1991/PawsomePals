package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecentChatRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatRoomModel;

public class ChatFragment extends Fragment {
    private TextView searchInput;
    private ImageButton searchButton;
    private Button createNewGroupButton;
    private RecyclerView chatRecyclerview;
    private RecentChatRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.chat_search_chat);
        searchButton = view.findViewById(R.id.chat_search_chat_btn);
//        createNewChatButton = view.findViewById(R.id.new_chat_btn);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);
        createNewGroupButton = view.findViewById(R.id.new_group_chat_btn);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchInput.getText().toString();

                if (searchTerm.isEmpty() || searchTerm.length() < 3) {
                    searchInput.setError("Invalid Username");
                    return;
                }

                setupRecyclerView();
            }
        });
        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatSearchChatroomActivity.class);
                startActivity(intent);
            }
        });
        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateNewGroupChat.class);
                startActivity(intent);
            }
        });

        getFCMToken();
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    ChatFirebaseUtil.currentUserDetails().update("fcmToken", token);
                    setupRecyclerView();
                }
            }
        });
    }

    private void setupRecyclerView() {
        Query query = ChatFirebaseUtil.allChatRoomCollectionReference()
                .whereArrayContains("userIds", ChatFirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatRoomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                        .setQuery(query, ChatRoomModel.class)
                        .build();
        adapter = new RecentChatRecyclerAdapter(options, getContext());
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
