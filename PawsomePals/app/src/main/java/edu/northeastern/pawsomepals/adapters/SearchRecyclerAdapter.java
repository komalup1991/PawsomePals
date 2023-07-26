package edu.northeastern.pawsomepals.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.search.SearchViewHolder;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private List<Recipe> recipes;

    private SearchRecyclerAdapter.OnItemActionListener onItemActionListener;
    public interface OnItemActionListener {
        void onRecipeClick(Recipe recipe);
    }

    public SearchRecyclerAdapter(List<Recipe> recipes, List<Users> user, SearchRecyclerAdapter.OnItemActionListener onItemActionListener) {
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
        Log.d("title",recipe.getTitle());
        holder.bindThisData(recipe);

        holder.title.setOnClickListener(new View.OnClickListener() {
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
}
