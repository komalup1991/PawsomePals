package edu.northeastern.pawsomepals.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.pawsomepals.ui.feed.FeedAllFragment;
import edu.northeastern.pawsomepals.ui.feed.FeedFriendsFragment;
import edu.northeastern.pawsomepals.ui.feed.FeedRecipeFragment;

public class FragmentAdapter extends FragmentStateAdapter {
    private static final int NUM_TABS = 3;
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create and return the appropriate fragment based on the position
        return switch (position) {
            case 0 -> new FeedAllFragment();
            case 1 -> new FeedFriendsFragment();
            case 2 -> new FeedRecipeFragment();
            default -> new FeedAllFragment(); // Return a default fragment if needed
        };
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
