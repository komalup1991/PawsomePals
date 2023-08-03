package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatUserRecyclerAdapter;
import edu.northeastern.pawsomepals.models.Users;


public class CreateNewChatActivity extends AppCompatActivity {
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton backButton;
    private RecyclerView chatRecyclerview;
    private ChatUserRecyclerAdapter adapter;

    private List<Users> userList, contactUsersList;
    private Button createNewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        userList = new ArrayList<>();
        searchInput = findViewById(R.id.chat_search_username);
        searchButton = findViewById(R.id.chat_search_user_btn);
        backButton = findViewById(R.id.chat_user_back_button);
        chatRecyclerview = findViewById(R.id.chat_search_user_recyclerView);
        createNewChat = findViewById(R.id.finish_select_button);

        searchInput.requestFocus();

        backButton.setOnClickListener(v -> onBackPressed());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setupSearchRecyclerView(searchInput.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();

            if (searchTerm.isEmpty() || searchTerm.length() < 2) {
                searchInput.setError("Invalid Username");
                return;
            }

            setupSearchRecyclerView(searchTerm);
        });

        createNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userList.size() >= 0) {
                    if (userList.size() == 1) {
                        //navigate to chat activity;
                        Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                        ChatFirebaseUtil.passUserModelAsIntent(intent, userList.get(0));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    } else {
                        createGroupChat();
                    }
                }
            }
        });
    }

    private void createGroupChat() {
    }

    private void setupSearchRecyclerView(String searchTerm) {
        Query query = ChatFirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("name", searchTerm);

        FirestoreRecyclerOptions<Users> options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class).build();
        adapter = new ChatUserRecyclerAdapter(options, getApplicationContext());
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
            adapter.startListening();
        }
//        synchronized(this) {
//            // update your adapter data here
//            adapter.notifyDataSetChanged();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}