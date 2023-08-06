package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecipeAdapter;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.CreateEventsActivity;
import edu.northeastern.pawsomepals.ui.feed.RecipeDetailActivity;
import edu.northeastern.pawsomepals.ui.profile.UserProfileActivity;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeRecyclerViewHolder extends RecyclerView.ViewHolder   {
    private final RecyclerView recipeRecyclerView;
    private final RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private Context context;

    private OnItemActionListener onItemActionListener = new OnItemActionListener() {

        @Override
        public void onRecipeClick(Recipe recipe) {
            Intent intent = new Intent(itemView.getContext(), RecipeDetailActivity.class);
            //   intent.putExtra("user", (CharSequence) user);
            intent.putExtra("recipe",recipe);
            //    intent.putExtra("user", (Serializable) user);
            itemView.getContext().startActivity(intent);

        }

        @Override
        public void onUserClick(Recipe recipe) {
            Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
            intent.putExtra("userId", recipe.getCreatedBy());
            itemView.getContext().startActivity(intent);

        }

        @Override
        public void onLikeClick(FeedItem feedItem) {

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
//                recipe.setRecipeId(document.getId());
                String userId = recipe.getCreatedBy();
                getUsernameByUserId(userId, recipe, recipeList);
                recipeList.add(recipe);
            }
            recipeAdapter.notifyDataSetChanged();
        });
    }
    public void getUsernameByUserId(String userId, Recipe recipe, List<Recipe> recipeList) {
        FirebaseUtil.fetchUserInfoFromFirestore( userId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                recipe.setUsername(user.getName());
                recipe.setUserProfileImage(user.getProfileImage());
                recipeAdapter.notifyDataSetChanged();
            } }
                );
    }

}