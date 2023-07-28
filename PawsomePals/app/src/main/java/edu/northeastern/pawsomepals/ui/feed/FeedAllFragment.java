package edu.northeastern.pawsomepals.ui.feed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.adapters.RecipeAdapter;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.profile.UserProfileActivity;

public class FeedAllFragment extends Fragment implements FirestoreDataLoader.FirestoreDataListener {
    private RecyclerView recipeRecyclerView;
    private RecyclerView feedsRecyclerView;
    private RecipeAdapter recipeAdapter;

    private final List<FeedItem> feedItemList = new ArrayList<>();
    private RecipeAdapter.OnItemActionListener onItemActionListener;
    private FeedAdapter feedAdapter;

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
        recipeAdapter = new RecipeAdapter(new ArrayList<>(), new ArrayList<Users>(), onItemActionListener);
        recipeRecyclerView.setAdapter(recipeAdapter);

//        feedsRecyclerView = view.findViewById(R.id.feedsRecyclerView);
//        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
//        feedsRecyclerView.setLayoutManager(verticalLayoutManager);
//        feedAdapter = new FeedAdapter(feedItemList, requireContext());
//        feedsRecyclerView.setAdapter(feedAdapter);

        fetchRecipesFromFirestore();

        List<CollectionReference> collections = new ArrayList<>();

        collections.add(FirebaseFirestore.getInstance().collection("events"));
        collections.add(FirebaseFirestore.getInstance().collection("posts"));
        collections.add(FirebaseFirestore.getInstance().collection("services"));
        collections.add(FirebaseFirestore.getInstance().collection("photoVideoPosts"));

        String orderByField = "createdAt";

        FirestoreDataLoader firestoreDataLoader = new FirestoreDataLoader(this, collections, orderByField);
        firestoreDataLoader.loadDataFromCollections();

    }

    private void initializeItemActionListener() {
        onItemActionListener = new RecipeAdapter.OnItemActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId());
                startActivity(intent);
            }

            @Override
            public void onUserClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra("userId", recipe.getCreatedBy());
                startActivity(intent);

            }
        };
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
                recipe.setRecipeId(document.getId());
                String userId = recipe.getCreatedBy();
                fetchUserNameFromFirestore(userId, recipe, recipeList);
                recipeList.add(recipe);
            }
            recipeAdapter.setRecipes(recipeList);
            recipeAdapter.notifyDataSetChanged();
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


            recipeAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDataLoaded(List<QuerySnapshot> querySnapshots) {

        for (QuerySnapshot querySnapshot : querySnapshots) {
            for (QueryDocumentSnapshot document : querySnapshot) {
                int type = Math.toIntExact((Long) document.getData().get("type"));
                FeedItem feedItem = null;
                switch (type) {
                    case FeedItem.TYPE_PHOTO_VIDEO -> feedItem = document.toObject(PhotoVideo.class);
                    case FeedItem.TYPE_EVENT -> feedItem = document.toObject(Event.class);
                    case FeedItem.TYPE_POST -> feedItem = document.toObject(Post.class);
                    case FeedItem.TYPE_SERVICE -> feedItem = document.toObject(Services.class);
                }
                if (feedItem != null) {
                    feedItemList.add(feedItem);
                }
            }
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
           //     feedAdapter.notifyDataSetChanged();
            }
        });
    }
}



