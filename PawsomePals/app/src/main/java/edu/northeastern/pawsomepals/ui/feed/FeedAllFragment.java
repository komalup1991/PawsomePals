package edu.northeastern.pawsomepals.ui.feed;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.map.MapFragment;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
import edu.northeastern.pawsomepals.utils.ActivityHelper;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class FeedAllFragment extends Fragment implements ActivityResultCallback<ActivityResult> {
    private RecyclerView feedsRecyclerView;
    private final List<FeedItem> feedItemList = new ArrayList<>();
    private final List<Users> userList = new ArrayList<>();
    private FeedAdapter feedAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedFragmentViewType feedFragmentViewType;
    private String feedIdFromDeepLink;
    private TextView pullToRefreshTextView;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed_all, container, false);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this // Use the current fragment as the callback
        );
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedFragmentViewType = (FeedFragmentViewType) requireArguments().getSerializable("feed_view_type");

        feedsRecyclerView = view.findViewById(R.id.feedsRecyclerView);
        pullToRefreshTextView = view.findViewById(R.id.pullToRefreshTextView);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        feedsRecyclerView.setLayoutManager(verticalLayoutManager);

        feedIdFromDeepLink = getActivity().getIntent().getStringExtra("feedId");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeeds();
                pullToRefreshTextView.setVisibility(View.GONE);
            }
        });

        List<String> usersIds = new ArrayList<>();
        if (feedFragmentViewType == FeedFragmentViewType.FRIEND) {
            FirebaseUtil.getFollowData(FirebaseAuth.getInstance().getCurrentUser().getUid(), new BaseDataCallback() {
                @Override
                public void onFollowingUserIdListReceived(List<String> followingUserIds) {
                    feedAdapter.setUserIds(followingUserIds);
                }
            });
        }
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
        Log.d("yoo", "yooooooo = " + feedItemList.size());
        fetchFeeds();
        addCollectionListeners();
    }

    private void refreshFeeds() {
        feedItemList.clear();
        fetchFeeds();
        swipeRefreshLayout.setRefreshing(false);
        pullToRefreshTextView.setVisibility(View.GONE);
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
            case POST -> {
                fetchPosts();
            }
            case FAVOURITE -> {
                fetchFavourites();
            }
        }
    }

    private void fetchPosts() {
        List<String> userIds = new ArrayList<>();

        String profileId = getArguments().getString("profileId");
        userIds.add(profileId);

        CollectionReference posts = FirebaseFirestore.getInstance().collection(FeedCollectionType.POSTS);
        //TODO check this listener
//        posts.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                for (DocumentChange doc : value.getDocumentChanges()) {
//                    Post e = doc.getDocument().toObject(Post.class);
//                    if (feedFragmentViewType == FeedFragmentViewType.RECIPE) {
//                        updateFeedItemList(e);
//                    }
//                }
//            }
//        });
        FirestoreDataLoader.loadDataFromCollectionsForUserIds(new ArrayList<>() {{
                                                                  add(posts);
                                                              }},
                userIds, new FirestoreDataLoader.FirestoreDataListener() {

                    @Override
                    public void onDataLoaded(List<FeedItem> feedItems) {
                        notifyDatasetChange(feedItems);
                    }
                });

    }

    public void fetchFavourites() {
        if (!feedItemList.isEmpty()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> feedIds = new ArrayList<>(FirestoreDataLoader.fetchUserFavFeedIds());
                if (!(feedIds.size() == 0))
                {
                    FirestoreDataLoader.loadDataFromCollectionsForFeedIds(FirestoreDataLoader.getAllCollections(), feedIds,
                            new FirestoreDataLoader.FirestoreDataListener() {
                                @Override
                                public void onDataLoaded(List<FeedItem> feedItems) {
                                    notifyDatasetChange(feedItems);
                                }
                            });
            }
                else {
                    return;
                }

            }
        }).start();


    }


    private void fetchRecipes() {
        CollectionReference recipes = FirebaseFirestore.getInstance().collection(FeedCollectionType.RECIPES);
        recipes.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange doc : value.getDocumentChanges()) {
                    Recipe e = doc.getDocument().toObject(Recipe.class);
                    if (feedFragmentViewType == FeedFragmentViewType.RECIPE) {
                        updateFeedItemList(e);
                    }
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
        FirestoreDataLoader.loadDataFromCollections(FirestoreDataLoader.getAllCollections(),
                new FirestoreDataLoader.FirestoreDataListener() {
                    @Override
                    public void onDataLoaded(List<FeedItem> feedItems) {
                        notifyDatasetChange(feedItems);
                    }
                });
    }

    private void addCollectionListeners() {
        for (CollectionReference collectionReference : FirestoreDataLoader.getAllCollectionsWithRecipes()) {
            collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (feedFragmentViewType != FeedFragmentViewType.RECIPE && feedFragmentViewType != FeedFragmentViewType.POST
                            && feedFragmentViewType != FeedFragmentViewType.FAVOURITE) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            DocumentSnapshot d = doc.getDocument();
                            int type = Integer.parseInt(Objects.requireNonNull(doc.getDocument().get("type")).toString());
                            switch (type) {
                                case FeedItem.TYPE_EVENT ->
                                        updateFeedItemList(d.toObject(Event.class));
                                case FeedItem.TYPE_SERVICE ->
                                        updateFeedItemList(d.toObject(Services.class));
                                case FeedItem.TYPE_POST ->
                                        updateFeedItemList(d.toObject(Post.class));
                                case FeedItem.TYPE_PHOTO_VIDEO ->
                                        updateFeedItemList(d.toObject(PhotoVideo.class));
                            }
                        }
                    }
                }
            });
        }
    }

    public void notifyDatasetChange(List<FeedItem> feedItems) {
        feedItemList.clear();
        if (feedFragmentViewType != FeedFragmentViewType.RECIPE && feedFragmentViewType != FeedFragmentViewType.POST && feedFragmentViewType != FeedFragmentViewType.FAVOURITE) {
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
//            int index = 0;
//            if (!feedItemList.isEmpty()) {
//                index = 1; // account for header
//            }
//            feedItemList.add(index, item);
//            feedAdapter.notifyItemChanged(index);
//            Log.d("yoo", "item = " + item.getFeedItemId());
        } else {
            for (int i = 0; i < feedItemList.size(); i++) {
                if (Objects.equals(feedItemList.get(i).getFeedItemId(), item.getFeedItemId())) {
                    if (feedItemList.get(i).getCommentCount() != item.getCommentCount()) {
                        feedItemList.get(i).setCommentCount(item.getCommentCount());
                        Log.d("yoo", "item else = " + item.getFeedItemId());
                        feedAdapter.notifyItemChanged(i);
                    }
                    break;
                }
            }
        }
    }

