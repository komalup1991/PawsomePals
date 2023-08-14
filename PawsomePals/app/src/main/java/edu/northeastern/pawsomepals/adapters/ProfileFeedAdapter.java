package edu.northeastern.pawsomepals.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.pawsomepals.ui.feed.FeedAllFragment;
import edu.northeastern.pawsomepals.ui.feed.FeedFragmentViewType;

public class ProfileFeedAdapter extends FragmentStateAdapter {
    private static final int NUM_TABS = 1;
    private final String profileId;
    private final FeedFragmentViewType viewType;

    public ProfileFeedAdapter(@NonNull FragmentActivity fragmentActivity, String profileId, FeedFragmentViewType viewType) {
        super(fragmentActivity);

        this.profileId = profileId;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle;

        if (position == 0 && viewType == FeedFragmentViewType.POST) {
            FeedAllFragment feedAllFragment = new FeedAllFragment();
            bundle = new Bundle();
            bundle.putSerializable("feed_view_type", viewType);
            bundle.putString("profileId", profileId);
            feedAllFragment.setArguments(bundle);
            return feedAllFragment;
        } else if (position == 0 && viewType == FeedFragmentViewType.FAVOURITE) {
            FeedAllFragment feedAllFragment = new FeedAllFragment();
            bundle = new Bundle();
            bundle.putSerializable("feed_view_type", viewType);
            bundle.putString("profileId", profileId);
            feedAllFragment.setArguments(bundle);
            return feedAllFragment;
        } else {
            return new FeedAllFragment();
        }

    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
