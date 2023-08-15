package edu.northeastern.pawsomepals.ui.profile;

import static android.content.Context.MODE_PRIVATE;
import static edu.northeastern.pawsomepals.ui.login.HomeActivity.PROFILE_ACTIVITY_REQUEST_CODE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileFragmentAdapter;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.FeedCollectionType;
import edu.northeastern.pawsomepals.ui.feed.FeedFragmentViewType;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;

public class ProfileFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser currentUser;
    private String userId;
    private ProgressBar progressBar;

    private FloatingActionButton fabAddDogProfile;

    private TextView profileName;
    private TextView postsCount;
    private TextView followersCount;
    private TextView followingCount;
    private ImageView profileImage;
    private Button editOrFollowButton;
    private String profileId;
    private long followingCountValue = 0;
    private long followersCountValue = 0;
    private long postsCountValue = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int PERMISSIONS_REQUEST_STORAGE = 4;
    private Uri photoUri;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Boolean isUserProfile = false;

    private LinearLayout followingLayout;
    private LinearLayout followersLayout;
    private LinearLayout postsLayout;
    private ImageButton favImageButton;
    private String profileIdArg;

    private long favoritesCountValue = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("ProfileId", MODE_PRIVATE);
        profileId = sharedPreferences.getString("profileId", "none");

        if (getArguments() != null) {
            profileIdArg = getArguments().getString("profileId");
        }

        if (profileIdArg != null) {
            profileId = profileIdArg;
        }


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        currentUser = firebaseAuth.getCurrentUser();


        if (currentUser != null) {
            userId = currentUser.getUid();
        }


        // Initialize UI elements
        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);
        postsCount = view.findViewById(R.id.postsCount);
        followersCount = view.findViewById(R.id.followersCount);
        followingCount = view.findViewById(R.id.followingCount);
        editOrFollowButton = view.findViewById(R.id.editOrFollowButton);
        progressBar = view.findViewById(R.id.progressBar);
        followingLayout = view.findViewById(R.id.followingLayout);
        followersLayout = view.findViewById(R.id.followersLayout);
        postsLayout = view.findViewById(R.id.postsLayout);
        fabAddDogProfile = view.findViewById(R.id.fabAddDogProfile);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        favImageButton = view.findViewById(R.id.favImageButton);

        // Check if the profileId is the same as the current user's userId
        if (userId.equals(profileId)) {
            editOrFollowButton.setText("Edit Profile");
            isUserProfile = true;
            favImageButton.setVisibility(View.VISIBLE);
        } else {
            setupFollowButton();
            fabAddDogProfile.hide();
            favImageButton.setVisibility(View.GONE);
        }

        ProfileFragmentAdapter fragmentAdapter = new ProfileFragmentAdapter(this.getActivity(), profileId, isUserProfile);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(fragmentAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0 -> tab.setText("Dogs");
                case 1 -> tab.setText("Recipes");
                case 2 -> tab.setText("Photos");
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0 && isUserProfile) {
                    fabAddDogProfile.show();
                } else {
                    fabAddDogProfile.hide();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });

        // Call the method to display name and image of the user
        getUserInfo(profileId);

        // Call the method to start listening for changes in the following count
        getFollowingCount(profileId);

        // Call the method to start listening for changes in the followers count
        getFollowersCount(profileId);

        // Call the method to start listening for changes in the posts count
        getPostsCount(profileId);

        fabAddDogProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewDogProfileActivity.class);
                intent.putExtra("redirectTo", "Profile");
                getContext().startActivity(intent);
            }
        });


        editOrFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonText = editOrFollowButton.getText().toString();

                if (buttonText.equals("Edit Profile")) {
                    navigateToEditUserProfile();
                } else if (buttonText.equals("Follow")) {
                    editOrFollowButton.setText("Following");
                    followProfile();
                } else if (buttonText.equals("Following")) {
                    unfollowProfile();
                }
            }
        });

        followingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!followingCount.getText().equals("0")) {
                    Intent intent = new Intent(getContext(), FollowersFollowingActivity.class);
                    intent.putExtra("profileId", profileId);
                    intent.putExtra("clickedValue", "following");
                    getActivity().startActivityForResult(intent, PROFILE_ACTIVITY_REQUEST_CODE);
                } else {
                    Toast.makeText(requireContext(), "Following no users", Toast.LENGTH_SHORT).show();
                }
            }
        });

        followersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!followersCount.getText().equals("0")) {
                    Intent intent = new Intent(getContext(), FollowersFollowingActivity.class);
                    intent.putExtra("profileId", profileId);
                    intent.putExtra("clickedValue", "followers");
                    getActivity().startActivityForResult(intent, PROFILE_ACTIVITY_REQUEST_CODE);
                } else {
                    Toast.makeText(requireContext(), "No followers", Toast.LENGTH_SHORT).show();
                }
            }
        });

        postsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!postsCount.getText().equals("0")) {
                    ProfileFeedFragment feedFragment = new ProfileFeedFragment();
                    Bundle args = new Bundle();

                    args.putString("profileId", profileId);
                    args.putString("tabText", "Posts");
                    args.putSerializable("feed_view_type", FeedFragmentViewType.POST);
                    feedFragment.setArguments(args);

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container_view, feedFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Toast.makeText(requireContext(), "No posts", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getFavoritesCount();

        favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favoritesCountValue > 0) {
                    ProfileFeedFragment feedFragment = new ProfileFeedFragment();
                    Bundle args = new Bundle();

                    args.putString("profileId", profileId);
                    args.putString("tabText", "Favourites");
                    args.putSerializable("feed_view_type", FeedFragmentViewType.FAVOURITE);
                    feedFragment.setArguments(args);

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container_view, feedFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Toast.makeText(requireContext(), "No Favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });


        progressBar.setVisibility(View.GONE);

        return view;
    }

    private void getFavoritesCount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> feedIds = new ArrayList<>(FirestoreDataLoader.fetchUserFavFeedIds());
                favoritesCountValue = feedIds.size();

                if (favoritesCountValue > 0) {
                    favImageButton.setImageResource(R.drawable.pawprintfull);
                } else {
                    favImageButton.setImageResource(R.drawable.pawprintempty);
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch and update user info when the fragment resumes
        getUserInfo(profileId);
    }

    private void navigateToEditUserProfile() {
        Intent intent = new Intent(getContext(), EditUserProfileActivity.class);
        getContext().startActivity(intent);
    }

    private void followProfile() {
        // Check if the document for the current user exists in the "follow" collection
        firebaseFirestore.collection("follow")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, update the "following" field
                        firebaseFirestore.collection("follow")
                                .document(userId)
                                .update("following", FieldValue.arrayUnion(profileId))
                                .addOnSuccessListener(aVoid -> {
                                    // Check if the document for the profileId exists in the "follow" collection
                                    firebaseFirestore.collection("follow")
                                            .document(profileId)
                                            .get()
                                            .addOnSuccessListener(profileDocumentSnapshot -> {
                                                if (profileDocumentSnapshot.exists()) {
                                                    // Document exists, update the "followers" field
                                                    List<String> followersList = (List<String>) profileDocumentSnapshot.get("followers");
                                                    if (followersList != null && !followersList.contains(userId)) {
                                                        followersList.add(userId);

                                                        firebaseFirestore.collection("follow")
                                                                .document(profileId)
                                                                .update("followers", followersList)
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    // Update the UI and change the button text to "Following"

                                                                    progressBar.setVisibility(View.GONE);

                                                                    // Update the Followers count in the UI
                                                                    followersCountValue = followersList.size();
                                                                    followersCount.setText(String.valueOf(followersCountValue));
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Log.w("Follow Profile", "Error updating followers", e);
                                                                });
                                                    } else {
                                                        // The current user is already in the followers list
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    // Document does not exist, create it and set the "followers" field
                                                    Map<String, Object> profileData = new HashMap<>();
                                                    profileData.put("followers", Collections.singletonList(userId));

                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .set(profileData)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"

                                                                progressBar.setVisibility(View.GONE);

                                                                // Update the Followers count in the UI
                                                                followersCountValue = 1;
                                                                followersCount.setText(String.valueOf(followersCountValue));
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Log.w("Follow Profile", "Error creating profile document", e);
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                Log.w("Follow Profile", "Error getting profile document", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.w("Follow Profile", "Error updating following", e);
                                });
                    } else {
                        // Document does not exist, create it and set the "following" field
                        Map<String, Object> data = new HashMap<>();
                        data.put("following", Collections.singletonList(profileId));
                        firebaseFirestore.collection("follow")
                                .document(userId)
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    // Check if the document for the profileId exists in the "follow" collection
                                    firebaseFirestore.collection("follow")
                                            .document(profileId)
                                            .get()
                                            .addOnSuccessListener(profileDocumentSnapshot -> {
                                                if (profileDocumentSnapshot.exists()) {
                                                    // Document exists, update the "followers" field
                                                    List<String> followersList = (List<String>) profileDocumentSnapshot.get("followers");
                                                    if (followersList != null && !followersList.contains(userId)) {
                                                        followersList.add(userId);
                                                        firebaseFirestore.collection("follow")
                                                                .document(profileId)
                                                                .update("followers", followersList)
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    // Update the UI and change the button text to "Following"

                                                                    progressBar.setVisibility(View.GONE);

                                                                    // Update the Followers count in the UI
                                                                    getFollowersCount(profileId);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Log.w("Follow Profile", "Error updating followers", e);
                                                                });
                                                    } else {
                                                        // The current user is already in the followers list
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    // Document does not exist, create it and set the "followers" field
                                                    Map<String, Object> profileData = new HashMap<>();
                                                    profileData.put("followers", Collections.singletonList(userId));
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .set(profileData)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"

                                                                progressBar.setVisibility(View.GONE);

                                                                // Update the Followers count in the UI
                                                                getFollowersCount(profileId);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Log.w("Follow Profile", "Error creating profile document", e);
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                Log.w("Follow Profile", "Error getting profile document", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.w("Follow Profile", "Error updating following", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.w("Follow Profile", "Error checking if document exists", e);
                });
    }


    private void unfollowProfile() {
        // Check if the document for the current user exists in the "follow" collection
        firebaseFirestore.collection("follow")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, update the "following" field
                        firebaseFirestore.collection("follow")
                                .document(userId)
                                .update("following", FieldValue.arrayRemove(profileId))
                                .addOnSuccessListener(aVoid -> {
                                    // Check if the document for the profileId exists in the "follow" collection
                                    firebaseFirestore.collection("follow")
                                            .document(profileId)
                                            .get()
                                            .addOnSuccessListener(profileDocumentSnapshot -> {
                                                if (profileDocumentSnapshot.exists()) {
                                                    // Document exists, update the "followers" field
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .update("followers", FieldValue.arrayRemove(userId))
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Follow"
                                                                editOrFollowButton.setText("Follow");
                                                                progressBar.setVisibility(View.GONE);

                                                                getFollowersCount(profileId);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Log.w("Unfollow Profile", "Error updating followers", e);
                                                            });
                                                } else {
                                                    // Document does not exist, log an error (should not happen)
                                                    progressBar.setVisibility(View.GONE);
                                                    Log.e("Unfollow Profile", "Profile document does not exist");
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                Log.w("Unfollow Profile", "Error getting profile document", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.w("Unfollow Profile", "Error updating following", e);
                                });
                    } else {
                        // Document does not exist, log an error (should not happen)
                        progressBar.setVisibility(View.GONE);
                        Log.e("Unfollow Profile", "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.w("Unfollow Profile", "Error checking if document exists", e);
                });
    }


    private void getFollowingCount(String userIdValue) {
        firebaseFirestore.collection("follow")
                .document(userIdValue)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("Get Following Count", "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> followingList = (List<String>) documentSnapshot.get("following");
                        if (followingList != null) {
                            followingCountValue = followingList.size();
                            // Update the UI with the new following count
                            followingCount.setText(String.valueOf(followingCountValue));
                        }
                    } else {
                        Log.d("Get Following Count", "Current data: null");
                    }
                });
    }

    private void getFollowersCount(String userIdValue) {
        firebaseFirestore.collection("follow")
                .document(userIdValue)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("Get Followers Count", "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Object followersValue = documentSnapshot.get("followers");
                        if (followersValue != null) {
                            if (followersValue instanceof Number) {
                                followersCountValue = ((Number) followersValue).longValue();
                            } else if (followersValue instanceof List) {
                                followersCountValue = ((List<?>) followersValue).size();
                            } else {
                                // Handle other data types if needed
                            }
                            // Update the UI with the new followers count
                            followersCount.setText(String.valueOf(followersCountValue));
                        }
                    } else {
                        Log.d("Get Followers Count", "Current data: null");
                    }
                });
    }


    private void getPostsCount(String userIdValue) {
        firebaseFirestore.collection(FeedCollectionType.POSTS)
                .whereEqualTo("createdBy", userIdValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsCountValue = queryDocumentSnapshots.size();
                    // Update the UI with the new posts count
                    postsCount.setText(String.valueOf(postsCountValue));
                })
                .addOnFailureListener(e -> {
                    Log.e("Get Posts Count", "Error getting documents.", e);
                });
    }

    private void getUserInfo(String userIdValue) {
        firebaseFirestore.collection("user")
                .whereEqualTo("userId", userIdValue)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            Users user = userDocument.toObject(Users.class);

                            String profileImagePath = user.getProfileImage();

                            if (!Objects.isNull(profileImagePath)) {
                                if (!profileImagePath.equals("") && !profileImagePath.equals("null")) {
                                    // Load the profile image using Glide
                                    Glide.with(requireContext())
                                            .load(profileImagePath)
                                            .into(profileImage);
                                } else {
                                    // If the profile image path is empty or null, you can use a placeholder image
                                    Glide.with(requireContext())
                                            .load(R.drawable.ud)
                                            .into(profileImage);
                                }
                            } else {
                                // If the profile image path is empty or null, you can use a placeholder image
                                Glide.with(requireContext())
                                        .load(R.drawable.ud)
                                        .into(profileImage);
                            }


                            profileName.setText(user.getName());


                        }
                    } else {
                    }
                });
    }


    private void setupFollowButton() {
        // Check if the document for the current user exists in the "follow" collection
        firebaseFirestore.collection("follow")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> followingList = (List<String>) documentSnapshot.get("following");
                        if (followingList != null && followingList.contains(profileId)) {
                            // The current user is already following this profile
                            editOrFollowButton.setText("Following");
                        } else {
                            // The current user is not following this profile
                            editOrFollowButton.setText("Follow");
                        }
                    } else {
                        // Document does not exist, the current user is not following this profile
                        editOrFollowButton.setText("Follow");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error if needed
                    Log.e("Setup Follow Button", "Error checking if document exists", e);
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
