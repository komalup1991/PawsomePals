package edu.northeastern.pawsomepals.ui.chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.MediaRouteButton;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatMessageRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatLocationModel;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.Comment;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.login.MainActivity;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
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
    private Dialog functionDialog;

    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton functionBtn,imgDisMissBtn,preview_dismiss_button,preview_location_dismiss_button,backBtn;
    private ImageButton infoBtn;
    private ImageView img_preview,image_view_container;
    private CardView img_cardview;
    private TextView imgPreviewTextView, locationPreviewTextView;
    private TextView chatRoomName;
    private LinearLayout chatRoomToolbar;

    private RecyclerView chatRoomRecyclerView;
    private List<Users> otherGroupUsers;
    private List<Users> groupUsers;
    private List<String> groupUsersNames;
    private ConstraintLayout chat_image_preview_container,chat_location_preview_container;

    private Users currentUser, otherUser;
    private FrameLayout profileShowBackground;
    private String chatRoomId;
    private ChatRoomModel chatRoomModel;
    private ChatMessageRecyclerAdapter adapter;

    private GroupChatModel group;
    private ChatLocationModel location;
    private Uri fileUri;
    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> startAutocomplete = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initialView();
        functionDialog = new Dialog(this);
        functionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        functionDialog.setContentView(R.layout.chat_bottom_sheet_layout);
        otherGroupUsers = new ArrayList<>();
        groupUsers = new ArrayList<>();
        groupUsersNames = new ArrayList<>();
        location = null;
        sendMessageBtn.setOnClickListener(view -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty() && img_preview == null) return;
            if (img_preview != null && fileUri != null) {
                sendImageToUser();
            } else if (!locationPreviewTextView.getText().equals("")) {
                locationPreviewTextView.setText("");
                locationPreviewTextView.setVisibility(View.INVISIBLE);
                sendLocationFileToUser();
            } else {
                sendMessageToUser(message);
            }
        });

        backBtn.setOnClickListener(v -> onBackPressed());
        imgDisMissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                img_cardview.setVisibility(View.INVISIBLE);
            }
        });
        preview_dismiss_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat_image_preview_container.setVisibility(View.INVISIBLE);
                fileUri = null;
            }
        });
        preview_location_dismiss_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat_location_preview_container.setVisibility(View.INVISIBLE);
                location = null;
            }
        });

        functionBtn.setOnClickListener(view -> showDialog()
        );

        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> currentUser = task.getResult().toObject(Users.class));
        if (ChatFirebaseUtil.getChatStyleFromIntent(getIntent()).equals(ChatStyle.ONEONONE.toString())) {
            otherUser = ChatFirebaseUtil.getUserModelFromIntent(getIntent());
            chatRoomId = ChatFirebaseUtil.getChatroomId(ChatFirebaseUtil.currentUserId(), otherUser.getUserId());
            infoBtn.setOnClickListener(v -> {
                navigateToProfileFragment(otherUser.getUserId());
            });
            if (ChatFirebaseUtil.getGroupNameFromIntent(getIntent()) != null) {
                chatRoomName.setText(ChatFirebaseUtil.getGroupNameFromIntent(getIntent()));
            } else {
                chatRoomName.setText(otherUser.getName());
            }

            getOrCreateChatRoomModel();

        } else if (ChatFirebaseUtil.getChatStyleFromIntent(getIntent()).equals(ChatStyle.GROUP.toString())) {
            infoBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), EditChatRoomInfoActivity.class);
                //check
                ChatFirebaseUtil.passGroupChatModelAsIntent(intent, groupUsers, chatRoomName.getText().toString());
                ChatFirebaseUtil.passMembersCardViewsDataAsIntent(intent,groupUsers);
                StringBuilder builder = new StringBuilder();
                startActivity(intent);
            });
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
                            groupUsersNames.add(user.getName().toLowerCase());
                        }
                        if (user.getUserId().equals(ChatFirebaseUtil.currentUserId())) {
                            groupUsers.add(user);
                            groupUsersNames.add(user.getName().toLowerCase());
                        }
                    }
                });
            }

            getOrCreateGroupChatModel();
        }

        setupChatRecyclerView();
        setupLocationRequire();
    }

    private void sendLocationFileToUser() {
        if (location != null) {
            chatRoomModel.setLastMessageTimestamp(Timestamp.now());
            chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
            chatRoomModel.setLastMessage("<Location>");
            ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
            uploadLocation(location);
            chat_location_preview_container.setVisibility(View.INVISIBLE);
        }
    }

    private void uploadLocation(ChatLocationModel location) {
        ChatMessageModel chatMessageModel;
        if (location != null) {
            chatMessageModel = new ChatMessageModel("<location>", ChatFirebaseUtil.currentUserId(), currentUser.getProfileImage(),Timestamp.now(), currentUser.getName(), null, location);
            chatMessageModel.setPicture(false);
            chatMessageModel.setPlace(true);
            ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            sendNotification("You receive a <location>");
                        }
                    });
        }
    }

    private void navigateToProfileFragment(String userIDValue) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profileId", userIDValue);
        setResult(RESULT_OK, resultIntent);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("ProfileId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileId", userIDValue);
        editor.apply();

        //Navigate to Profile Fragment
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chatRoomContainer, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
//
//        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("ProfileId", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("profileId", userIDValue);
//        editor.apply();
//
//        ProfileFragment profileFragment = new ProfileFragment();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container_view,profileFragment)
//                .commit();
    }

    private void setupLocationRequire() {
        if (startAutocomplete == null) {
            startAutocomplete = this.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                Place place = Autocomplete.getPlaceFromIntent(intent);
                                String displayText = place.getName() + "\n" + place.getAddress();
                                place.getLatLng();
                                locationPreviewTextView.setText(displayText);
                                chat_location_preview_container.setVisibility(View.VISIBLE);
                                location = new ChatLocationModel(place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude);
                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            // The user canceled the operation.
                        }
                    });
        }
    }

    private void initialView() {
        chatRoomName = findViewById(R.id.userName);
        messageInput = findViewById(R.id.message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        functionBtn = findViewById(R.id.function_btn);
        imgDisMissBtn = findViewById(R.id.dismiss_button);
        infoBtn = findViewById(R.id.chatroom_more_button);
        preview_dismiss_button = findViewById(R.id.preview_dismiss_button);
        preview_location_dismiss_button = findViewById(R.id.preview_location_dismiss_button);
        chatRoomRecyclerView = findViewById(R.id.message_recycler_view);
        backBtn = findViewById(R.id.message_back_button);
        img_preview = findViewById(R.id.chat_image_preview);
        img_cardview = findViewById(R.id.image_cardview);
        chat_location_preview_container = findViewById(R.id.chat_location_preview_container);
        image_view_container = findViewById(R.id.image_view_container);
        imgPreviewTextView = findViewById(R.id.image_preview_textView);
        locationPreviewTextView = findViewById(R.id.location_preview_textView);
        profileShowBackground = findViewById(R.id.profile_show_background);
        chat_image_preview_container = findViewById(R.id.chat_image_preview_container);
        chatRoomToolbar = findViewById(R.id.chatRoomToolBar);
    }

    private void showDialog() {
        LinearLayout cameraLayout = functionDialog.findViewById(R.id.layoutCamera);
        LinearLayout galleryLayout = functionDialog.findViewById(R.id.layoutGallery);
        LinearLayout locationLayout = functionDialog.findViewById(R.id.layoutLocation);

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleImageCaptureFromCamera();
                functionDialog.dismiss();
            }
        });
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleImagePickFromGallery();
                functionDialog.dismiss();
            }
        });
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLocationPickFromApi();
                functionDialog.dismiss();
            }
        });
        functionDialog.show();
        functionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        functionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        functionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        functionDialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void handleLocationPickFromApi() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startAutocomplete.launch(intent);
    }


    private void setupChatRecyclerView() {
        Query query = ChatFirebaseUtil.getChatroomMessageReference(chatRoomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatMessageRecyclerAdapter(options, getApplicationContext());
        adapter.setOnItemClickListener(new ChatMessageRecyclerAdapter.OnImgItemClickListener() {
            @Override
            public void onItemClick(int position) {
                img_cardview.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext())
                        .load(options.getSnapshots().get(position).getImage())
                        .into(image_view_container);
            }
        });
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

        if (fileUri == null && location == null) {
            chatMessageModel = new ChatMessageModel(message, ChatFirebaseUtil.currentUserId(), currentUser.getProfileImage(),Timestamp.now(), currentUser.getName(), null, null);
            chatMessageModel.setPicture(false);
            chatMessageModel.setPlace(false);

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

    private void getOrCreateChatRoomModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null) {
                    if (ChatFirebaseUtil.getCurrentUserNameFromIntent(getIntent()) == null) {
                        chatRoomModel = new ChatRoomModel(
                                chatRoomId,
                                otherUser.getName(),
                                Arrays.asList(ChatFirebaseUtil.currentUserId(), otherUser.getUserId()),
                                Arrays.asList("", otherUser.getName().toLowerCase()),
                                Timestamp.now(),
                                ""
                        );
                    } else {
                        //first time chat
                        chatRoomModel = new ChatRoomModel(
                                chatRoomId,
                                otherUser.getName(),
                                Arrays.asList(ChatFirebaseUtil.currentUserId(), otherUser.getUserId()),
                                Arrays.asList(ChatFirebaseUtil.getCurrentUserNameFromIntent(getIntent()).toLowerCase(), otherUser.getName().toLowerCase()),
                                Timestamp.now(),
                                ""
                        );
                    }
                    for (String name : chatRoomModel.getUserNames()) {
                        Log.i("create", name + "create");
                    }
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
                            group.getGroupName(),
                            group.getGroupMembers(),
                            group.getGroupMemberNames(),
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
                        if (group == null) {
                            JSONObject jasonObject = new JSONObject();
                            JSONObject notificationObj = new JSONObject();
                            JSONObject dataObj = new JSONObject();
                            notificationObj.put("title", currentUser.getName());
                            notificationObj.put("body", message);

                            dataObj.put("userId", currentUser.getUserId());
                            jasonObject.put("notification", notificationObj);
                            jasonObject.put("data", dataObj);
                            jasonObject.put("to", otherUser.getFcmToken());
                            callApi(jasonObject);
                        } else {
                            for (Users user : otherGroupUsers) {
                                JSONObject jasonObject = new JSONObject();
                                JSONObject notificationObj = new JSONObject();
                                JSONObject dataObj = new JSONObject();
                                StringBuilder userIdBuilder = new StringBuilder();
                                StringBuilder userNameBuilder = new StringBuilder();
                                userIdBuilder.append(currentUser.getUserId() + " ");
                                userNameBuilder.append(currentUser.getName() + " ");
                                for (int i = 0; i < otherGroupUsers.size(); i++) {
                                    userIdBuilder.append(otherGroupUsers.get(i).getUserId());
                                    userNameBuilder.append(otherGroupUsers.get(i).getName());
                                    if (i != otherGroupUsers.size() - 1) {
                                        userIdBuilder.append(" ");
                                        userNameBuilder.append(" ");
                                    }
                                }
                                notificationObj.put("title", group.getGroupName());
                                notificationObj.put("body", message);

                                jasonObject.put("notification", notificationObj);


                                dataObj.put("groupName", group.getGroupName());
                                dataObj.put("groupUserNames", userNameBuilder.toString());
                                dataObj.put("userId", userIdBuilder.toString());
                                jasonObject.put("data", dataObj);

                                jasonObject.put("to", user.getFcmToken());
                                callApi(jasonObject);
                            }
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
        String playload= jasonObject.toString();

        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(playload, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "key=AAAA7LggfNU:APA91bHUj7USk9d2fCoErjjeekUeYJ7LM1JHYAvqX1SeBxyuKYVXn0yl4onyw2hRt8TUo7Pd_Q0LfaqmUNpl5X8--ylQb5qvBoFTCxNHwBfBwQ901LkGbGGkofPpOizcy-Vo74Nfa8CU")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
//                    runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "Data fetched successfully.", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "Error fetching data.", Toast.LENGTH_SHORT).show());
                }
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
                    chat_image_preview_container.setVisibility(View.VISIBLE);
                    fileUri = cameraUri;
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
                    chat_image_preview_container.setVisibility(View.VISIBLE);
                    fileUri = imageUri;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (!locationPreviewTextView.equals("")) {
            } else {
                Toast.makeText(this, "Please choose image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendImageToUser() {
        if (fileUri != null) {
            chatRoomModel.setLastMessageTimestamp(Timestamp.now());
            chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
            chatRoomModel.setLastMessage("<Image>");
            ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);

            uploadPicture(fileUri);
        }
    }

    private void uploadPicture(Uri imageUri) {
        AlertDialog dialog = new AlertDialog.Builder(ChatRoomActivity.this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();
        dialog.show();

        String fileName = getFileName(getContentResolver(), imageUri);
        String path = new StringBuilder(ChatFirebaseUtil.currentUserId())
                .append("/")
                .append(fileName)
                .toString();
//        StorageReference imageRef = storageRef.child("chat_message_images/" + imageName);
        storageReference = FirebaseStorage.getInstance()
                .getReference("chat_message_images/")
                .child(path);
        UploadTask uploadTask = storageReference.putFile(imageUri);
        //create task
        Task<Uri> task = uploadTask.continueWithTask(task1 -> {
            if (!task1.isSuccessful()) {
                Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show();
            }
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task12 -> {
            if (task12.isSuccessful()) {
                ChatMessageModel chatMessageModel;
                String url = task12.getResult().toString();
                dialog.dismiss();

                chat_image_preview_container.setVisibility(View.INVISIBLE);

                chatMessageModel = new ChatMessageModel("<Image>", ChatFirebaseUtil.currentUserId(), currentUser.getProfileImage(),Timestamp.now(), currentUser.getName(), url, null);
                chatMessageModel.setPicture(true);
                chatMessageModel.setPlace(false);
                ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                        .addOnCompleteListener(addTask -> {
                            if (addTask.isSuccessful()) {
                                messageInput.setText("");
                                sendNotification("You receive an <image>");
                            }
                        });
                fileUri = null;
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


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}