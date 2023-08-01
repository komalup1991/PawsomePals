package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatGroupUserRecyclerAdapter;
import edu.northeastern.pawsomepals.adapters.ChatUserRecyclerAdapter;
import edu.northeastern.pawsomepals.models.Users;

public class CreateNewGroupChat extends AppCompatActivity {
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton backButton;
    private RecyclerView usersRecyclerview;
    private ChatGroupUserRecyclerAdapter adapter;
    FirestoreRecyclerOptions<Users> options;

    private List<Users> userList;
    private Button createNewChat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        userList = new ArrayList<>();
        searchInput = findViewById(R.id.chat_search_username);
        searchButton = findViewById(R.id.chat_search_user_btn);
        backButton = findViewById(R.id.chat_user_back_button);
        usersRecyclerview = findViewById(R.id.chat_search_user_recyclerView);
        createNewChat = findViewById(R.id.finish_select_button);
        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Users currentUser = task.getResult().toObject(Users.class);
                userList.add(currentUser);
            }
        });

        backButton.setOnClickListener(v -> onBackPressed());

        setupUsersRecyclerView();
        createNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupChat();
            }
        });
    }

    private void setupUsersRecyclerView() {
        Query query = ChatFirebaseUtil.allUserCollectionReference().orderBy("name");

        options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class).build();
        adapter = new ChatGroupUserRecyclerAdapter(options, getApplicationContext());
        usersRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        usersRecyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    private void createGroupChat() {

        for (Users model : options.getSnapshots()) {
            if (model.isChatSelected()) {
                userList.add(model);
            }
        }

        if (userList.size() > 1) {
            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            ChatFirebaseUtil.passGroupChatModelAsIntent(intent, userList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }
    }
}
