package edu.northeastern.pawsomepals.ui.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatCreateGroupUserImgAdapter;
import edu.northeastern.pawsomepals.adapters.ChatGroupUserRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.Users;

public class CreateNewGroupChat extends AppCompatActivity {
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton backButton;
    private RecyclerView usersRecyclerview;
    private RecyclerView groupUserImgsRecyclerview;
    private ChatGroupUserRecyclerAdapter userAdapter;
    private ChatCreateGroupUserImgAdapter imgAdapter;
    private FirestoreRecyclerOptions<Users> options;
    private List<Users> userList;
    private List<String> imgList;
    private Users currentUser;
    private Button createNewChat;
    private String groupName;
    private EditText editTextField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        userList = new ArrayList<>();
        imgList = new ArrayList<>();
        searchInput = findViewById(R.id.chat_search_username);
        searchButton = findViewById(R.id.chat_search_user_btn);
        backButton = findViewById(R.id.chat_user_back_button);
        usersRecyclerview = findViewById(R.id.chat_search_user_recyclerView);
        groupUserImgsRecyclerview = findViewById(R.id.create_group_imgs_recyclerview);
        createNewChat = findViewById(R.id.finish_select_button);

        backButton.setOnClickListener(v -> onBackPressed());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setupUsersRecyclerView(searchInput.getText().toString());
                searchInput.setHint("");
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
                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                currentUser = task.getResult().toObject(Users.class);
                if (!userList.contains(currentUser)) {
                    userList.add(currentUser);
                }
            }
        });
        setupUsersRecyclerView(searchInput.getText().toString());
        setupCreateGroupImgsRecyclerView();
    }

    private void setupUsersRecyclerView(String searchTerm) {
        Query query = ChatFirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("searchName", searchTerm.toLowerCase());

        options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class).build();

        userAdapter = new ChatGroupUserRecyclerAdapter(options, getApplicationContext());
        userAdapter.setOnItemClickListener(new ChatGroupUserRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Users selectedUser = options.getSnapshots().get(position);
                if(!imgList.contains(selectedUser.getProfileImage())){
                userList.add(selectedUser);
                imgList.add(selectedUser.getProfileImage());
                imgAdapter.updateData(imgList);}else{
                    Toast.makeText(getApplicationContext(),"You have selected this user",Toast.LENGTH_SHORT).show();
                }
            }
        });
        usersRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        usersRecyclerview.setAdapter(userAdapter);
        userAdapter.startListening();
    }

    private void setupCreateGroupImgsRecyclerView() {
        imgAdapter = new ChatCreateGroupUserImgAdapter(getApplicationContext(),imgList);
        imgAdapter.setOnItemClickListener(new ChatCreateGroupUserImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Users selectedUser = userList.get(position);
                userList.remove(position);
                imgList.remove(position);
                imgAdapter.updateData(imgList);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(RecyclerView.HORIZONTAL);
        groupUserImgsRecyclerview.setLayoutManager(manager);
        groupUserImgsRecyclerview.setAdapter(imgAdapter);
        imgAdapter.notifyDataSetChanged();
    }

    private void createGroupChat() {
        if (userList.size() > 2 && userList.size() <= 5) {
            createDialogAndCreateIntent();
        } else if (userList.size() == 2) {
            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            ChatFirebaseUtil.passCurrentUserNameAsIntent(intent,currentUser.getName());
            ChatFirebaseUtil.passUserModelAsIntent(intent, userList.get(1));
            ChatFirebaseUtil.passChatStyleFromIntent(intent, ChatStyle.ONEONONE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } else {
            Toast.makeText(this, "Group members must be more than 1 and less than 5", Toast.LENGTH_SHORT).show();
        }
    }

    private void createDialogAndCreateIntent() {
        editTextField = new EditText(this.getApplicationContext());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create A Group Name")
                .setView(editTextField)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        groupName = editTextField.getText().toString();
                        Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                        ChatFirebaseUtil.passGroupChatModelAsIntent(intent, userList, groupName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
}
