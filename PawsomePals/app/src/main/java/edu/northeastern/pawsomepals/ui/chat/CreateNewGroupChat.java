package edu.northeastern.pawsomepals.ui.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatCreateGroupUserImgAdapter;
import edu.northeastern.pawsomepals.adapters.ChatGroupUserRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.Users;

public class CreateNewGroupChat extends AppCompatActivity {
    public final static int ADD_DATA_POSITION = -1;
    private EditText searchInput;
    private RecyclerView usersRecyclerview;
    private RecyclerView groupUserImgsRecyclerview;
    private ChatGroupUserRecyclerAdapter userAdapter;
    private ChatCreateGroupUserImgAdapter imgAdapter;
    private FirestoreRecyclerOptions<Users> options;
    private List<Users> userList;
    private List<String> imgList, checkedUserList;
    private Users currentUser;
    private String groupName;
    private EditText editTextField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        userList = new ArrayList<>();
        imgList = new ArrayList<>();
        checkedUserList = new ArrayList<>();
        searchInput = findViewById(R.id.chat_search_username);
        ImageButton backButton = findViewById(R.id.chat_user_back_button);
        usersRecyclerview = findViewById(R.id.chat_search_user_recyclerView);
        groupUserImgsRecyclerview = findViewById(R.id.create_group_imgs_recyclerview);
        Button createNewChat = findViewById(R.id.finish_select_button);

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
        createNewChat.setOnClickListener(view -> createGroupChat());
        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
//            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            currentUser = task.getResult().toObject(Users.class);
        });
        setupUsersRecyclerView(searchInput.getText().toString());
        setupCreateGroupImgsRecyclerView();
    }

    private void setupUsersRecyclerView(String searchTerm) {
        Query query;
        if (searchTerm.equals("")) {
            query = ChatFirebaseUtil.allUserCollectionReference()
                    .orderBy("searchName");
        } else {
            query = ChatFirebaseUtil.allUserCollectionReference()
                    .whereGreaterThanOrEqualTo("searchName", searchTerm.toLowerCase());
        }

        options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class).build();

        userAdapter = new ChatGroupUserRecyclerAdapter(options, getApplicationContext(), checkedUserList);
        userAdapter.setOnItemClickListener((position, isChecked) -> {
            Users selectedUser = options.getSnapshots().get(position);
            if (isChecked) {
                if (!checkedUserList.contains(selectedUser.getUserId()))
                    checkedUserList.add(selectedUser.getUserId());
                if (!imgList.contains(selectedUser.getProfileImage())) {
                    userList.add(selectedUser);
                    imgList.add(selectedUser.getProfileImage());
                    imgAdapter.updateData(imgList,ADD_DATA_POSITION);
                } else if (imgList.contains(selectedUser.getProfileImage())){
                    Toast.makeText(getApplicationContext(), "You have selected this user", Toast.LENGTH_SHORT).show();
                }
            } else {
                checkedUserList.remove(selectedUser.getUserId());
                if (imgList.contains(selectedUser.getProfileImage())) {
                    Optional<String> findFirst = imgList.stream()
                            .filter(a -> a.equals(selectedUser.getProfileImage()))
                            .findFirst();
                    findFirst.ifPresent(img -> {
                        userList.remove(imgList.indexOf(img));
                        imgList.remove(img);
                        imgAdapter.updateData(imgList,imgList.indexOf(img));
                    });
                } else if(!imgList.contains(selectedUser.getProfileImage())){
                    Toast.makeText(getApplicationContext(), "You did not select this user", Toast.LENGTH_SHORT).show();
                }
            }
        });
        usersRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        usersRecyclerview.setAdapter(userAdapter);
        userAdapter.startListening();
    }

    private void setupCreateGroupImgsRecyclerView() {
        imgAdapter = new ChatCreateGroupUserImgAdapter(getApplicationContext(), imgList);
        imgAdapter.setOnItemClickListener(position -> {
            Users selectedUser = userList.get(position);
            userList.remove(position);
            imgList.remove(position);
            checkedUserList.remove(selectedUser.getUserId());
            imgAdapter.updateData(imgList,position);
            userAdapter.updateData(checkedUserList);
            userAdapter.updateOptions(options);
        });
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(RecyclerView.HORIZONTAL);
        groupUserImgsRecyclerview.setLayoutManager(manager);
        groupUserImgsRecyclerview.setAdapter(imgAdapter);
        imgAdapter.notifyDataSetChanged();
    }

    private void createGroupChat() {
        if (imgList.size() > 1 && imgList.size() < 5) {
            createDialogAndCreateIntent();
        } else if (imgList.size() == 1) {
            Log.i("googleaccount", userList.get(0).toString()+"account");
            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            ChatFirebaseUtil.passCurrentUserNameAsIntent(intent, currentUser.getName());
            ChatFirebaseUtil.passUserModelAsIntent(intent, userList.get(0));
            ChatFirebaseUtil.passChatStyleFromIntent(intent, ChatStyle.ONEONONE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } else {
            Toast.makeText(this, "Selected members must be more than 0 and less than 5", Toast.LENGTH_SHORT).show();
        }
    }

    private void createDialogAndCreateIntent() {
        if (!userList.contains(currentUser)) {
            userList.add(currentUser);
            checkedUserList.add(currentUser.getUserId());
        }
        checkGroupExistence(checkedUserList);

    }

    private void checkGroupExistence(List<String> userList) {
        String roomId = ChatFirebaseUtil.getGroupRoomId(userList);
        DocumentReference df = ChatFirebaseUtil.allChatRoomCollectionReference().document(roomId);
        df.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()){
                            Toast.makeText(getApplicationContext(),"The chatroom already exist.",Toast.LENGTH_SHORT).show();
                        }else{
                            showDialogAndCreateRoom();
                        }
                    }
                });

    }
    private void showDialogAndCreateRoom(){
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
