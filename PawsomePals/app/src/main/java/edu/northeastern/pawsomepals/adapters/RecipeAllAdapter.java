package edu.northeastern.pawsomepals.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class RecipeAllAdapter extends RecyclerView.Adapter<RecipeAllAdapter.RecipeAllViewHolder> {

    private final Activity activity;
    private List<Recipe> recipes;
    private OnItemActionListener onItemActionListener;


    public RecipeAllAdapter(Activity activity, List<Recipe> recipes, List<Users> user, OnItemActionListener onItemActionListener) {
        this.recipes = recipes;
        this.onItemActionListener = onItemActionListener;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecipeAllAdapter.RecipeAllViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_all_layout, parent, false);
        return new RecipeAllAdapter.RecipeAllViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAllAdapter.RecipeAllViewHolder holder, int position) {
        holder.bindData(activity, recipes.get(holder.getBindingAdapterPosition()), onItemActionListener);
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void setRecipes(List<Recipe> recipeList) {
        recipes = recipeList;
    }

    public static class RecipeAllViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView username;
        ImageView userProfilePic;
        TextView recipeName, timestampTextView;

        FeedActionsLayout feedActionsLayout;

        public RecipeAllViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImageView);
            userProfilePic = itemView.findViewById(R.id.userProfilePic);
            recipeName = itemView.findViewById(R.id.recipeNameTextView);
            username = itemView.findViewById(R.id.usernameTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            feedActionsLayout = itemView.findViewById(R.id.feed_action);
        }

        public void bindData(Activity activity, Recipe recipe, OnItemActionListener onItemActionListener) {
            feedActionsLayout.bindView(activity, recipe);
            recipeName.setText(recipe.getTitle());
            username.setText(recipe.getUsername());
            timestampTextView.setText(TimeUtil.formatTime(recipe.getCreatedAt()));

            Glide.with(itemView.getContext())
                    .load(recipe.getImg())
                    .transform(new FitCenter(), new RoundedCorners(20))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(recipeImage);

            Glide.with(itemView.getContext())
                    .load(recipe.getUserProfileImage())
                    .into(userProfilePic);

            recipeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onRecipeClick(recipe);
                }
            });

            recipeName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onRecipeClick(recipe);
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onUserClick(recipe);
                }
            });
            userProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onUserClick(recipe);
                }
            });
        }

    }
}
