package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecipeHorizontalListAdapter;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.ui.feed.FeedCollectionType;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;
import edu.northeastern.pawsomepals.utils.FeedFilter;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeFeedsHeaderViewHolder extends RecyclerView.ViewHolder {
    private final RecyclerView recipeRecyclerView;
    private final RecipeHorizontalListAdapter recipeHorizontalListAdapter;
    private final TextView recipeTitle;
    private final List<Recipe> recipeList = new ArrayList<>();
    private final Spinner filterSpinner;
    private int feedFilter = FeedFilter.POPULAR;


    private List<String> userIds = new ArrayList<>();

    public RecipeFeedsHeaderViewHolder(@NonNull View itemView, List<String> userIds, OnItemActionListener onItemActionListener) {
        super(itemView);
        recipeTitle = itemView.findViewById(R.id.top_recipes_title);
        recipeRecyclerView = itemView.findViewById(R.id.recipeRecyclerView);
        filterSpinner = itemView.findViewById(R.id.filterSpinner);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recipeRecyclerView.setLayoutManager(horizontalLayoutManager);
        recipeHorizontalListAdapter = new RecipeHorizontalListAdapter(recipeList, new ArrayList<>(), onItemActionListener);
        recipeRecyclerView.setAdapter(recipeHorizontalListAdapter);
        this.userIds = userIds;

        setupFeedFilterSpinner(itemView.getContext(), onItemActionListener);
    }

    private void setupFeedFilterSpinner(Context context, OnItemActionListener onItemActionListener) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.filter_options_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onItemActionListener.onFeedFilterSpinnerClick(i);
                feedFilter = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void bindRecylerViewData() {
        fetchRecipesFromFirestore();
    }

    private void fetchRecipesFromFirestore() {
        CollectionReference recipes = FirebaseFirestore.getInstance().collection(FeedCollectionType.RECIPES);
        FirestoreDataLoader.loadDataFromCollectionsForUserIds(new ArrayList<>() {{
            add(recipes);
        }}, userIds, feedFilter, new FirestoreDataLoader.FirestoreDataListener() {
            @Override
            public void onDataLoaded(List<FeedItem> feedItems) {
                recipeList.clear();
                for (FeedItem r : feedItems) {
                    recipeList.add((Recipe) r);
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (recipeList.isEmpty()) {
                            recipeTitle.setVisibility(View.GONE);
                        } else {
                            recipeTitle.setVisibility(View.VISIBLE);
                        }
                        recipeHorizontalListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}