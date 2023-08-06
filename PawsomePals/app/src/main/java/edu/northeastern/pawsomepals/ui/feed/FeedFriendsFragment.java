package edu.northeastern.pawsomepals.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class FeedFriendsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUtil.getFollowData(FirebaseAuth.getInstance().getCurrentUser().getUid(), new BaseDataCallback() {
            @Override
            public void onFollowingUserIdListReceived(List<String> followingUserIds) {
                super.onFollowingUserIdListReceived(followingUserIds);
                FirestoreDataLoader.loadDataFromCollectionsForUserIds(FirestoreDataLoader.getAllCollections(),
                        followingUserIds, new FirestoreDataLoader.FirestoreDataListener() {

                            @Override
                            public void onDataLoaded(List<FeedItem> feedItems) {

                            }
                        });
            }
        });
    }
}

