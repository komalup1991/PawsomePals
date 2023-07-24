package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.northeastern.pawsomepals.models.Users;

public class ChatFirebaseUtil {
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId() != null){
            return true;
        }
        return false;
    }

    public static void passUserModelAsIntent(Intent intent, Users model){
        intent.putExtra("name",model.getName());
        intent.putExtra("userId",model.getUserId());
        intent.putExtra("email",model.getEmail());
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("user").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("user");
    }

    public static CollectionReference allRecipeCollectionReference(){
        Log.i("coll", FirebaseFirestore.getInstance().collection("recipes").getClass().toString());
        return FirebaseFirestore.getInstance().collection("recipes");
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatroom").document(chatroomId);
    }

    public static String getChatroomId(String userId1,String userId2){
        if (userId1.hashCode() < userId2.hashCode()){
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static Users getUserModelFromIntent(Intent intent) {
        String userName = intent.getStringExtra("name");
        String userId =  intent.getStringExtra("userId");
        String email = intent.getStringExtra("email");
        return new Users(userName,userId,email);
    }
}
