package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class ChatFirebaseUtil {
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        return false;
    }

    public static void passUserModelAsIntent(Intent intent, Users model) {
        intent.putExtra("name", model.getName());
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("email", model.getEmail());
        intent.putExtra("fcmToken", model.getFcmToken());
//        intent.putExtra("chatStyle", "ONEONONE");
    }
    public static void passChatStyleFromIntent(Intent intent, ChatStyle style){
        intent.putExtra("chatStyle",style.toString());
    }

    public static void passCurrentUserNameAsIntent(Intent intent, String name){
        intent.putExtra("currentUserName",name);
    }
    public static void passGroupNameAsIntent(Intent intent, String newGroupName) {
        intent.putExtra("groupName", newGroupName);
    }
    public static void passGroupUsersNamesAsIntent(Intent intent, String groupUsersNames) {
        intent.putExtra("groupUserNames",groupUsersNames);
    }

    public static void passGroupChatModelAsIntent(Intent intent, List<Users> users, String groupName) {
        StringBuilder idBuilder = new StringBuilder();
        StringBuilder nameBuilder = new StringBuilder();

        for (Users user : users) {
            if (user != null) {
                idBuilder.append(user.getUserId() + " ");
                nameBuilder.append(user.getName().toLowerCase() +" ");
            }
        }
        intent.putExtra("name", groupName);
        intent.putExtra("groupUserNames",nameBuilder.toString());
        intent.putExtra("ids", idBuilder.toString());
        intent.putExtra("chatStyle", ChatStyle.GROUP.toString());
    }
    public static void passGroupChatModelFromNotification(Intent intent, String userIds, String userNames,String groupName){
        intent.putExtra("name", groupName);
        intent.putExtra("groupUserNames",userNames);
        intent.putExtra("ids", userIds);
        intent.putExtra("chatStyle", ChatStyle.GROUP);
    }

    public static DocumentReference currentUserDetails() {
        return FirebaseFirestore.getInstance().collection("user").document(currentUserId());
    }

//    public static String currentUserName(){
//        final String name;
//        FirebaseFirestore.getInstance().collection("user").document(currentUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot snapshot) {
//
//                name = snapshot.get("name").toString();
//            }
//        });
//        return name;
//    }

    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("user");
    }

    public static CollectionReference allRecipeCollectionReference() {
        Log.i("coll", FirebaseFirestore.getInstance().collection("recipes").getClass().toString());
        return FirebaseFirestore.getInstance().collection("recipes");
    }

    public static StorageReference allChatRoomImagesCollectionReference() {
        return FirebaseStorage.getInstance().getReference().child("chat_message_images");
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        return FirebaseFirestore.getInstance().collection("chatroom").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId) {
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static String getGroupRoomId(List<String> userIds) {
        StringBuilder groupChatId = new StringBuilder();
        userIds.sort((s1, s2) -> {
            int hashCompare = Integer.compare(s1.hashCode(), s2.hashCode());
            if (hashCompare != 0) {
                return hashCompare;
            } else {
                return s1.compareTo(s2);
            }
        });

        for (int i = 0; i < userIds.size(); i++) {
            groupChatId.append(userIds.get(i));
            if (i != userIds.size() - 1) {
                groupChatId.append("_");
            }

        }
        return "group_" + groupChatId;
    }
    public static String getCurrentUserNameFromIntent(Intent intent){
        return intent.getStringExtra("currentUserName");
    }
    public static Users getUserModelFromIntent(Intent intent) {
        String userName = intent.getStringExtra("name");
        String userId = intent.getStringExtra("userId");
        String email = intent.getStringExtra("email");
        String fcmToken = intent.getStringExtra("fcmToken");

        return new Users(userName, userId, email, fcmToken);
    }

    public static String getChatStyleFromIntent(Intent intent) {
        return intent.getStringExtra("chatStyle");
    }

    public static GroupChatModel getGroupChatModelFromIntent(Intent intent) {
        String groupName = intent.getStringExtra("name");
        String ids = intent.getStringExtra("ids");
        String names = intent.getStringExtra("groupUserNames");
        List idList = Arrays.asList(ids.split(" "));
        List nameList = Arrays.asList(names.split(" "));
        return new GroupChatModel(idList, nameList,groupName);
    }

    public static String getGroupNameFromIntent(Intent intent) {
        return intent.getStringExtra("groupName");
    }

    public static String getGroupUsersNamesAsIntent(Intent intent) {
        return intent.getStringExtra("groupUserNames");
    }


    public static CollectionReference allChatRoomCollectionReference() {
        return FirebaseFirestore.getInstance().collection("chatroom");
    }
    public static void passMembersCardViewsDataAsIntent(Intent intent, List<Users> groupUsers ){
        StringBuilder imgBuilder = new StringBuilder();
        StringBuilder nameBuilder = new StringBuilder();

        for(Users user:groupUsers){
            imgBuilder.append(user.getProfileImage()+" ");
            nameBuilder.append(user.getName()+" ");
        }
        intent.putExtra("userImgs",imgBuilder.toString());
        intent.putExtra("groupUserNames",nameBuilder.toString());
    }
    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        if (userIds.get(0).equals(ChatFirebaseUtil.currentUserId())) {
            return allUserCollectionReference().document(userIds.get(1));
        } else {
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static List<DocumentReference> getGroupFromChatRoom(List<String> userIds) {
        List<DocumentReference> references = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            references.add(allUserCollectionReference().document(userIds.get(i)));
        }
        return references;
    }

    public static String getGroupMemberImgsFromChatRoom(Intent intent){
        return intent.getStringExtra("userImgs");
    }
    public static String getGroupMemberNameFromChatRoom(Intent intent){
        return intent.getStringExtra("groupUserNames");
    }

    public static String timestampToString(Timestamp timestamp) {
        return new SimpleDateFormat("MMM d HH:mm:ss").format(timestamp.toDate());
    }

    public static void uploadImageToStorage(Uri imageUri, FirebaseUtil.DataCallback dataCallback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri uploadImageUri = null;
        if (imageUri != null) {
            uploadImageUri = imageUri;
        }

        if (uploadImageUri != null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "chat" + "_image_" + timestamp + ".jpg";
            StorageReference imageRef = storageRef.child("chat_message_images/" + imageName);
            UploadTask uploadTask = imageRef.putFile(uploadImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            dataCallback.onImageUriReceived(downloadUri.toString());
                        }
                    } else {
                        dataCallback.onError(task.getException());
                    }
                }
            });
        } else {
            dataCallback.onDismiss();
        }
    }
}
