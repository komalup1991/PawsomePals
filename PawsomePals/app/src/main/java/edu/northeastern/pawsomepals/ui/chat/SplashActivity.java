package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.login.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (ChatFirebaseUtil.isLoggedIn() && getIntent().getExtras() != null){
            //from notification
            String userId = getIntent().getExtras().getString("userId");
            ChatFirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                Users model = task.getResult().toObject(Users.class);

                                Intent mainIntent = new Intent(getApplicationContext(),ChatFragment.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mainIntent);

                                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                                ChatFirebaseUtil.passUserModelAsIntent(intent,model);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(ChatFirebaseUtil.isLoggedIn()){
                        startActivity(new Intent(SplashActivity.this, ChatFragment.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    }
                    finish();
                }
            },1000);
        }
    }
}
