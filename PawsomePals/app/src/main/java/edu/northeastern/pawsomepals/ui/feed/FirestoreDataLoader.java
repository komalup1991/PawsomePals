package edu.northeastern.pawsomepals.ui.feed;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.Favorite;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Like;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class FirestoreDataLoader {

    private final FirestoreDataListener listener;
    private final FirebaseFirestore db;
    private final List<CollectionReference> collections;
    private final String orderByField;

    public FirestoreDataLoader(FirestoreDataListener listener, List<CollectionReference> collections, String orderByField) {
        this.listener = listener;
        this.collections = collections;
        db = FirebaseFirestore.getInstance();
        this.orderByField = orderByField;
    }

    public void loadDataFromCollections() {
        loadDataFromCollections(new ArrayList<>());
    }

    public void loadDataFromCollections(List<String> feedItemIds) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Set<String> list = fetchUserFavFeedIds();
                Set<String> likeList = fetchUserLikeFeedIds();
                List<QuerySnapshot> querySnapshots = new ArrayList<>();
                try {
                    for (CollectionReference collection : collections) {
                        Query query = null;
                        if (!feedItemIds.isEmpty()) {
                            collection.whereIn("feedItemId", feedItemIds)
                                    .orderBy(orderByField, Query.Direction.DESCENDING);
                        } else {
                            collection.orderBy(orderByField, Query.Direction.DESCENDING);
                        }
                        Task<QuerySnapshot> task = query.get();
                        Tasks.await(task);
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                querySnapshots.add(querySnapshot);
                            }
                        } else {
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listener != null) {
                    listener.onDataLoaded(process(querySnapshots, list, likeList));
                }
            }
        }).start();
    }

    private Set<String> fetchUserLikeFeedIds() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> taskLikes = db.collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("likes")
                .get();
        Set<String> likeFeedIds = new HashSet<>();
        try {
            Tasks.await(taskLikes);
            if (taskLikes.isSuccessful()) {
                QuerySnapshot querySnapshot = taskLikes.getResult();
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    likeFeedIds.add(document.toObject(Like.class).getFeedItemId());
                }

            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return likeFeedIds;
    }

    private List<FeedItem> process(List<QuerySnapshot> querySnapshots, Set<String> favoriteFeedIds, Set<String> likeFeedIds) {
        List<FeedItem> feedItemList = new ArrayList<>();
        feedItemList.add(new FeedItem() {
            @Override
            public int getType() {
                return FeedItem.TYPE_RECIPE_HEADER;
            }
        });
        for (QuerySnapshot querySnapshot : querySnapshots) {
            for (QueryDocumentSnapshot document : querySnapshot) {
                int type = Math.toIntExact((Long) document.getData().get("type"));
                FeedItem feedItem = null;
                switch (type) {
                    case FeedItem.TYPE_PHOTO_VIDEO ->
                            feedItem = document.toObject(PhotoVideo.class);
                    case FeedItem.TYPE_EVENT -> feedItem = document.toObject(Event.class);
                    case FeedItem.TYPE_POST -> feedItem = document.toObject(Post.class);
                    case FeedItem.TYPE_SERVICE -> feedItem = document.toObject(Services.class);
                }

                if (feedItem != null) {
                    if (favoriteFeedIds.contains(feedItem.getFeedItemId())) {
                        feedItem.setFavorite(true);
                    }
                    if (likeFeedIds.contains(feedItem.getFeedItemId())) {
                        feedItem.setLiked(true);
                    }
//                    try {
//                        feedItem.setDisplayTime(TimeUtil.formatTime(feedItem.getCreatedAt()));
//                    } catch (ParseException e) {
//                        throw new RuntimeException(e);
//                    }
                    feedItemList.add(feedItem);
                }
            }
        }


        Collections.sort(feedItemList, new Comparator<FeedItem>() {
            @Override
            public int compare(FeedItem feedItem, FeedItem feedItem2) {
                if (feedItem.getType() == FeedItem.TYPE_RECIPE_HEADER || feedItem2.getType() == FeedItem.TYPE_RECIPE_HEADER) {
                    return 1;
                }
                return convertStringToDate(feedItem2.getCreatedAt()).compareTo(convertStringToDate(feedItem.getCreatedAt()));
            }
        });
        return feedItemList;
    }

    private Set<String> fetchUserFavFeedIds() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> taskFavorites = db.collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("favorites")
                .get();
        Set<String> favoriteFeedIds = new HashSet<>();
        try {
            Tasks.await(taskFavorites);
            if (taskFavorites.isSuccessful()) {
                QuerySnapshot querySnapshot = taskFavorites.getResult();
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    favoriteFeedIds.add(document.toObject(Favorite.class).getFeedItemId());
                }

            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return favoriteFeedIds;
    }

    private Date convertStringToDate(String createdAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            return sdf.parse(createdAt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public interface FirestoreDataListener {
        void onDataLoaded(List<FeedItem> feedItems);
    }
}
