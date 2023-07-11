package edu.northeastern.pawsomepals.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import edu.northeastern.pawsomepals.R;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private int currentSelectedItemIndex = 0;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar=findViewById(R.id.chatToolBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                currentSelectedItemIndex = item.getItemId();
                Class fragmentClass = getFragmentClassBasedOnId(item.getItemId());
                replaceFragment(fragmentClass, item.getItemId());
                return true;
            }
        });

        if (savedInstanceState == null) {
            currentSelectedItemIndex = R.id.feed;
        } else {
            currentSelectedItemIndex = savedInstanceState.getInt("current_selected_item_index");
        }
        showInitialFragment(savedInstanceState);
    }

    private void showInitialFragment(Bundle savedInstanceState) {
        Class initialFragment = null;
        if (savedInstanceState == null) {
            initialFragment = getFragmentClassBasedOnId(currentSelectedItemIndex);
            String tag = "fragment_" + currentSelectedItemIndex;
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, initialFragment,null, tag)
                    .commit();
        } else {
            initialFragment = getFragmentClassBasedOnId(currentSelectedItemIndex);
            replaceFragment(initialFragment, currentSelectedItemIndex);
        }
    }

    private Class getFragmentClassBasedOnId(int itemId) {
        if (itemId == R.id.feed) {
            toolbar.setTitle("Feed");
            return FeedFragment.class;
        } else if (itemId == R.id.search) {
            toolbar.setTitle("Search");
            return SearchFragment.class;
        } else if (itemId == R.id.chat) {
            toolbar.setTitle("Chat");
            return ChatFragment.class;
        } else if (itemId == R.id.profile) {
            toolbar.setTitle( "Profile");
            return ProfileFragment.class;
        } else if (itemId == R.id.map) {
            toolbar.setTitle( "Map");
            return MapFragment.class;
        }
        return null;
    }
    private void replaceFragment(Class fragmentClass, int itemId) {
        String tag = "fragment_" + itemId;
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragmentByTag == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, fragmentClass, null, tag)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, fragmentByTag, tag)
                    .commit();
        }
    }
    @Override
    public void onBackPressed() {
        if (currentSelectedItemIndex != R.id.feed) {
            replaceFragment(FeedFragment.class,currentSelectedItemIndex);
            bottomNavigationView.setSelectedItemId(R.id.feed);
            currentSelectedItemIndex = R.id.feed;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("current_selected_item_index", currentSelectedItemIndex);
        super.onSaveInstanceState(outState);
    }
}