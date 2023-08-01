package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecipeAdapter;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.RecipeDetailActivity;
import edu.northeastern.pawsomepals.ui.profile.UserProfileActivity;

public class RecipeRecyclerViewHolder extends RecyclerView.ViewHolder {
    private final RecyclerView recipeRecyclerView;
    private final RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();

    private RecipeAdapter.OnItemActionListener onItemActionListener = new RecipeAdapter.OnItemActionListener() {
        @Override
        public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(itemView.getContext(), RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId());
                itemView.getContext().startActivity(intent);
        }

        @Override
        public void onUserClick(Recipe recipe) {
            Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
            intent.putExtra("userId", recipe.getCreatedBy());
            itemView.getContext().startActivity(intent);

        }
    };

    public RecipeRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        recipeRecyclerView = itemView.findViewById(R.id.recipeRecyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recipeRecyclerView.setLayoutManager(horizontalLayoutManager);
        recipeAdapter = new RecipeAdapter(recipeList, new ArrayList<>(), onItemActionListener);
        recipeRecyclerView.setAdapter(recipeAdapter);
    }

    public void bindRecylerViewData() {
        fetchRecipesFromFirestore();
    }

    private void fetchRecipesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes").addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("FeedAllFragment", "Error getting recipes.", error);
                return;
            }

            recipeList.clear();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Recipe recipe = document.toObject(Recipe.class);
                recipe.setRecipeId(document.getId());
                String userId = recipe.getCreatedBy();
                getUsernameByUserId(userId, recipe, recipeList);
                recipeList.add(recipe);
            }
            recipeAdapter.notifyDataSetChanged();
        });
    }


    public void getUsernameByUserId(String userId, Recipe recipe, List<Recipe> recipeList) {
        FirebaseFirestore.getInstance().collection("user")
                .whereEqualTo("userId", userId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Users user = task.getResult().getDocuments().get(0).toObject(Users.class);
                            recipe.setUsername(user.getName());
                            recipe.setUserProfileImage(user.getProfileImage());
                        }
                        recipeAdapter.notifyDataSetChanged();
                    }
                });
    }
}