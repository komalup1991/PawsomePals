package edu.northeastern.pawsomepals.ui.feed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class FeedAllFragment extends Fragment implements FirestoreDataLoader.FirestoreDataListener {
    private RecyclerView feedsRecyclerView;
    private final List<FeedItem> feedItemList = new ArrayList<>();
    private FeedAdapter feedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedsRecyclerView = view.findViewById(R.id.feedsRecyclerView);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        feedsRecyclerView.setLayoutManager(verticalLayoutManager);

        feedAdapter = new FeedAdapter(feedItemList, requireContext());
        feedsRecyclerView.setAdapter(feedAdapter);

        fetchFeeds();
    }

    private void fetchFeeds() {
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


        String orderByField = "createdAt";

        FirestoreDataLoader firestoreDataLoader = new FirestoreDataLoader(this, collections, orderByField);
        firestoreDataLoader.loadDataFromCollections();
    }

    private void updateFeedItemList(FeedItem e) {
        if (!feedItemList.contains(e) && !feedItemList.isEmpty()) {
            int index = 0;
            if (!feedItemList.isEmpty()) {
                index = 1; // account for header
            }
            feedItemList.add(index, e);
            feedAdapter.notifyItemChanged(index);
        }
    }


    @Override
    public void onDataLoaded(List<QuerySnapshot> querySnapshots) {
        feedItemList.clear();
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
                    case FeedItem.TYPE_PHOTO_VIDEO -> feedItem = document.toObject(PhotoVideo.class);
                    case FeedItem.TYPE_EVENT -> feedItem = document.toObject(Event.class);
                    case FeedItem.TYPE_POST -> feedItem = document.toObject(Post.class);
                    case FeedItem.TYPE_SERVICE -> feedItem = document.toObject(Services.class);
                }
                if (feedItem != null) {
                    try {
                        feedItem.setCreatedAt(TimeUtil.formatTime(feedItem.getCreatedAt()));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    feedItemList.add(feedItem);
                }
            }
        }


        Collections.sort(feedItemList);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                feedAdapter.notifyDataSetChanged();
            }
        });
    }


}



