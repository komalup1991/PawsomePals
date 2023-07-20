package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.auth.User;

import java.sql.Timestamp;
import java.util.Arrays;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.Users;

public class ChatRoomActivity extends AppCompatActivity {
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton functionBtn;
    ImageButton backBtn;
    ImageButton infoBtn;
    TextView otherUserName;
    RecyclerView chatRoomRecyclerView;

    Users otherUser;
    String chatRoomId;
    ChatRoomModel chatRoomModel;
    Timestamp currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        otherUser = ChatFirebaseUtil.getUserModelFromIntent(getIntent());
        chatRoomId = ChatFirebaseUtil.getChatroomId(ChatFirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput = findViewById(R.id.message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        functionBtn = findViewById(R.id.function_btn);
        infoBtn = findViewById(R.id.chatroom_more_button);
        chatRoomRecyclerView = findViewById(R.id.message_recycler_view);
        backBtn = findViewById(R.id.message_back_button);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check
                Intent intent = new Intent(getApplicationContext(),EditChatRoomInfoActivity.class);
                startActivity(intent);
            }
        });

        getOrCreateChatRoomModel();
    }

    private void getOrCreateChatRoomModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task ->{
            if(task.isSuccessful()){
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null){
                    currentTime = new Timestamp(System.currentTimeMillis());
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            Arrays.asList(ChatFirebaseUtil.currentUserId(),otherUser.getUserId()),
                            currentTime,
                            ""

                    );
                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }
}