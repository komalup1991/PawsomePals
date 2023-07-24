package edu.northeastern.pawsomepals.ui.feed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecipeAdapter;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.profile.UserProfileActivity;

public class FeedAllFragment extends Fragment {
    private RecyclerView recipeRecyclerView;
    private RecipeAdapter recipeAdapter;
    private RecipeAdapter.OnItemActionListener onItemActionListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeItemActionListener();
        recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recipeRecyclerView.setLayoutManager(horizontalLayoutManager);

        recipeAdapter = new RecipeAdapter(new ArrayList<Recipe>(),new ArrayList<Users>(),onItemActionListener);
        recipeRecyclerView.setAdapter(recipeAdapter);
        fetchRecipesFromFirestore();

    }

    private void initializeItemActionListener() {
        onItemActionListener = new RecipeAdapter.OnItemActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipeId",recipe.getRecipeId());
                startActivity(intent);
            }

            @Override
            public void onUserClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra("userId",recipe.getCreatedBy());
                startActivity(intent);

            }
        };
    }

    private void fetchRecipesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("FeedAllFragment", "Error getting recipes.", error);
                        return;
                    }

                    List<Recipe> recipeList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        Log.d("yoooo", recipe.getRecipeId());
                        String userId = recipe.getCreatedBy();
                        fetchUserNameFromFirestore(userId, recipe,recipeList);
                        recipeList.add(recipe);
                    }
                    recipeAdapter.setRecipes(recipeList);
                    recipeAdapter.notifyDataSetChanged();
                });
    }


    private void fetchUserNameFromFirestore(String userId, Recipe recipe, List<Recipe> recipeList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            Users user = userDocument.toObject(Users.class);
                            recipe.setUsername(user.getName());
                            recipe.setUserProfileImage(user.getProfileImage());
                        }
                    } else {
                        Log.e("FeedAllFragment", "Error getting user's name.", task.getException());
                    }


                    recipeAdapter.notifyDataSetChanged();
                });
    }


}



