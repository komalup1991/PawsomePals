package edu.northeastern.pawsomepals.ui.chat;

import static edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil.allUserCollectionReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.provider.MediaStore;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatMessageRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ChatRoomActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int PERMISSIONS_REQUEST_STORAGE = 4;
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton functionBtn;
    private ImageButton backBtn;
    private ImageButton infoBtn;
    private TextView chatRoomName;
    private RecyclerView chatRoomRecyclerView;
    private List<Users> otherGroupUsers;
    private List<Users> groupUsers;

    private Users otherUser;
    private String chatRoomId;
    private ChatRoomModel chatRoomModel;
    private ChatMessageRecyclerAdapter adapter;

    private GroupChatModel group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initialView();
        otherGroupUsers = new ArrayList<>();
        groupUsers = new ArrayList<>();
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageInput.getText().toString().trim();
                if (message.isEmpty()) return;
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
                Intent intent = new Intent(getApplicationContext(), EditChatRoomInfoActivity.class);
                //check
                ChatFirebaseUtil.passGroupChatModelAsIntent(intent, groupUsers);
                startActivity(intent);
            }
        });

        functionBtn.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               showDialog();
                                           }
                                       }
        );

        if (ChatFirebaseUtil.getChatStyleFromIntent(getIntent()).equals("oneOnOne")) {
            otherUser = ChatFirebaseUtil.getUserModelFromIntent(getIntent());
            chatRoomId = ChatFirebaseUtil.getChatroomId(ChatFirebaseUtil.currentUserId(), otherUser.getUserId());
            chatRoomName.setText(otherUser.getName());
            getOrCreateChatRoomModel();

        } else if (ChatFirebaseUtil.getChatStyleFromIntent(getIntent()).equals("group")) {
            group = ChatFirebaseUtil.getGroupChatModelFromIntent(getIntent());
            chatRoomId = ChatFirebaseUtil.getGroupRoomId(group.getGroupMembers());
            chatRoomName.setText(group.getGroupName());

            List<DocumentReference> references = ChatFirebaseUtil.getGroupFromChatRoom(group.getGroupMembers());
            for (DocumentReference reference : references) {
                reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Users user = snapshot.toObject(Users.class);
                        if (user != null)
                            if (!user.getUserId().equals(ChatFirebaseUtil.currentUserId())) {
                                otherGroupUsers.add(user);
                                groupUsers.add(user);
                            }
                        if (user.getUserId().equals(ChatFirebaseUtil.currentUserId())) {
                            groupUsers.add(user);
                        }
                    }
                });
            }

            getOrCreateGroupChatModel();
        }

        setupChatRecyclerView();
    }

    private void initialView() {
        chatRoomName = findViewById(R.id.userName);
        messageInput = findViewById(R.id.message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        functionBtn = findViewById(R.id.function_btn);
        infoBtn = findViewById(R.id.chatroom_more_button);
        chatRoomRecyclerView = findViewById(R.id.message_recycler_view);
        backBtn = findViewById(R.id.message_back_button);
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chat_bottom_sheet_layout);

        LinearLayout cameraLayout = dialog.findViewById(R.id.layoutCamera);
        LinearLayout galleryLayout = dialog.findViewById(R.id.layoutGallery);
        LinearLayout locationLayout = dialog.findViewById(R.id.layoutLocation);

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleImageCaptureFromCamera();
            }
        });
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleImagePickFromGallery();
            }
        });
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    private void setupChatRecyclerView() {
        Query query = ChatFirebaseUtil.getChatroomMessageReference(chatRoomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatMessageRecyclerAdapter(options, getApplicationContext(),Arrays.asList(otherUser.getName()));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        chatRoomRecyclerView.setLayoutManager(manager);
        chatRoomRecyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chatRoomRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToUser(String message) {
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, ChatFirebaseUtil.currentUserId(), Timestamp.now());
        ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            sendNotification(message);
                        }
                    }
                });
    }

    private void getOrCreateChatRoomModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null) {
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            Arrays.asList(ChatFirebaseUtil.currentUserId(), otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    chatRoomModel.setChatStyle(ChatStyle.ONEONONE);
                    chatRoomModel.setOtherUserName(otherUser.getName());
                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }

    private void getOrCreateGroupChatModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null) {
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            group.getGroupMembers(),
                            Timestamp.now(),
                            ""
                    );
                    chatRoomModel.setChatStyle(ChatStyle.GROUP);
                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }

    private void sendNotification(String message) {
        if (otherGroupUsers.size() == 0) {
            otherGroupUsers.add(otherUser);
        }

        //current username, message, currentuserid,otheruserid
        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Users currentUser = task.getResult().toObject(Users.class);

                    try {
                        for (Users user : otherGroupUsers) {
                            JSONObject jasonObject = new JSONObject();
                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", currentUser.getName());
                            notificationObj.put("body", message);

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", currentUser.getUserId());

                            jasonObject.put("notification", notificationObj);
                            jasonObject.put("data", dataObj);

                            if (user != null) {
                                jasonObject.put("to", user.getFcmToken());
                            }
                            callApi(jasonObject);
                        }
                    } catch (Exception e) {
                        Log.i("info", "exeption");
                    }
                }
            }
        });
    }

    private void callApi(JSONObject jasonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jasonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA7LggfNU:APA91bHUj7USk9d2fCoErjjeekUeYJ7LM1JHYAvqX1SeBxyuKYVXn0yl4onyw2hRt8TUo7Pd_Q0LfaqmUNpl5X8--ylQb5qvBoFTCxNHwBfBwQ901LkGbGGkofPpOizcy-Vo74Nfa8CU")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()){
//                    try {
//                        if (response.body() != null) {
//                            JSONObject responseJson = new JSONObject();
//                            JSONArray results = responseJson.getJSONArray("results");
//                            if(responseJson.getInt("failure")==1){
//                                JSONObject error = (JSONObject) results.get(0);
//                                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                    } catch (JSONException e){
//                        e.printStackTrace();
//                    }
//                    showToast("Notification sent successfully");
////                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permissions granted, proceed with image capture
                    handleImageCaptureFromCamera();
                } else {
                    // Camera permissions denied, show a message or handle accordingly
                    Toast.makeText(this, "Camera permissions denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Storage permissions granted, proceed with image pick
                    handleImagePickFromGallery();
                } else {
                    // Storage permissions denied, show a message or handle accordingly
                    Toast.makeText(this, "Storage permissions denied.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void handleImageCaptureFromCamera() {
        // Check if camera permissions are granted
        if (checkCameraPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            // Request camera permissions if not granted
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
    }

    private void handleImagePickFromGallery() {
        // Check if storage permissions are granted
        if (checkStoragePermission()) {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_GALLERY);
        } else {
            // Request storage permissions if not granted
            requestStoragePermission();
        }
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
        else
        {return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;}
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_IMAGE_GALLERY);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_STORAGE);
        }
    }

}