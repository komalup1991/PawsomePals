package edu.northeastern.pawsomepals.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.pawsomepals.ui.profile.DogsFragment;
import edu.northeastern.pawsomepals.ui.profile.FavoritesFragment;
import edu.northeastern.pawsomepals.ui.profile.RecipesFragment;

public class ProfileFragmentAdapter  extends FragmentStateAdapter{
    private static final int NUM_TABS = 3;

    private String profileId;
    private Boolean isUserProfile;
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
            case 1 -> new RecipesFragment();
            case 2 -> new FavoritesFragment();
            default -> new DogsFragment(); // Return a default fragment if needed
        };
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
