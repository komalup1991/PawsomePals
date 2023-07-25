package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;


import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatUserRecyclerAdapter;
import edu.northeastern.pawsomepals.models.Users;


public class CreateNewChatActivity extends AppCompatActivity {
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton backButton;
    private RecyclerView chatRecyclerview;
    private ChatUserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        searchInput = findViewById(R.id.chat_search_username);
        searchButton = findViewById(R.id.chat_search_user_btn);
        backButton = findViewById(R.id.chat_user_back_button);
        chatRecyclerview = findViewById(R.id.chat_search_user_recyclerView);

        searchInput.requestFocus();

        backButton.setOnClickListener(v -> onBackPressed());

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();

            if (searchTerm.isEmpty() || searchTerm.length() < 3){
                searchInput.setError("Invalid Username");
                return;
            }

            setupSearchRecyclerView(searchTerm);
        });
    }

    private void setupSearchRecyclerView(String searchTerm) {
        Query query = ChatFirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("name",searchTerm);

        FirestoreRecyclerOptions<Users> options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query,Users.class).build();
        adapter = new ChatUserRecyclerAdapter(options,getApplicationContext());
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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