package edu.northeastern.pawsomepals.ui.search;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public ImageView recipeImage;


    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = itemView.findViewById(R.id.title);
        this.recipeImage = itemView.findViewById(R.id.circleImageView);

    }

    public void bindThisData(Recipe recipe) {
       // title.setText(recipe.getTitle());

    }
}
