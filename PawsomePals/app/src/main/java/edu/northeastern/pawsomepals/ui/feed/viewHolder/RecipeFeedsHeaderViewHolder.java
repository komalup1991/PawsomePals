package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecipeHorizontalListAdapter;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeFeedsHeaderViewHolder extends RecyclerView.ViewHolder {
    private final RecyclerView recipeRecyclerView;
    private final RecipeHorizontalListAdapter recipeHorizontalListAdapter;
    private List<Recipe> recipeList = new ArrayList<>();

    public RecipeFeedsHeaderViewHolder(@NonNull View itemView, OnItemActionListener onItemActionListener) {
        super(itemView);
        recipeRecyclerView = itemView.findViewById(R.id.recipeRecyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recipeRecyclerView.setLayoutManager(horizontalLayoutManager);
        recipeHorizontalListAdapter = new RecipeHorizontalListAdapter(recipeList, new ArrayList<>(), onItemActionListener);
        recipeRecyclerView.setAdapter(recipeHorizontalListAdapter);
    }

    public void bindRecylerViewData() {
        fetchRecipesFromFirestore();
    }

    private void fetchRecipesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUtil.getFollowData(FirebaseAuth.getInstance().getCurrentUser().getUid(), new BaseDataCallback() {
            @Override
            public void onFollowingUserIdListReceived(List<String> followingUserIds) {
                super.onFollowingUserIdListReceived(followingUserIds);
            }
        });
        db.collection("recipes").addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("FeedAllFragment", "Error getting recipes.", error);
                return;
            }

            recipeList.clear();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Recipe recipe = document.toObject(Recipe.class);
                String userId = recipe.getCreatedBy();
                getUsernameByUserId(userId, recipe, recipeList);
                recipeList.add(recipe);
            }
            recipeHorizontalListAdapter.notifyDataSetChanged();
        });
    }

    public void getUsernameByUserId(String userId, Recipe recipe, List<Recipe> recipeList) {
        FirebaseUtil.fetchUserInfoFromFirestore(userId, new BaseDataCallback() {
                    @Override
                    public void onUserReceived(Users user) {
                        recipe.setUsername(user.getName());
                        recipe.setUserProfileImage(user.getProfileImage());
                        recipeHorizontalListAdapter.notifyDataSetChanged();
                    }
                }
        );
    }

}