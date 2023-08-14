package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecentChatRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatRoomModel;

public class ChatSearchChatroomActivity extends AppCompatActivity {
    private EditText searchInput;
//    private ImageButton searchButton;
    private RecyclerView chatRecyclerview;
    private RecentChatRecyclerAdapter adapter;
    private boolean buttonClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_search_chatroom);
        searchInput = findViewById(R.id.chat_search_chat);
//        searchButton = findViewById(R.id.chat_search_chat_btn);
        chatRecyclerview = findViewById(R.id.chat_search_user_recyclerView);
        buttonClicked = false;
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setupRecyclerView(searchInput.getText().toString(),false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setupRecyclerView(searchInput.getText().toString(),true);
//            }
//        });
    }

    private void setupRecyclerView(String searchTerm,boolean buttonClicked) {
        Query query = ChatFirebaseUtil.allChatRoomCollectionReference()
                .whereArrayContains("userIds", ChatFirebaseUtil.currentUserId())
                .whereArrayContains("userNames",searchTerm.toLowerCase());

        FirestoreRecyclerOptions<ChatRoomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                        .setQuery(query, ChatRoomModel.class)
                        .build();
        if (buttonClicked && options.getSnapshots().isEmpty()){
            Toast.makeText(getApplicationContext(),"You still have no chats with this user.",Toast.LENGTH_SHORT).show();
        }
        adapter = new RecentChatRecyclerAdapter(options, this.getApplicationContext(), new ChatFragment.ProfilePicClickListener() {
            @Override
            public void onItemClicked(String userId) {
            }
        }, new RecentChatRecyclerAdapter.DataUpdateListener() {
            @Override
            public void onDataUpdated() {

            }
        }
        );
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        chatRecyclerview.setAdapter(adapter);
        adapter.startListening();
    }
}