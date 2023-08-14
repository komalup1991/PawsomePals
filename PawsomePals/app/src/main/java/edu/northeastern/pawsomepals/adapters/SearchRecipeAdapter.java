package edu.northeastern.pawsomepals.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.ui.search.SearchViewHolder;

public class SearchRecipeAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private List<Recipe> recipes;

    private SearchRecipeAdapter.OnItemActionListener onItemActionListener;
    public interface OnItemActionListener {
        void onRecipeClick(Recipe recipe);
    }

    public SearchRecipeAdapter(List<Recipe> recipes, SearchRecipeAdapter.OnItemActionListener onItemActionListener) {
        this.recipes = recipes;
        this.onItemActionListener = onItemActionListener;
    }
    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_recipe, parent, false);
        return new SearchViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {

            Recipe recipe = recipes.get(position);
            holder.title.setText(recipe.getTitle());
            Log.d("title", recipe.getTitle());
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImg())
                    .into(holder.searchImage);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onRecipeClick(recipes.get(position));
                }
            });


        }


    public void setRecipes(List<Recipe> recipeList) {
        recipes = recipeList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void clearData() {
        recipes.clear();
        notifyDataSetChanged();
    }
}
