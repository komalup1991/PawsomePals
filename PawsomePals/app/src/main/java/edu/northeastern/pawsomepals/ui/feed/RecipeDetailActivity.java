package edu.northeastern.pawsomepals.ui.feed;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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
    ImageView recipeImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        Toolbar toolbar = findViewById(R.id.recipeDetailToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        userProfilePic = findViewById(R.id.userProfilePic);

        usernameTextView = findViewById(R.id.usernameTextView);
        recipeNameTextView = findViewById(R.id.recipeNameTextView);

        recipeDescriptionTextView = findViewById(R.id.recipeDescriptionTextView);
        recipeIngredientsTextView = findViewById(R.id.recipeIngredientsTextView);
        recipeInstructionTextView = findViewById(R.id.recipeInstructionTextView);
        recipeServingSizeTextView = findViewById(R.id.recipeServingSizeTextView);
        recipeCookTimeTextView = findViewById(R.id.recipeCookTimeTextView);
        recipePrepTimeTextView = findViewById(R.id.recipePrepTimeTextView);
        recipeImageView = findViewById(R.id.recipeImageView);
        CardView cardView2 = findViewById(R.id.cardView2);
        CardView cardView3 = findViewById(R.id.cardView3);
        CardView cardView4 = findViewById(R.id.cardView4);
        CardView cardView5 = findViewById(R.id.cardView5);
        CardView cardView6 = findViewById(R.id.cardView6);
        CardView cardView7 = findViewById(R.id.cardView7);


        recipeNameTextView.setText(recipe.getTitle());
        String description = recipe.getDesc();
        if (description != null && !description.isEmpty()) {
            recipeDescriptionTextView.setText(description);
        } else {
            cardView2.setVisibility(View.GONE);
        }

        String ingredients = recipe.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            recipeIngredientsTextView.setText(ingredients);
        } else {
            cardView3.setVisibility(View.GONE);
        }

        String instruction = recipe.getInstructions();
        if (instruction != null && !instruction.isEmpty()) {
            recipeInstructionTextView.setText(instruction);
        } else {
            cardView4.setVisibility(View.GONE);
        }

        String servingSize = recipe.getServing();
        if (servingSize != null && !servingSize.isEmpty()) {
            recipeServingSizeTextView.setText(servingSize);
        } else {
            cardView5.setVisibility(View.GONE);
        }

        String cookTime = recipe.getCookTime();
        if (cookTime != null && !cookTime.isEmpty()) {
            recipeCookTimeTextView.setText(cookTime);
        } else {
            cardView6.setVisibility(View.GONE);
        }

        String prepTime = recipe.getPrepTime();
        if (prepTime != null && !prepTime.isEmpty()) {
            recipePrepTimeTextView.setText(prepTime);
        } else {
            cardView7.setVisibility(View.GONE);
        }
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}


