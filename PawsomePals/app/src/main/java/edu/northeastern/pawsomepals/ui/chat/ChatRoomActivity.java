package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.Arrays;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatRoomModel;

public class ChatRoomActivity extends AppCompatActivity {
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton functionBtn;
    ImageButton backBtn;
    TextView otherUserName;
    RecyclerView chatRoomRecyclerView;

//    UserModel otherUser;
    String chatRoomId;
    ChatRoomModel chatRoomModel;
    Timestamp currentTime;

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
        backBtn = findViewById(R.id.message_back_button);

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
//            if(task.isSuccessful()){
//                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
//
//                if (chatRoomModel == null){
//                    currentTime = new Timestamp(System.currentTimeMillis());
//                    //first time chat
//                    chatRoomModel = new ChatRoomModel(
//                            chatRoomId,
//                            Arrays.asList(ChatFirebaseUtil.currentUserId(),otherUser.getUserId),
//                            currentTime,
//                            ""
//
//                    );
//                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
//                }
//            }
//        });
    }
}