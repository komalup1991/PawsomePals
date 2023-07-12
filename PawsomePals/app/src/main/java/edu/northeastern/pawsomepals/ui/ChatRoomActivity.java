package edu.northeastern.pawsomepals.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import edu.northeastern.pawsomepals.R;

public class ChatRoomActivity extends AppCompatActivity {
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton functionBtn;
    ImageButton backBtn;
    TextView otherUserName;
    RecyclerView chatRoomRecyclerView;

//    User otherUser;
    String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

//        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
//        chatRoomId = ChatFirebaseUtil.getChatroomId(ChatFirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput = findViewById(R.id.message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        functionBtn = findViewById(R.id.function_btn);
        chatRoomRecyclerView = findViewById(R.id.message_recycler_view);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getOrCreateChatRoomModel();
    }

    private void getOrCreateChatRoomModel() {
//        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task ->{
//            if()
//        });
    }
}