package edu.northeastern.pawsomepals.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.northeastern.pawsomepals.R;

public class ProfileFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser currentUser;
    private String userId;
    private ProgressBar progressBar;

    private RecyclerView recyclerViewDogs;
    private RecyclerView recyclerViewPhotos;
    private RecyclerView recyclerViewRecipes;
    private FloatingActionButton fabAddDogProfile;

    private TextView profileName;
    private TextView postsCount;
    private TextView followersCount;
    private TextView followingCount;
    private ImageView profileImage;
    private Button editOrFollowButton;
    private String profileId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("ProfileId", MODE_PRIVATE);
        profileId = sharedPreferences.getString("profileId", "none");
        // Initialize UI elements
        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);
        postsCount = view.findViewById(R.id.postsCount);
        followersCount = view.findViewById(R.id.followersCount);
        followingCount = view.findViewById(R.id.followingCount);
        recyclerViewDogs = view.findViewById(R.id.recyclerViewDogs);
        recyclerViewPhotos = view.findViewById(R.id.recyclerViewPhotos);
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes);
        fabAddDogProfile = view.findViewById(R.id.fabAddDogProfile);
        editOrFollowButton = view.findViewById(R.id.editOrFollowButton);
        progressBar = view.findViewById(R.id.progressBar);
        editOrFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonText = editOrFollowButton.getText().toString();

                if(buttonText.equals("Edit Profile"))
                {
                    navigateToEditUserProfile();
                } else if (buttonText.equals("Follow")) {
                    followProfile();
                } else if (buttonText.equals("Following")) {
                    unfollowProfile();
                }
            }
        });
        return view;
    }

    private void navigateToEditUserProfile() {
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
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .update("followers", FieldValue.arrayUnion(userId))
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"
                                                                editOrFollowButton.setText("Following");
                                                                progressBar.setVisibility(View.GONE);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Log.w("Follow Profile", "Error updating followers", e);
                                                            });
                                                } else {
                                                    // Document does not exist, create it and set the "followers" field
                                                    Map<String, Object> data = new HashMap<>();
                                                    data.put("followers", Arrays.asList(userId));
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .set(data)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"
                                                                editOrFollowButton.setText("Following");
                                                                progressBar.setVisibility(View.GONE);
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
                        data.put("following", Arrays.asList(profileId));
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
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .update("followers", FieldValue.arrayUnion(userId))
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"
                                                                editOrFollowButton.setText("Following");
                                                                progressBar.setVisibility(View.GONE);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Log.w("Follow Profile", "Error updating followers", e);
                                                            });
                                                } else {
                                                    // Document does not exist, create it and set the "followers" field
                                                    Map<String, Object> profileData = new HashMap<>();
                                                    profileData.put("followers", Arrays.asList(userId));
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .set(profileData)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"
                                                                editOrFollowButton.setText("Following");
                                                                progressBar.setVisibility(View.GONE);
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



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
