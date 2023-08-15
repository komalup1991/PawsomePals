package edu.northeastern.pawsomepals.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class DogDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);

        Toolbar toolbar = findViewById(R.id.recipeDetailToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Dogs dogs = (Dogs) getIntent().getSerializableExtra("dogs");
        Intent intent = getIntent();
        String dogName = intent.getStringExtra("name");
        String dogImage = intent.getStringExtra("image");

        TextView dogNameText = findViewById(R.id.DogName);
        TextView dogBreed = findViewById(R.id.DogBreed);
        TextView dogGender = findViewById(R.id.DogGender);
        TextView dogSize = findViewById(R.id.DogSize);

        String userId = dogs.getUserId();

        CardView cardViewUserDetails = findViewById(R.id.cardViewUserDetails);
        TextView username = cardViewUserDetails.findViewById(R.id.UserName);
        ImageView userImage = cardViewUserDetails.findViewById(R.id.userImage);

        FirebaseUtil.fetchUserInfoFromFirestore(userId, new FirebaseUtil.DataCallback() {
            @Override
            public void onUserReceived(Users user) {
                if (user == null) {
                    return;
                }
                username.setText(user.getName());
                Glide.with(DogDetailActivity.this)
                        .load(user.getProfileImage())
                        .into(userImage);
            }

            @Override
            public void onImageUriReceived(String imageUrl) {
                // Nothing
            }

            @Override
            public void onError(Exception exception) {
                // Nothing
            }

            @Override
            public void onDismiss() {
                // Nothing
            }

            @Override
            public void onRecipeReceived(Recipe recipe) {
                // Nothing
            }

            @Override
            public void onFollowingUserIdListReceived(List<String> followingUserIds) {
                // Nothing
            }
        });

        CircleImageView dogImageCircle = findViewById(R.id.imageProfile);

        dogNameText.setText(dogName);
        Glide.with(this)
                .load(dogImage)
                .into(dogImageCircle);

        if (dogs.getIsMixedBreed()) {
            String mixed = dogs.getBreed() + " + " + dogs.getMixedBreed();
            dogBreed.setText(mixed);
        } else {
            dogBreed.setText(dogs.getBreed());
        }

        dogGender.setText(dogs.getGender());
        dogSize.setText(dogs.getSize());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
