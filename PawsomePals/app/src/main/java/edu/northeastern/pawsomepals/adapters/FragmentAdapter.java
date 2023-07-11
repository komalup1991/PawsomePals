package edu.northeastern.pawsomepals.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.pawsomepals.ui.FeedAllFragment;
import edu.northeastern.pawsomepals.ui.FeedFriendsFragment;
import edu.northeastern.pawsomepals.ui.FeedRecipeFragment;

public class FragmentAdapter extends FragmentStateAdapter {
    private static final int NUM_TABS = 3;
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create and return the appropriate fragment based on the position
        switch (position) {
            case 0:
                return new FeedAllFragment();
            case 1:
                return new FeedFriendsFragment();
            case 2:
                return new FeedRecipeFragment();
            default:
                return new FeedAllFragment(); // Return a default fragment if needed
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
