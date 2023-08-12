package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeHorizontalFeedViewHolder extends RecyclerView.ViewHolder {
    ImageView recipeImage;
    TextView username;
    ImageView userProfilePic;
    TextView recipeName;


    public RecipeHorizontalFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        recipeImage = itemView.findViewById(R.id.recipeImage);
        userProfilePic = itemView.findViewById(R.id.userProfilePic);
        recipeName = itemView.findViewById(R.id.recipeName);
        username = itemView.findViewById(R.id.username);
    }

    public void bindData(Recipe recipe, OnItemActionListener onItemActionListener) {
        recipeName.setText(recipe.getTitle());
        username.setText(recipe.getUsername());
        Glide.with(itemView.getContext())
                .load(recipe.getImg())
                .transform(new CenterCrop(), new RoundedCorners(23))
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
                onItemActionListener.onUserClick(recipe.getCreatedBy());
            }
        });
        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipe.getCreatedBy());
            }
        });
    }
}
