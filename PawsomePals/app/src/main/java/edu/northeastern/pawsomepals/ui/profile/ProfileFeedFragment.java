package edu.northeastern.pawsomepals.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileFeedAdapter;
import edu.northeastern.pawsomepals.ui.feed.FeedFragmentViewType;

public class ProfileFeedFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private String profileId;

    private FeedFragmentViewType viewType;
    private String tabText;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        if (getArguments() != null) {
            profileId = getArguments().getString("profileId");
            tabText = getArguments().getString("tabText");
            viewType = (FeedFragmentViewType) getArguments().getSerializable("feed_view_type");
        }

        ProfileFeedAdapter fragmentAdapter = new ProfileFeedAdapter(this.getActivity(), profileId, viewType);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(1);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(tabText);
            }
        }).attach();
    }
}
