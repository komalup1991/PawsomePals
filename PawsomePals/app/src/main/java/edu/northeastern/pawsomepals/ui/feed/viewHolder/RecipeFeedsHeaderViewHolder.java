package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.adapters.RecipeHorizontalListAdapter;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.ui.feed.FeedCollectionType;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeFeedsHeaderViewHolder extends RecyclerView.ViewHolder {
    private final RecyclerView recipeRecyclerView;
    private final RecipeHorizontalListAdapter recipeHorizontalListAdapter;
    private List<Recipe> recipeList = new ArrayList<>();

    private List<String> userIds = new ArrayList<>();

    public RecipeFeedsHeaderViewHolder(@NonNull View itemView, List<String> userIds, OnItemActionListener onItemActionListener) {
        super(itemView);
        recipeRecyclerView = itemView.findViewById(R.id.recipeRecyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recipeRecyclerView.setLayoutManager(horizontalLayoutManager);
        recipeHorizontalListAdapter = new RecipeHorizontalListAdapter(recipeList, new ArrayList<>(), onItemActionListener);
        recipeRecyclerView.setAdapter(recipeHorizontalListAdapter);
        this.userIds = userIds;
    }

    public void bindRecylerViewData() {
        fetchRecipesFromFirestore();
    }

    private void fetchRecipesFromFirestore() {
        CollectionReference recipes = FirebaseFirestore.getInstance().collection(FeedCollectionType.RECIPES);
        FirestoreDataLoader.loadDataFromCollectionsForUserIds(new ArrayList<>() {{
            add(recipes);
        }}, userIds, new FirestoreDataLoader.FirestoreDataListener() {
            @Override
            public void onDataLoaded(List<FeedItem> feedItems) {
                recipeList.clear();
                for (FeedItem r : feedItems) {
                    recipeList.add((Recipe) r);
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        recipeHorizontalListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}