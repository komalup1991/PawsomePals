package edu.northeastern.pawsomepals.utils;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.pawsomepals.R;

public class BulkUserCreationActivity extends AppCompatActivity {

    private static final String TAG = "BulkUserCreation";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        for (int i = 0; i < 10; i++) {
            String email = "user" + i + "@example.com";
            String password = "password123";

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = auth.getCurrentUser();
                                if (currentUser != null) {
                                    String loggedInUserId = currentUser.getUid();
                                    createUserDetails(email, loggedInUserId);
                                }
                                Log.d(TAG, "User created successfully: " + email);
                            } else {
                                Log.e(TAG, "User creation failed for: " + email, task.getException());
                            }
                        }
                    });
        }
    }

    private void createUserDetails(String email, String loggedInUserId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", loggedInUserId);
        userInfo.put("email", email);

        firebaseFirestore.collection("user")
                .document(loggedInUserId)
                .set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("Create User", "Error adding document", e);
                });
    }
}
