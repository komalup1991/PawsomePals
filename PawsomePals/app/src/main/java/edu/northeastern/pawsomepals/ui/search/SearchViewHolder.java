package edu.northeastern.pawsomepals.ui.search;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    public TextView title;

    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = itemView.findViewById(R.id.title);

    }

    public void bindThisData(Recipe recipe) {
        title.setText(recipe.getName());

    }
}
