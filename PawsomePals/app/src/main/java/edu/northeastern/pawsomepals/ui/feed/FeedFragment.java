package edu.northeastern.pawsomepals.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FragmentAdapter;
import edu.northeastern.pawsomepals.ui.feed.FeedCreateListDialogFragment;

public class FeedFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = view.findViewById(R.id.fab);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this.getActivity());
        viewPager.setAdapter(fragmentAdapter);

        // Connect the TabLayout with the ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Set the tab text based on the position
            switch (position) {
                case 0 -> tab.setText("All");
                case 1 -> tab.setText("Friends");
                case 2 -> tab.setText("Recipes");
            }
        }).attach();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedCreateListDialogFragment dialogFragment = FeedCreateListDialogFragment.newInstance();
                dialogFragment.show(getParentFragmentManager(), "CreateListDialog");
            }
        });

    }
}
