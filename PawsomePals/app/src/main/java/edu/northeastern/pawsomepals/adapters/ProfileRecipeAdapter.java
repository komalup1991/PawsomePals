package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;

import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.ui.feed.RecipeDetailActivity;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.ProfileRecipeViewHolder> {
    private List<Recipe> recipes;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private Boolean isUserProfile;
    private static final int VIEW_TYPE_RECIPE_PROFILE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    public ProfileRecipeAdapter(List<Recipe> recipes, Context context) {
        this.recipes = recipes;
        this.context = context;
        this.isUserProfile = isUserProfile;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    @NonNull
    @Override
    public ProfileRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_recipes_items_row, parent, false);
        return new ProfileRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        if (recipes.isEmpty()) {
            holder.hideViews();
        } else {
            holder.showViews();
            try {
                holder.bind(recipe);

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, RecipeDetailActivity.class);
                        intent.putExtra("recipe", recipe);
                        context.startActivity(intent);
                    }
                });

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return recipes.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_RECIPE_PROFILE;
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView emptyTextView;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyTextView = itemView.findViewById(R.id.emptyTextView);
        }
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;

    }

    public class ProfileRecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageRecipe;
        private TextView nameTextView;

        private CardView cardView;

        public ProfileRecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            imageRecipe = itemView.findViewById(R.id.imageRecipe);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Recipe recipe) throws ParseException {

            nameTextView.setText(recipe.getTitle());


            String imageUrl = recipe.getImg();

            if (!Objects.isNull(imageUrl)) {
                if (!imageUrl.equals("") && !imageUrl.equals("null")) {
                    Glide.with(context)
                            .load(imageUrl)
                            .into(imageRecipe);
                } else {
                    Glide.with(context)
                            .load(R.drawable.dog)
                            .into(imageRecipe);
                }
            } else {
                Glide.with(context)
                        .load(R.drawable.dog)
                        .into(imageRecipe);
            }
        }

        private void hideViews() {
            imageRecipe.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
        }

        private void showViews() {
            // Show views when dog profiles are available
            imageRecipe.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
        }

    }


}
