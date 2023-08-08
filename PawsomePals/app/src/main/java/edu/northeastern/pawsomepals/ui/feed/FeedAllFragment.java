package edu.northeastern.pawsomepals.ui.feed;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.map.MapFragment;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class FeedAllFragment extends Fragment {
    private RecyclerView feedsRecyclerView;
    private final List<FeedItem> feedItemList = new ArrayList<>();
    private FeedAdapter feedAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedFragmentViewType feedFragmentViewType;
    private String feedIdFromDeepLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed_all, container, false);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedFragmentViewType = (FeedFragmentViewType) requireArguments().getSerializable("feed_view_type");

        feedsRecyclerView = view.findViewById(R.id.feedsRecyclerView);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        feedsRecyclerView.setLayoutManager(verticalLayoutManager);

        feedIdFromDeepLink = getActivity().getIntent().getStringExtra("feedId");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeeds();
            }
        });

        feedAdapter = new FeedAdapter(feedItemList, requireContext(), new OnItemActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe", recipe);
                getActivity().startActivity(intent);
            }

            @Override
            public void onUserClick(String userId) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ProfileId", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileId", userId);
                editor.apply();

                //Navigate to Profile Fragment
                ProfileFragment profileFragment = new ProfileFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container_view, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onLocationClick(FeedItem feedItem) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("feedItem", feedItem);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_view, MapFragment.class, bundle, "MapFragment")
                        .commit();
            }
        });
        feedsRecyclerView.setAdapter(feedAdapter);
        fetchFeeds();
    }

    private void refreshFeeds() {
        feedItemList.clear();
        fetchFeeds();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void fetchFeeds() {
        switch (feedFragmentViewType) {
            case ALL -> {
                fetchAllFeeds();
            }
            case FRIEND -> {
                fetchFriendsFeeds();
            }
            case RECIPE -> {
                fetchRecipes();
            }
        }
    }

    private void fetchRecipes() {
        CollectionReference recipes = FirebaseFirestore.getInstance().collection("recipes");
        recipes.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Recipe e = doc.toObject(Recipe.class);
                    updateFeedItemList(e);
                }
            }
        });
        FirestoreDataLoader.loadDataFromCollections(new ArrayList<>() {{
                                                        add(recipes);
                                                    }},
                new FirestoreDataLoader.FirestoreDataListener() {
                    @Override
                    public void onDataLoaded(List<FeedItem> feedItems) {
                        notifyDatasetChange(feedItems);
                    }
                });
    }

    private void fetchFriendsFeeds() {
        FirebaseUtil.getFollowData(FirebaseAuth.getInstance().getCurrentUser().getUid(), new BaseDataCallback() {
            @Override
            public void onFollowingUserIdListReceived(List<String> followingUserIds) {
                super.onFollowingUserIdListReceived(followingUserIds);
                FirestoreDataLoader.loadDataFromCollectionsForUserIds(FirestoreDataLoader.getAllCollections(),
                        followingUserIds, new FirestoreDataLoader.FirestoreDataListener() {

                            @Override
                            public void onDataLoaded(List<FeedItem> feedItems) {
                                notifyDatasetChange(feedItems);
                            }
                        });
            }
        });
    }

    private void fetchAllFeeds() {
        if (!feedItemList.isEmpty()) {
            return;
        }
        List<CollectionReference> collections = new ArrayList<>();
        CollectionReference event = FirebaseFirestore.getInstance().collection("events");
        event.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Event e = doc.toObject(Event.class);
                    updateFeedItemList(e);
                }
            }
        });
        collections.add(event);
        CollectionReference posts = FirebaseFirestore.getInstance().collection("posts");
        posts.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Post p = doc.toObject(Post.class);
                    updateFeedItemList(p);
                }
            }
        });
        collections.add(posts);

        CollectionReference services = FirebaseFirestore.getInstance().collection("services");
        services.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Services s = doc.toObject(Services.class);
                    updateFeedItemList(s);
                }
            }
        });
        collections.add(services);

        CollectionReference photoVideo = FirebaseFirestore.getInstance().collection("photovideo");
        photoVideo.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    PhotoVideo pv = doc.toObject(PhotoVideo.class);
                    updateFeedItemList(pv);
                }
            }
        });

        collections.add(photoVideo);

        FirestoreDataLoader.loadDataFromCollections(collections,
                new FirestoreDataLoader.FirestoreDataListener() {
                    @Override
                    public void onDataLoaded(List<FeedItem> feedItems) {
                        notifyDatasetChange(feedItems);
                    }
                });
    }

    public void notifyDatasetChange(List<FeedItem> feedItems) {
        feedItemList.clear();
        if (feedFragmentViewType != FeedFragmentViewType.RECIPE) {
            feedItemList.add(new FeedItem() {
                @Override
                public int getType() {
                    return FeedItem.TYPE_RECIPE_HEADER;
                }
            });
        }
        feedItemList.addAll(feedItems);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                feedAdapter.notifyDataSetChanged();
                if (feedIdFromDeepLink != null) {
                    scrollToFeedItem(feedIdFromDeepLink);
                }
            }
        });
    }

    private void updateFeedItemList(FeedItem item) {
        if (!feedItemList.contains(item)) {
            int index = 0;
            if (!feedItemList.isEmpty()) {
                index = 1; // account for header
            }
            feedItemList.add(index, item);
            feedAdapter.notifyItemChanged(index);
        } else {
            for (FeedItem feedItem : feedItemList) {
                Log.d("feednow ", feedItem + "");
                if (Objects.equals(feedItem.getFeedItemId(), item.getFeedItemId())) {
                    feedItem.setCommentCount(item.getCommentCount());
                }
            }
            feedAdapter.notifyDataSetChanged();
        }
    }
    private void scrollToFeedItem(String feedId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i<feedItemList.size();i++) {
                    FeedItem feedItem = feedItemList.get(i);
                    if (feedId.equals(feedItem.getFeedItemId())) {
                        // Scroll to the specific feed item
                        int position = i;
                        feedsRecyclerView.scrollToPosition(position);
                        break;
                    }
                }
            }
        }, 500);
    }
}



