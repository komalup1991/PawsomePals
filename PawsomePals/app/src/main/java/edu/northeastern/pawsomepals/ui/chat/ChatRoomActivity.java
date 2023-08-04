package edu.northeastern.pawsomepals.ui.chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatMessageRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.Comment;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.ImageUtil;
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
    private ImageView img_preview;
    private TextView imgPreviewTextView;
    private TextView chatRoomName;
    private RecyclerView chatRoomRecyclerView;
    private List<Users> otherGroupUsers;
    private List<Users> groupUsers;

    private Users currentUser, otherUser;
    private String chatRoomId;
    private ChatRoomModel chatRoomModel;
    private ChatMessageRecyclerAdapter adapter;

    private GroupChatModel group;
    private Uri fileUri;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initialView();
        otherGroupUsers = new ArrayList<>();
        groupUsers = new ArrayList<>();
        sendMessageBtn.setOnClickListener(view -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty() && img_preview == null) return;
            if (img_preview != null) {
                sendImageToUser();
            } else {
                sendMessageToUser(message);
            }
        });

        backBtn.setOnClickListener(v -> onBackPressed());

        infoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditChatRoomInfoActivity.class);
            //check
            ChatFirebaseUtil.passGroupChatModelAsIntent(intent, groupUsers);
            startActivity(intent);
        });

        functionBtn.setOnClickListener(view -> showDialog()
        );

        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> currentUser = task.getResult().toObject(Users.class));

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
                reference.get().addOnSuccessListener(snapshot -> {
                    Users user = snapshot.toObject(Users.class);
                    if (user != null) {
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
        img_preview = findViewById(R.id.chat_image_preview);
        imgPreviewTextView = findViewById(R.id.image_preview_textView);
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

        adapter = new ChatMessageRecyclerAdapter(options, getApplicationContext());
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
        ChatMessageModel chatMessageModel;
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);

        if (fileUri == null) {
            chatMessageModel = new ChatMessageModel(message, ChatFirebaseUtil.currentUserId(), Timestamp.now(), currentUser.getName());
            chatMessageModel.setPicture(false);
            ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            sendNotification(message);
                        }
                    });
        } else {

        }

    }

    private void submitChatToFileBase(ChatMessageModel chatMessageModel, boolean picture) {

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
        if (ChatImgUtil.checkCameraPermission(ChatRoomActivity.this)) {
            ChatImgUtil.openCamera(ChatRoomActivity.this);
        } else {
            // Request camera permissions if not granted
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
    }

    private void handleImagePickFromGallery() {
        // Check if storage permissions are granted
        if (ChatImgUtil.checkStoragePermission(ChatRoomActivity.this)) {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPhotoIntent.setType("image/*");
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_GALLERY);
        } else {
            // Request storage permissions if not granted

            ChatImgUtil.requestStoragePermission(ChatRoomActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Handle image capture from the camera
                try {
                    Uri cameraUri = ChatImgUtil.saveCameraImageToFile(data, this);

                    Bitmap bitmap = MediaStore.Images.Media
                            .getBitmap(getContentResolver(), cameraUri);

                    img_preview.setImageBitmap(bitmap);
                    img_preview.setVisibility(View.VISIBLE);
                    imgPreviewTextView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to capture image from camera.", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                try {
                    final Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media
                            .getBitmap(getContentResolver(), imageUri);
                    int targetWidth = 400;
                    int targetHeight = 400;
                    bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                    img_preview.setImageBitmap(bitmap);
                    img_preview.setVisibility(View.VISIBLE);
                    imgPreviewTextView.setVisibility(View.VISIBLE);
                    fileUri = imageUri;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "Please choose image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendImageToUser() {
        ChatMessageModel chatMessageModel;
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage("<Image>" + fileUri);
        ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);

        chatMessageModel = new ChatMessageModel("<Image>", ChatFirebaseUtil.currentUserId(), Timestamp.now(), currentUser.getName());
        chatMessageModel.setPicture(true);

        if (fileUri != null)
            uploadPicture(fileUri, chatMessageModel);
        ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        sendNotification("You receive an <image>");
                    }
                });
        imgPreviewTextView.setVisibility(View.INVISIBLE);
        img_preview.setVisibility(View.INVISIBLE);
        img_preview = null;
        fileUri = null;
    }



    private void uploadPicture(Uri fileUri, ChatMessageModel chatMessageModel) {
        AlertDialog dialog = new AlertDialog.Builder(ChatRoomActivity.this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();
        dialog.show();

        String fileName = getFileName(getContentResolver(), fileUri);
        String path = new StringBuilder(ChatFirebaseUtil.currentUserId())
                .append("/")
                .append(fileName)
                .toString();
//        StorageReference imageRef = storageRef.child("chat_message_images/" + imageName);
        storageReference = FirebaseStorage.getInstance()
                .getReference("chat_message_images/")
                .child(path);
        UploadTask uploadTask = storageReference.putFile(fileUri);
        //create task
        Task<Uri> task = uploadTask.continueWithTask(task1 -> {
            if (!task1.isSuccessful()) {
                Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show();
            }
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task12 -> {
            if (task12.isSuccessful()) {
                String url = task12.getResult().toString();
                dialog.dismiss();

                chatMessageModel.setPicture(true);
                chatMessageModel.setPictureLink(url);

                submitChatToFileBase(chatMessageModel, chatMessageModel.isPicture());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatRoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;

        if (fileUri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(fileUri, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        // Column exists, do something with the data
                        result = cursor.getString(index);
                    } else {
                        // Column does not exist, handle the error
                        Log.e(TAG, "Column 'column_name' does not exist in cursor");
                    }
                }
            } finally {
                cursor.close();
            }
        }

        if (result == null) {
            result = fileUri.getPath();
            int cut = result.lastIndexOf("/");
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

//    @NonNull
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view;
//        if(viewType == 0){
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item,parent,false);
//            return new ChatModelViewHolder(view);
//        } else if (viewType == 1){
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_picture_friend,parent,false);
//            return new ChatPictureReceiveHolder(view);
//        } else if(viewType == 2){
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item,parent,false);
//            return new ChatModelViewHolder(view);
//        } else {
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_picture_own,parent,false);
//            return new ChatPictureHolder(view);
//        }
//
//    }

}