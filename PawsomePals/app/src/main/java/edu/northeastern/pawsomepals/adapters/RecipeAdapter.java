package edu.northeastern.pawsomepals.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;

    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeAdapter.RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_layout, parent, false);
        return new RecipeViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.recipeName.setText(recipe.getTitle());
        holder.username.setText(recipe.getUsername());
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImg())
                .into(holder.recipeImage);

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImg())
                .into(holder.userProfilePic);

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
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder{
        ImageView recipeImage;
        TextView username;
        ImageView userProfilePic;
        TextView recipeName;


        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            userProfilePic = itemView.findViewById(R.id.userProfilePic);
            recipeName = itemView.findViewById(R.id.recipeName);
            username = itemView.findViewById(R.id.username);

        }
    }
}
