package edu.northeastern.pawsomepals.ui.feed;

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
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.map.MapFragment;
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

        feedAdapter = new FeedAdapter(feedItemList, requireContext(), new FeedAdapter.LocationClickListener() {
            @Override
            public void onClick(FeedItem feedItem) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("feedItem", feedItem);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_view, MapFragment.class,bundle, "MapFragment")
                        .commit();
            }
        });
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
                    Log.d("yoo", "s item id " + s.getFeedItemId());
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
                    Log.d("yoo", "pv item id " + pv.getFeedItemId());
                    updateFeedItemList(pv);
                }
            }
        });
        collections.add(photoVideo);


        String orderByField = "createdAt";

        FirestoreDataLoader firestoreDataLoader = new FirestoreDataLoader(this, collections, orderByField);
        firestoreDataLoader.loadDataFromCollections();
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
                Log.d("feednow ",feedItem + "");
                if (Objects.equals(feedItem.getFeedItemId(), item.getFeedItemId())) {
                    feedItem.setCommentCount(item.getCommentCount());
                }
            }
            feedAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onDataLoaded(List<FeedItem> feedItems) {
        this.feedItemList.clear();
        this.feedItemList.addAll(feedItems);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                feedAdapter.notifyDataSetChanged();
            }
        });
    }
}



