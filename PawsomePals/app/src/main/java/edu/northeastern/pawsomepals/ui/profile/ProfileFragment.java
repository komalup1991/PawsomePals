package edu.northeastern.pawsomepals.ui.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileFragmentAdapter;
import edu.northeastern.pawsomepals.models.Users;

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
        editOrFollowButton = view.findViewById(R.id.editOrFollowButton);
        progressBar = view.findViewById(R.id.progressBar);
        followingLayout = view.findViewById(R.id.followingLayout);
        followersLayout = view.findViewById(R.id.followersLayout);
        postsLayout = view.findViewById(R.id.postsLayout);
        fabAddDogProfile = view.findViewById(R.id.fabAddDogProfile);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Check if the profileId is the same as the current user's userId
        if (profileId.equals(userId)) {
            editOrFollowButton.setText("Edit Profile");
            isUserProfile = true;
        } else {
            setupFollowButton();
            fabAddDogProfile.hide();
        }

        ProfileFragmentAdapter fragmentAdapter = new ProfileFragmentAdapter(this.getActivity(), profileId, isUserProfile);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(fragmentAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0 -> tab.setText("Dogs");
                case 1 -> tab.setText("Recipes");
                case 2 -> tab.setText("Photos");
                case 3 -> tab.setText("Favourites");
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
                    followProfile();
                } else if (buttonText.equals("Following")) {
                    unfollowProfile();
                }
            }
        });

        followingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersFollowingActivity.class);
                intent.putExtra("profileId", profileId);
                intent.putExtra("clickedValue", "following");
                getContext().startActivity(intent);
            }
        });

        followersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersFollowingActivity.class);
                intent.putExtra("profileId", profileId);
                intent.putExtra("clickedValue", "followers");
                getContext().startActivity(intent);
            }
        });

        if (isUserProfile) {
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openImageChooserDialog();
                }
            });
        }


        // Call the method to display name and image of the user
        getUserInfo(profileId);

        // Call the method to start listening for changes in the following count
        getFollowingCount(profileId);

        // Call the method to start listening for changes in the followers count
        getFollowersCount(profileId);

        // Call the method to start listening for changes in the posts count
        getPostsCount(profileId);


        progressBar.setVisibility(View.GONE);

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
                                                    List<String> followersList = (List<String>) profileDocumentSnapshot.get("followers");
                                                    if (followersList != null && !followersList.contains(userId)) {
                                                        followersList.add(userId);
                                                        firebaseFirestore.collection("follow")
                                                                .document(profileId)
                                                                .update("followers", followersList)
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    // Update the UI and change the button text to "Following"
                                                                    editOrFollowButton.setText("Following");
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
                                                    profileData.put("followers", Arrays.asList(userId));
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .set(profileData)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"
                                                                editOrFollowButton.setText("Following");
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
                                                    List<String> followersList = (List<String>) profileDocumentSnapshot.get("followers");
                                                    if (followersList != null && !followersList.contains(userId)) {
                                                        followersList.add(userId);
                                                        firebaseFirestore.collection("follow")
                                                                .document(profileId)
                                                                .update("followers", followersList)
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    // Update the UI and change the button text to "Following"
                                                                    editOrFollowButton.setText("Following");
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
                                                    profileData.put("followers", Arrays.asList(userId));
                                                    firebaseFirestore.collection("follow")
                                                            .document(profileId)
                                                            .set(profileData)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Update the UI and change the button text to "Following"
                                                                editOrFollowButton.setText("Following");
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

                                                                // Update the Following count in the UI
                                                                if (followingCountValue > 0) {
                                                                    followingCountValue--;
                                                                    followingCount.setText(String.valueOf(followingCountValue));
                                                                }

                                                                // Update the Followers count in the UI
                                                                if (followersCountValue > 0) {
                                                                    followersCountValue--;
                                                                    followersCount.setText(String.valueOf(followersCountValue));
                                                                }
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
        firebaseFirestore.collection("posts")
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
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsCountValue = queryDocumentSnapshots.size();
                    // Update the UI with the new posts count
                    postsCount.setText(String.valueOf(postsCountValue));
                })
                .addOnFailureListener(e -> {
                    Log.e("Get Posts Count", "Error getting documents.", e);
                });

        firebaseFirestore.collection("user")
                .whereEqualTo("userId", userIdValue)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            Users user = userDocument.toObject(Users.class);

                            String profileImagePath = user.getProfileImage();
                            if (!profileImagePath.equals("") && !profileImagePath.equals("null")) {
                                // Load the profile image using Glide
                                Glide.with(requireContext())
                                        .load(profileImagePath)
                                        .into(profileImage);
                            } else {
                                // If the profile image path is empty or null, you can use a placeholder image
                                Glide.with(requireContext())
                                        .load(R.drawable.default_profile_image)
                                        .into(profileImage);
                            }


                            profileName.setText(user.getName());


                        }
                    } else {
                    }
                });
    }

    private void openImageChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Profile Picture");
        builder.setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
            if (which == 0) {
                handleImageCaptureFromCamera();
            } else if (which == 1) {
                handleImagePickFromGallery();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permissions granted, proceed with image capture
                    handleImageCaptureFromCamera();
                } else {
                    // Camera permissions denied, show a message or handle accordingly
                    Toast.makeText(getActivity(), "Camera permissions denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Storage permissions granted, proceed with image pick
                    handleImagePickFromGallery();
                } else {
                    // Storage permissions denied, show a message or handle accordingly
                    Toast.makeText(getActivity(), "Storage permissions denied.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void handleImagePickFromGallery() {
        // Check if storage permissions are granted
        if (checkStoragePermission()) {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_GALLERY);
        } else {
            // Request storage permissions if not granted
            requestStoragePermission();
        }
    }

    private void handleImageCaptureFromCamera() {
        // Check if camera permissions are granted
        if (checkCameraPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            // Request camera permissions if not granted
            requestCameraPermission();
        }
    }


    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_IMAGE_GALLERY);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_STORAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Handle image capture from the camera
                if (data != null && data.getExtras() != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    profileImage.setImageBitmap(bitmap);

                    // Convert the Bitmap to a URI and set it to the photoUri
                    photoUri = getImageUriFromBitmap(bitmap);
                    // Update the profileImage field in Firestore
                    updateProfileImageInFirestore(photoUri);
                } else {
                    // Handle the case when data is null or the image capture failed
                    Toast.makeText(getActivity(), "Failed to capture image from camera.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                // Handle image pick from the gallery
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    photoUri = selectedImageUri;
                    profileImage.setImageURI(photoUri);
                    // Update the profileImage field in Firestore
                    updateProfileImageInFirestore(photoUri);
                }
            }
        }
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }

    private void updateProfileImageInFirestore(Uri photoUri) {
        if (currentUser != null) {
            // Get the Firestore reference for the user document
            DocumentReference userRef = firebaseFirestore.collection("user").document(userId);

            // Create a map to update the profileImage field
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("profileImage", photoUri.toString());

            // Update the document with the new profileImage value
            userRef.update(updateData)
                    .addOnSuccessListener(aVoid -> {
                        // Update successful, do something if needed
                    })
                    .addOnFailureListener(e -> {
                        // Update failed, handle the error
                        Log.e("Update Profile Image", "Error updating profileImage field", e);
                    });
        }
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
