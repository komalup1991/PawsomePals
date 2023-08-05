package edu.northeastern.pawsomepals.ui.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.index.qual.LengthOf;

import java.util.ArrayList;
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
    private FirestoreRecyclerOptions<Users> options;

    private List<Users> userList;
    private Users currentUser;
    private Button createNewChat;
    private String groupName;

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

        backButton.setOnClickListener(v -> onBackPressed());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setupUsersRecyclerView(searchInput.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        createNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupChat();
            }
        });
        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUser = task.getResult().toObject(Users.class);
                if (!userList.contains(currentUser)) {
                    userList.add(currentUser);
                }
            }
        });
    }

    private void setupUsersRecyclerView(String searchTerm) {
        Query query = ChatFirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("searchName", searchTerm.toLowerCase());

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
        for (Users user : userList) {
            Log.i("create", user.getName() + "info");
        }

        if (userList.size() > 2 && userList.size() <= 5) {
            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            ChatFirebaseUtil.passGroupChatModelAsIntent(intent, userList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } else if (userList.size() == 2) {
            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            ChatFirebaseUtil.passUserModelAsIntent(intent, userList.get(1));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } else {
            Toast.makeText(this, "Group members must be more than 1 and less than 5", Toast.LENGTH_SHORT).show();
        }
    }
//
//    private void createGroupDialog() {
//        LayoutInflater li = LayoutInflater.from(this.getApplicationContext());
//        View viewInflated = li.inflate(R.layout.chat_create_group_dialog, null);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this.getApplicationContext());
//
//        // set prompts.xml to alertdialog builder
//        builder.setView(viewInflated);
//        builder.setTitle("Create A Group");
//
//        // Set up the input
//        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
//        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//        builder.setView(viewInflated);
//
//        // Set up the buttons
//        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                groupName = input.getText().toString();
//            }
//        });
//        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        builder.show();
//    }
}
