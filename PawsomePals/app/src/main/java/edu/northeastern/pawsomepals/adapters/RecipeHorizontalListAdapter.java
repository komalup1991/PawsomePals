package edu.northeastern.pawsomepals.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.RecipeHorizontalFeedViewHolder;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeHorizontalListAdapter extends RecyclerView.Adapter<RecipeHorizontalFeedViewHolder> {
    private List<Recipe> recipes;
    private final OnItemActionListener onItemActionListener;


    public RecipeHorizontalListAdapter(List<Recipe> recipes, List<Users> user, OnItemActionListener onItemActionListener) {
        this.recipes = recipes;
        this.onItemActionListener = onItemActionListener;
    }

    @NonNull
    @Override
    public RecipeHorizontalFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_layout, parent, false);
        return new RecipeHorizontalFeedViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeHorizontalFeedViewHolder holder, int position) {
        holder.bindData(recipes.get(position), onItemActionListener);
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
}
