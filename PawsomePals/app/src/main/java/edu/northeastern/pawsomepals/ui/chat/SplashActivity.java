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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.login.HomeActivity;
import edu.northeastern.pawsomepals.ui.login.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ChatFirebaseUtil.isLoggedIn() && getIntent().getExtras() != null){
            //from notification
            String userId = getIntent().getExtras().getString("userId");
            userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
            ChatFirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Users model = task.getResult().toObject(Users.class);

                            Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                            ChatFirebaseUtil.passUserModelAsIntent(intent,model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            },10);
        }
    }
}
