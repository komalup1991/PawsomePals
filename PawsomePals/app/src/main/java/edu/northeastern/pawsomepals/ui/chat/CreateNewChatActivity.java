package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import edu.northeastern.pawsomepals.R;

public class CreateNewChatActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView chatRecyclerview;
//    ChatUserRecyclerAdapter adapter;

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
//        Query query = ChatFirebaseUtil.allUserCollectionReference()
//                .whereGreaterThanOrEqualTo("username",searchTerm);
//
//        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
//                .setQuery(query,UserModel.class).build();
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