//    private void updateFeedItemListOnActivitySuccess(FeedItem item) {
//
//        if (!feedItemList.contains(item)) {
//            int index = 0;
//            if (!feedItemList.isEmpty()) {
//                index = 1; // account for header
//            }
//            feedItemList.add(index, item);
//            feedAdapter.notifyItemChanged(index);
//            Log.d("yoo", "item = " + item.getFeedItemId());
//        }}

    private void scrollToFeedItem(String feedId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < feedItemList.size(); i++) {
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

//    void function() {
//        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        if (result.getResultCode() == Activity.RESULT_OK) {
//                            // There are no request codes
//                            Intent data = result.getData();
//
//                        }
//                    }
//                });
//    }


    @Override
    public void onActivityResult(ActivityResult result) {
        int resultCode = result.getResultCode();
        Log.d("yoo","result.getResultCode() "+result.getResultCode());
        if (resultCode == Activity.RESULT_OK) {
            int creationStatus = result.getData().getIntExtra(ActivityHelper.CREATION_STATUS, 0);
            if (creationStatus == ActivityHelper.SUCCESS_CODE) {
                Log.d("yoo","creationStatus "+creationStatus);
                Log.d("yoo","ActivityHelper.SUCCESS_CODE "+ActivityHelper.SUCCESS_CODE);
                // Handle successful post creation
                // For example, you can refresh the feed
                refreshFeeds();
             //   updateFeedItemListOnActivitySuccess (item);
            }
    }}
}



