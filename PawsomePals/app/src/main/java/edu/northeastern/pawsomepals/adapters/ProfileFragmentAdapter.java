package edu.northeastern.pawsomepals.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.pawsomepals.ui.profile.DogsFragment;
import edu.northeastern.pawsomepals.ui.profile.PhotosFragment;
import edu.northeastern.pawsomepals.ui.profile.RecipesFragment;

public class ProfileFragmentAdapter extends FragmentStateAdapter {
    private static final int NUM_TABS = 3;

    private final String profileId;
    private final Boolean isUserProfile;

    public ProfileFragmentAdapter(@NonNull FragmentActivity fragmentActivity, String profileId, Boolean isUserProfile) {
        super(fragmentActivity);

        this.profileId = profileId;
        this.isUserProfile = isUserProfile;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create and return the appropriate fragment based on the position
        return switch (position) {
            case 0 -> DogsFragment.newInstance(profileId, isUserProfile);
            case 1 -> RecipesFragment.newInstance(profileId);
            case 2 -> PhotosFragment.newInstance(profileId);
            default -> DogsFragment.newInstance(profileId, isUserProfile);
        };
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }

}
