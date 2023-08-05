package edu.northeastern.pawsomepals.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileRecipeAdapter;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.models.Recipe;

public class RecipesFragment extends Fragment {

    private RecyclerView recyclerViewRecipes;
    private ProfileRecipeAdapter profileRecipeAdapter;
    private final List<Recipe> recipes = new ArrayList<>();
    private TextView textNoRecipeProfiles;
    public RecipesFragment() {
        // Required empty public constructor
    }


    public static RecipesFragment newInstance(String profileId) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        args.putString("profile_id", profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewRecipes = view.findViewById(R.id.recyclerView);
        textNoRecipeProfiles = view.findViewById(R.id.textViewEmptyList);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerViewRecipes.setLayoutManager(layoutManager);

        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("profile_id");
            profileRecipeAdapter = new ProfileRecipeAdapter(recipes, requireContext());

            recyclerViewRecipes.setAdapter(profileRecipeAdapter);

            fetchRecipes(userId);

            // Show/hide the TextView based on the availability of dog profiles
            if (recipes != null && !recipes.isEmpty()) {
                recyclerViewRecipes.setVisibility(View.VISIBLE);
                textNoRecipeProfiles.setVisibility(View.GONE);
            } else {
                recyclerViewRecipes.setVisibility(View.GONE);
                textNoRecipeProfiles.setText("No recipes available");
                textNoRecipeProfiles.setVisibility(View.VISIBLE);
            }
        } else {
            // Handle the case when the arguments are null or not available
            recyclerViewRecipes.setVisibility(View.GONE);
            textNoRecipeProfiles.setText("No recipes available");
            textNoRecipeProfiles.setVisibility(View.VISIBLE);
        }


    }

    private void fetchRecipes(String userIdValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes")
                .whereEqualTo("createdBy", userIdValue)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Fetch Recipes", "Error fetching recipes", error);
                        return;
                    }
                    List<Recipe> userRecipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Recipe userRecipe = document.toObject(Recipe.class);
                        userRecipe.setRecipeId(document.getId());
                        userRecipes.add(userRecipe);
                    }
                    profileRecipeAdapter.setRecipes(userRecipes);
                    if (userRecipes != null && !userRecipes.isEmpty() && !(userRecipes.size()==0)) {
                        recyclerViewRecipes.setVisibility(View.VISIBLE);
                        textNoRecipeProfiles.setVisibility(View.GONE);
                    } else {
                        recyclerViewRecipes.setVisibility(View.GONE);
                        textNoRecipeProfiles.setText("No recipes available");
                        textNoRecipeProfiles.setVisibility(View.VISIBLE);
                    }
                    profileRecipeAdapter.notifyDataSetChanged();
                });
    }
}