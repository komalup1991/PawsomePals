package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.login.HomeActivity;
import edu.northeastern.pawsomepals.ui.login.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ChatFirebaseUtil.isLoggedIn() && getIntent().getExtras() != null) {
            String userId;
            //from notification
            if (getIntent().getExtras().getString("userId") != null) {
                List<String> userIdList = Arrays.asList(getIntent().getExtras().getString("userId").split(" "));
                if (userIdList.size() == 1) {
                    userId = getIntent().getExtras().getString("userId");
                    ChatFirebaseUtil.allUserCollectionReference().document(userId).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Users model = task.getResult().toObject(Users.class);

                                    Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(mainIntent);

                                    Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                                    ChatFirebaseUtil.passUserModelAsIntent(intent, model);
                                    ChatFirebaseUtil.passChatStyleFromIntent(intent, ChatStyle.ONEONONE);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                } else {
                    Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainIntent);

                    Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                    ChatFirebaseUtil.passGroupChatModelFromNotification(intent,
                            getIntent().getExtras().getString("userId"),
                            getIntent().getExtras().getString("groupUserNames"),
                            getIntent().getExtras().getString("groupName"));
                    ChatFirebaseUtil.passChatStyleFromIntent(intent, ChatStyle.GROUP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }
            } else {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                ChatFirebaseUtil.allUserCollectionReference().document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Users model = task.getResult().toObject(Users.class);

                                Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mainIntent);

                                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                                ChatFirebaseUtil.passUserModelAsIntent(intent, model);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }, 100);
        }
    }
}
