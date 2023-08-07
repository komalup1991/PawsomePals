package edu.northeastern.pawsomepals.ui.feed;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class RecipeDetailActivity extends AppCompatActivity {
    CircleImageView userProfilePic;
    TextView usernameTextView, recipeNameTextView, recipeDescriptionTextView, recipeIngredientsTextView,
            recipeInstructionTextView, recipeServingSizeTextView, recipeCookTimeTextView, recipePrepTimeTextView;
    EditText recipeNotes;
    ImageView recipeImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        userProfilePic = findViewById(R.id.userProfilePic);

        usernameTextView = findViewById(R.id.usernameTextView);
        recipeNameTextView = findViewById(R.id.recipeNameTextView);

        recipeDescriptionTextView = findViewById(R.id.recipeDescriptionTextView);
        recipeIngredientsTextView = findViewById(R.id.recipeIngredientsTextView);
        recipeInstructionTextView = findViewById(R.id.recipeInstructionTextView);
        recipeServingSizeTextView = findViewById(R.id.recipeServingSizeTextView);
        recipeCookTimeTextView = findViewById(R.id.recipeCookTimeTextView);
        recipePrepTimeTextView = findViewById(R.id.recipePrepTimeTextView);
        recipeNotes = findViewById(R.id.recipeNotes);
        recipeImageView = findViewById(R.id.recipeImageView);


        recipeNameTextView.setText(recipe.getTitle());
        recipeDescriptionTextView.setText(recipe.getDesc());
        recipeIngredientsTextView.setText(recipe.getIngredients());
        recipeInstructionTextView.setText(recipe.getInstructions());
        recipeServingSizeTextView.setText(recipe.getServing());
        recipeCookTimeTextView.setText(recipe.getCookTime());
        recipePrepTimeTextView.setText(recipe.getPrepTime());
        Glide.with(RecipeDetailActivity.this)
                .load(recipe.getImg())
                .into(recipeImageView);

        FirebaseUtil.fetchUserInfoFromFirestore(recipe.getCreatedBy(), new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                if (user == null) {
                    return;
                }
                Glide.with(RecipeDetailActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                usernameTextView.setText(user.getName());
            }
        });


    }


}


