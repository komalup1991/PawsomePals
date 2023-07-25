package edu.northeastern.pawsomepals.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import java.util.Arrays;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatMessageRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.Users;

public class ChatRoomActivity extends AppCompatActivity {
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton functionBtn;
    private ImageButton backBtn;
    private ImageButton infoBtn;
    private TextView otherUserName;
    private RecyclerView chatRoomRecyclerView;

    private Users otherUser;
    private String chatRoomId;
    private ChatRoomModel chatRoomModel;
    private ChatMessageRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        otherUser = ChatFirebaseUtil.getUserModelFromIntent(getIntent());
        otherUserName = findViewById(R.id.userName);
        chatRoomId = ChatFirebaseUtil.getChatroomId(ChatFirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput = findViewById(R.id.message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        functionBtn = findViewById(R.id.function_btn);
        infoBtn = findViewById(R.id.chatroom_more_button);
        chatRoomRecyclerView = findViewById(R.id.message_recycler_view);
        backBtn = findViewById(R.id.message_back_button);

        otherUserName.setText(otherUser.getName());

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageInput.getText().toString().trim();
                if(message.isEmpty()) return;
                sendMessageToUser(message);
            }
        });

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
        setupChatRecyclerView();
    }

    private void setupChatRecyclerView() {
        Query query = ChatFirebaseUtil.getChatroomMessageReference(chatRoomId)
                .orderBy("timestamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();
        adapter = new ChatMessageRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        chatRoomRecyclerView.setLayoutManager(manager);
        chatRoomRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void sendMessageToUser(String message) {
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,ChatFirebaseUtil.currentUserId(), Timestamp.now());
        ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            messageInput.setText("");
                        }
                    }
                });
    }

    private void getOrCreateChatRoomModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task ->{
            if(task.isSuccessful()){
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null){
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            Arrays.asList(ChatFirebaseUtil.currentUserId(),otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }
}