package edu.northeastern.pawsomepals.ui.feed;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.utils.TimeUtil;
import edu.northeastern.pawsomepals.adapters.RecipeAllAdapter;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.profile.UserProfileActivity;

public class FeedRecipeFragment extends Fragment {
    private RecyclerView recipeAllRecyclerView;
    private RecipeAllAdapter recipeAllAdapter;
    private RecipeAllAdapter.OnItemActionListener onItemActionListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeItemActionListener();
        recipeAllRecyclerView = view.findViewById(R.id.recipeAllRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recipeAllRecyclerView.setLayoutManager(layoutManager);
        recipeAllAdapter = new RecipeAllAdapter(new ArrayList<>(), new ArrayList<Users>(), onItemActionListener);
        recipeAllRecyclerView.setAdapter(recipeAllAdapter);
        fetchRecipesFromFirestore();
    }

    private void fetchRecipesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes").addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("FeedAllFragment", "Error getting recipes.", error);
                return;
            }
            List<Recipe> recipeList = new ArrayList<>();
            for (QueryDocumentSnapshot document : querySnapshot) {
                Recipe recipe = document.toObject(Recipe.class);
                try {
                    recipe.setCreatedAt(TimeUtil.formatTime(recipe.getCreatedAt()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                recipe.setRecipeId(document.getId());
                String userId = recipe.getCreatedBy();
                fetchUserNameFromFirestore(userId, recipe, recipeList);
                recipeList.add(recipe);
            }
            recipeAllAdapter.setRecipes(recipeList);
            recipeAllAdapter.notifyDataSetChanged();
        });
    }

    private void fetchUserNameFromFirestore(String userId, Recipe recipe, List<Recipe> recipeList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot userDocument : task.getResult()) {
                    Users user = userDocument.toObject(Users.class);
                    recipe.setUsername(user.getName());
                    recipe.setUserProfileImage(user.getProfileImage());
                }
            } else {
                Log.e("FeedAllFragment", "Error getting user's name.", task.getException());
            }


            recipeAllAdapter.notifyDataSetChanged();
        });
    }

    private void initializeItemActionListener() {
        onItemActionListener = new RecipeAllAdapter.OnItemActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId());
                startActivity(intent);
            }

            @Override
            public void onUserClick(Recipe recipe) {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("ProfileId", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileId", recipe.getCreatedBy());
                editor.apply();

                //Navigate to Profile Fragment

            }
        };
    }
}

