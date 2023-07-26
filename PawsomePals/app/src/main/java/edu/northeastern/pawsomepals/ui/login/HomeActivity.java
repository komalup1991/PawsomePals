package edu.northeastern.pawsomepals.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;
import edu.northeastern.pawsomepals.ui.chat.ChatFragment;
import edu.northeastern.pawsomepals.ui.feed.FeedFragment;
import edu.northeastern.pawsomepals.ui.map.MapFragment;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
import edu.northeastern.pawsomepals.ui.search.SearchFragment;

public class HomeActivity extends AppCompatActivity implements LogoutDialogListener{
    BottomNavigationView bottomNavigationView;
    private int currentSelectedItemIndex = 0;
    private Toolbar toolbar;

    private ImageView logoutImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar=findViewById(R.id.chatToolBar);

        logoutImageView = findViewById(R.id.logoutImageView);

        logoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialogFragment dialog = new LogoutDialogFragment();
                dialog.setLogoutDialogListener((LogoutDialogListener) HomeActivity.this);
                dialog.show(getSupportFragmentManager(), "logout_dialog");
//                Log out delete messaging token to avoid receiving messages
//                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                       if (task.isSuccessful()){
//                           LogoutDialogFragment dialog = new LogoutDialogFragment();
//                           dialog.setLogoutDialogListener((LogoutDialogListener) HomeActivity.this);
//                           dialog.show(getSupportFragmentManager(), "logout_dialog");
//                       }
//                    }
//                });
            }
        });

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

    @Override
    public void onLogoutConfirmed() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }
}