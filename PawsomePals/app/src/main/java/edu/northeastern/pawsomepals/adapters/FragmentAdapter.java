package edu.northeastern.pawsomepals.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.pawsomepals.ui.feed.FeedAllFragment;
import edu.northeastern.pawsomepals.ui.feed.FeedFragmentViewType;

public class FragmentAdapter extends FragmentStateAdapter {
    private static final int NUM_TABS = 3;
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle;
        switch (position) {
            case 0:
                FeedAllFragment feedAllFragment = new FeedAllFragment();
                bundle = new Bundle();
                bundle.putSerializable("feed_view_type", FeedFragmentViewType.ALL);
                feedAllFragment.setArguments(bundle);
                return feedAllFragment;
            case 1:
                FeedAllFragment feedFriendFragment = new FeedAllFragment();
                bundle = new Bundle();
                bundle.putSerializable("feed_view_type", FeedFragmentViewType.FRIEND);
                feedFriendFragment.setArguments(bundle);
                return feedFriendFragment;
            case 2:
                FeedAllFragment feedRecipeFragment = new FeedAllFragment();
                bundle = new Bundle();
                bundle.putSerializable("feed_view_type", FeedFragmentViewType.RECIPE);
                feedRecipeFragment.setArguments(bundle);
                return feedRecipeFragment;
            default:
                return new FeedAllFragment(); // Return a default fragment if needed
        }

    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
