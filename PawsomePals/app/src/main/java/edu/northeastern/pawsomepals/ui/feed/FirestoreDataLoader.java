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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.Favorite;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Like;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class FirestoreDataLoader {

    public static List<CollectionReference> getAllCollections() {
        List<CollectionReference> collections = new ArrayList<>();
        collections.add(FirebaseFirestore.getInstance().collection("events"));
        collections.add(FirebaseFirestore.getInstance().collection("posts"));
        collections.add(FirebaseFirestore.getInstance().collection("services"));
        collections.add(FirebaseFirestore.getInstance().collection("photovideo"));
        return collections;
    }

    public static List<CollectionReference> getAllCollectionsWithRecipes() {
        List<CollectionReference> collections = new ArrayList<>();
        collections.add(FirebaseFirestore.getInstance().collection("events"));
        collections.add(FirebaseFirestore.getInstance().collection("posts"));
        collections.add(FirebaseFirestore.getInstance().collection("services"));
        collections.add(FirebaseFirestore.getInstance().collection("photovideo"));
        collections.add(FirebaseFirestore.getInstance().collection("recipes"));
        return collections;
    }

    public static void loadDataFromCollections(List<CollectionReference> collections, FirestoreDataListener firestoreDataListener) {
        loadDataFromCollections(new ArrayList<>(), new ArrayList<>(), collections, firestoreDataListener);
    }

    public static void loadDataFromCollectionsForUserIds(List<CollectionReference> collections, List<String> userIds, FirestoreDataListener firestoreDataListener) {
        loadDataFromCollections(new ArrayList<>(), userIds, collections, firestoreDataListener);
    }

    public static void loadDataFromCollectionsForFeedIds(List<CollectionReference> collections, List<String> feedIds, FirestoreDataListener firestoreDataListener) {
        loadDataFromCollections(feedIds, new ArrayList<>(), collections, firestoreDataListener);
    }


    /**
     * Usage:-
     * This will fetch only post from two users and with specific postId.
     * firestoreDataLoader.loadDataFromCollections(new ArrayList<>() {{
     * add("ff18eda6-a984-4ffa-be4b-62dd7b9bc816"); // feedIds
     * add("98420a72-f7ce-4d15-84a1-b0e13a8469e2");
     * }}, new ArrayList<>() {{
     * add("wGBHhmQlPfVK0BhCLjsFpBeGW6W2"); // userIds
     * add("yBpKl27Heofi17xefV3B7V1q6bN2");
     * }});
     *
     * @param feedItemIds
     * @param userIds
     */
    public static void loadDataFromCollections(List<String> feedItemIds, List<String> userIds,
                                               List<CollectionReference> collections,
                                               FirestoreDataListener firestoreDataListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Set<String> list = fetchUserFavFeedIds();
                Set<String> likeList = fetchUserLikeFeedIds();
                List<QuerySnapshot> querySnapshots = new ArrayList<>();
                try {
                    for (CollectionReference collection : collections) {
                        Query query;
                        Task<QuerySnapshot> task;
                        if (!feedItemIds.isEmpty() && !userIds.isEmpty()) {
                            query = collection.whereIn("feedItemId", feedItemIds)
                                    .whereIn("createdBy", userIds);
                            task = query.get();
                        } else if (!feedItemIds.isEmpty()) {
                            query = collection.whereIn("feedItemId", feedItemIds);
                            task = query.get();
                        } else if (!userIds.isEmpty()) {
                            query = collection.whereIn("createdBy", userIds);
                            task = query.get();
                        } else {
                            task = collection.get();
                        }
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

                if (firestoreDataListener != null) {
                    firestoreDataListener.onDataLoaded(process(querySnapshots, list, likeList));
                }
            }
        }).start();
    }

    private static Set<String> fetchUserLikeFeedIds() {
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

    private static List<FeedItem> process(List<QuerySnapshot> querySnapshots, Set<String> favoriteFeedIds, Set<String> likeFeedIds) {
        List<FeedItem> feedItemList = new ArrayList<>();
        Map<String, Users> users = new HashMap<>();
        Set<String> userIds = new HashSet<>();
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
                    case FeedItem.TYPE_RECIPE -> feedItem = document.toObject(Recipe.class);
                }

                if (feedItem != null) {
                    if (feedItem.getFeedItemId() != null) {
                        if (favoriteFeedIds.contains(feedItem.getFeedItemId())) {
                            feedItem.setFavorite(true);
                        }
                        if (likeFeedIds.contains(feedItem.getFeedItemId())) {
                            feedItem.setLiked(true);
                        }
                    }
                    if (feedItem.getFeedItemId() != null && likeFeedIds.contains(feedItem.getFeedItemId())) {
                        feedItem.setLiked(true);
                    }

                    userIds.add(feedItem.getCreatedBy());
                    feedItemList.add(feedItem);
                }
            }
        }

        List<String> userList = new ArrayList<>();
        userList.addAll(userIds);
        users = FirebaseUtil.fetchUserInfoFromFirestoreBlocking(userList);
        for (FeedItem feedItem : feedItemList) {
            Users u = users.get(feedItem.getCreatedBy());
            if (u != null) {
                feedItem.setUsername(u.getName());
                feedItem.setUserProfileImage(u.getProfileImage());
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

    public static Set<String> fetchUserFavFeedIds() {
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

    private static List<String> fetchUserFavFeedIds(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> taskFavorites = db.collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("favorites")
                .get();
        List<String> favoriteFeedIds = new ArrayList<>();
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

    private static Date convertStringToDate(String createdAt) {
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
