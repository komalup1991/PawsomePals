package edu.northeastern.pawsomepals.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeAllAdapter extends RecyclerView.Adapter<RecipeAllAdapter.RecipeAllViewHolder> {

    private List<Recipe> recipes;
    private OnItemActionListener onItemActionListener;


    public RecipeAllAdapter(List<Recipe> recipes, List<Users> user, OnItemActionListener onItemActionListener) {
        this.recipes = recipes;
        this.onItemActionListener = onItemActionListener;
    }

    @NonNull
    @Override
    public RecipeAllAdapter.RecipeAllViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_all_layout, parent, false);
        return new RecipeAllAdapter.RecipeAllViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAllAdapter.RecipeAllViewHolder holder, int position) {

        Recipe recipe = recipes.get(position);
        String user = recipe.getUsername();
        holder.recipeName.setText(recipe.getTitle());
        holder.username.setText(recipe.getUsername());
        holder.timestampTextView.setText(recipe.getCreatedAt());

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImg())
                .transform(new FitCenter(), new RoundedCorners(20))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.recipeImage);

        Glide.with(holder.itemView.getContext())
                .load(recipe.getUserProfileImage())
                .into(holder.userProfilePic);

        holder.recipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onRecipeClick(recipes.get(position));
            }
        });

        holder.recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onRecipeClick(recipes.get(position));
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipes.get(holder.getAbsoluteAdapterPosition()));
            }
        });
        holder.userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipes.get(holder.getAbsoluteAdapterPosition()));
            }
        });

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


        public RecipeAllViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImageView);
            userProfilePic = itemView.findViewById(R.id.userProfilePic);
            recipeName = itemView.findViewById(R.id.recipeNameTextView);
            username = itemView.findViewById(R.id.usernameTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);


        }

    }
}
