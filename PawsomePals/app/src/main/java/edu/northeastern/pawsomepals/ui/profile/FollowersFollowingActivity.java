package edu.northeastern.pawsomepals.ui.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileDogAdapter;
import edu.northeastern.pawsomepals.adapters.ProfileFollowingFollowerAdapter;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.models.Like;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;

public class FollowersFollowingActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser currentUser;
    private String userId;
    private ProgressBar progressBar;
    private String profileId;

    private RecyclerView recyclerViewUsers;
    private ProfileFollowingFollowerAdapter profileFollowingFollowerAdapter;
    private List<Users> userProfiles = new ArrayList<>();
    private TextView textNoUserProfiles;
    private SearchView searchView;
    private String displayFollowersOrFollowingUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_following);

        Intent intent = getIntent();
        profileId = intent.getStringExtra("profileId");
        displayFollowersOrFollowingUsers = intent.getStringExtra("clickedValue");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Initialize UI elements
        recyclerViewUsers = findViewById(R.id.recyclerView);
        textNoUserProfiles = findViewById(R.id.textViewEmptyList);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(displayFollowersOrFollowingUsers.equals("following")) {
            actionBar.setTitle("Following Users");} else {
                actionBar.setTitle("Followers");
            }
        }

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewUsers.setLayoutManager(verticalLayoutManager);

        profileFollowingFollowerAdapter = new ProfileFollowingFollowerAdapter(userProfiles, this);

        recyclerViewUsers.setAdapter(profileFollowingFollowerAdapter);

        profileFollowingFollowerAdapter.setOnItemClickListener(new ProfileFollowingFollowerAdapter.OnItemClickListener() {
            @Override
            public void onUserClick(int position) {
                Users selectedUser = userProfiles.get(position);
                String selectedUserId = selectedUser.getUserId();
                navigateToProfileFragment(selectedUserId);
            }
        });

        if(displayFollowersOrFollowingUsers.equals("following")) {
            fetchFollowingUserProfiles(profileId);
        }else {
            fetchFollowersUserProfiles(profileId);
        }
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void navigateToProfileFragment(String userIDValue) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("ProfileId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileId", userIDValue);
        editor.apply();

        //Navigate to Profile Fragment
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void filterList(String newText) {
        List<Users> filteredList = new ArrayList<>();
        for (Users user : userProfiles) {
            if (user.getName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(user);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
        }
        else {
            profileFollowingFollowerAdapter.setFilteredList(filteredList);
        }
    }


    private void fetchFollowingUserProfiles(String userIdValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("follow")
                .document(userIdValue)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> followingList = (List<String>) document.get("following");
                            if (followingList != null && !followingList.isEmpty()) {
                                // Step 2: Fetch documents from the "user" collection based on the list of user IDs
                                db.collection("user")
                                        .whereIn("userId", followingList)
                                        .get()
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful()) {
                                                userProfiles = new ArrayList<>();
                                                for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                                                    // Replace "UserModel" with your actual model class for users
                                                    Users user = userDoc.toObject(Users.class);

                                                    userProfiles.add(user);
                                                }
                                                profileFollowingFollowerAdapter.setUserProfiles(userProfiles);

                                                profileFollowingFollowerAdapter.notifyDataSetChanged();
                                            } else {
                                                // Handle errors while fetching user documents
                                            }
                                        });
                            } else {
                                if (followingList != null && !followingList.isEmpty() && !(followingList.size() == 0)) {
                                    recyclerViewUsers.setVisibility(View.VISIBLE);
                                    textNoUserProfiles.setVisibility(View.GONE);
                                } else {
                                    recyclerViewUsers.setVisibility(View.GONE);
                                    textNoUserProfiles.setText("Following no users");
                                    textNoUserProfiles.setVisibility(View.VISIBLE);
                                }
                                profileFollowingFollowerAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.e("Fetch User Profiles", "Error fetching user profiles");
                        }
                    } else {
                        Log.e("Fetch User Profiles", "Error fetching user profiles");
                    }
                });
    }

    private void fetchFollowersUserProfiles(String userIdValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("follow")
                .document(userIdValue)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> followingList = (List<String>) document.get("followers");
                            if (followingList != null && !followingList.isEmpty()) {
                                // Step 2: Fetch documents from the "user" collection based on the list of user IDs
                                db.collection("user")
                                        .whereIn("userId", followingList)
                                        .get()
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful()) {
                                                userProfiles = new ArrayList<>();
                                                for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                                                    // Replace "UserModel" with your actual model class for users
                                                    Users user = userDoc.toObject(Users.class);

                                                    userProfiles.add(user);
                                                }
                                                profileFollowingFollowerAdapter.setUserProfiles(userProfiles);

                                                profileFollowingFollowerAdapter.notifyDataSetChanged();
                                            } else {
                                                Log.e("Fetch User Profiles", "Error fetching user profiles");
                                            }
                                        });
                            } else {
                                if (followingList != null && !followingList.isEmpty() && !(followingList.size() == 0)) {
                                    recyclerViewUsers.setVisibility(View.VISIBLE);
                                    textNoUserProfiles.setVisibility(View.GONE);
                                } else {
                                    recyclerViewUsers.setVisibility(View.GONE);
                                    textNoUserProfiles.setText("No user is following you");
                                    textNoUserProfiles.setVisibility(View.VISIBLE);
                                }
                                profileFollowingFollowerAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.e("Fetch User Profiles", "Error fetching user profiles");
                        }
                    } else {
                        Log.e("Fetch User Profiles", "Error fetching user profiles");
                    }
                });
    }
}