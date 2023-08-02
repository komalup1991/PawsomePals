package edu.northeastern.pawsomepals.ui.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.ImageUtil;


public class CreatePhotoVideoActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 3;
    private Toolbar toolbar;
    private CircleImageView userProfilePic;
    private String photoVideoPostDocId;
    private TextView userNameTextView, tagPeopleTextView, addLocationTextView, taggedUserDisplayTextView;
    private EditText captionEditTextView;
    private Dialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String loggedInUserId;
    private Map<String, Users> allUsers;
    private List<Users> selectedUsers;
    private AutoCompleteTextView searchLocationDisplayTextView;
    private ImageView selectPhoto,photoVideoImageView;
    private Uri galleryImageUri, cameraImageUri;
    private boolean isEditImageDialogVisible = false;
    private boolean isDeleteConfirmationDialogVisible = false;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_photo_video);

        allUsers = new HashMap<>();
        selectedUsers = new ArrayList<>();
        toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        captionEditTextView = findViewById(R.id.captionEditTextView);
        tagPeopleTextView = findViewById(R.id.tagPeopleTextView);
        taggedUserDisplayTextView = findViewById(R.id.taggedUserDisplayTextView);
        addLocationTextView = findViewById(R.id.addLocationTextView);
        searchLocationDisplayTextView = findViewById(R.id.searchLocationDisplayTextView);
        photoVideoImageView = findViewById(R.id.photoVideoImageView);
        selectPhoto = findViewById(R.id.addPhotoImageView);
        ImageView deleteImageView = findViewById(R.id.deleteImageView);
        ImageView editImageView = findViewById(R.id.editImageView);


        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        currentUser = auth.getCurrentUser();
        loggedInUserId = currentUser.getUid();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Post");
        }
        FirebaseUtil.fetchUserInfoFromFirestore(loggedInUserId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed=user.getName();
                userProfileUrlToSaveInFeed=user.getProfileImage();

                Glide.with(CreatePhotoVideoActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                userNameTextView.setText(user.getName());
            }
        });

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.showPhotoSelectionDialog(CreatePhotoVideoActivity.this);
            }
        });

        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditImageDialog();
            }
        });

        tagPeopleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserSelectionDialog();
            }
        });

        taggedUserDisplayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserSelectionDialog();
            }
        });

        fetchAllUsersFromFirestore();

        List<String> locationSuggestions = new ArrayList<>();
        locationSuggestions.add("New York, USA");
        locationSuggestions.add("Los Angeles, USA");
        locationSuggestions.add("London, UK");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locationSuggestions);
        searchLocationDisplayTextView.setAdapter(adapter);


        addLocationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocationDisplayTextView.setVisibility(View.VISIBLE);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (captionEditTextView.getText().toString().isEmpty()) {
                    captionEditTextView.setError("This field is required");
                    Toast.makeText(CreatePhotoVideoActivity.this, "Post Caption is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoVideoImageView.getDrawable() == null) {
                    Toast.makeText(CreatePhotoVideoActivity.this, "Post Image is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUtil.uploadImageToStorage(cameraImageUri, galleryImageUri,
                        "photovideo", new BaseDataCallback() {

                            @Override
                            public void onImageUriReceived(String imageUrl) {
                                createFeedMap(imageUrl);
                            }

                            @Override
                            public void onDismiss() {
                                DialogHelper.hideProgressDialog(progressDialog);
                                finish();
                            }

                            @Override
                            public void onError(Exception exception) {
                                Toast.makeText(context, "Error uploading image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                DialogHelper.showProgressDialog("Your Photo/Video is being saved...", progressDialog, CreatePhotoVideoActivity.this);
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelConfirmationDialog();
                finish();
            }
        });
    }
    private void showEditImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Photo");
        builder.setItems(new CharSequence[]{"Take New Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (ImageUtil.checkCameraPermission(CreatePhotoVideoActivity.this)) {
                            ImageUtil.openCamera(CreatePhotoVideoActivity.this);
                        } else {
                            ImageUtil.requestCameraPermission(CreatePhotoVideoActivity.this);
                        }
                        break;
                    case 1:
                        if (ImageUtil.checkStoragePermission(CreatePhotoVideoActivity.this)) {
                            ImageUtil.openGallery(CreatePhotoVideoActivity.this);
                        } else {
                            ImageUtil.requestStoragePermission(CreatePhotoVideoActivity.this);
                        }
                        break;
                }
            }
        });
        builder.show();

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isEditImageDialogVisible = false;
            }
        });
        isEditImageDialogVisible = true;
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                galleryImageUri = null;
                cameraImageUri = ImageUtil.saveCameraImageToFile(data,this);
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(cameraImageUri).centerCrop().into(photoVideoImageView);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                cameraImageUri = null;
                galleryImageUri = data.getData();
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(galleryImageUri).centerCrop().into(photoVideoImageView);
            }
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ImageUtil.checkCameraPermission(this)) {
                    ImageUtil.openCamera(this);
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete the selected image?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDeleteConfirmationDialogVisible = false;
            }
        });
        isDeleteConfirmationDialogVisible = true;
        dialog.show();
    }

    private void deleteImage() {
        photoVideoImageView.setImageDrawable(null);
        selectPhoto.setVisibility(View.VISIBLE);
        galleryImageUri = null;
        cameraImageUri = null;
    }



    private void createFeedMap(String imageUrlFromFirebaseStorage) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String caption = captionEditTextView.getText().toString();
        String userTagged = taggedUserDisplayTextView.getText().toString();
        String locationTagged = searchLocationDisplayTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> photoVideoPosts = new HashMap<>();
        photoVideoPosts.put("createdBy", loggedInUserId);
        photoVideoPosts.put("caption", caption);
        photoVideoPosts.put("userTagged", userTagged);
        photoVideoPosts.put("locationTagged", locationTagged);
        photoVideoPosts.put("createdAt", createdAt);
        photoVideoPosts.put("username",userNameToSaveInFeed);
        photoVideoPosts.put("userProfileImage",userProfileUrlToSaveInFeed);
        photoVideoPosts.put("type",1);
        photoVideoPosts.put("img", imageUrlFromFirebaseStorage);
        photoVideoPosts.put("feedItemId", UUID.randomUUID().toString());

        FirebaseUtil.createCollectionInFirestore(photoVideoPosts, "photovideo",new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);
                finish();
            }
        });
    }

    private void fetchAllUsersFromFirestore() {
        db.collection("user")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        allUsers.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Users user = document.toObject(Users.class);
                            if (!user.getUserId().equals(loggedInUserId)) {
                                allUsers.putIfAbsent(user.getUserId(), user);
                            }
                        }
                    }
                });
    }


    private void showUserSelectionDialog() {
        List<String> userNames = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        boolean[] checkedItems = new boolean[allUsers.size()];

        int i = 0;
        for (Map.Entry<String, Users> entry : allUsers.entrySet()) {
            Users user = entry.getValue();
            if (user.getName() != null) {
                userNames.add(user.getName());
                userIds.add(user.getUserId());
            }
            checkedItems[i++] = selectedUsers.contains(user);
        }

        String[] userNamesArray = userNames.toArray(new String[userNames.size()]);
        String[] userIdsArray = userIds.toArray(new String[userNames.size()]);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Select Users")
                .setMultiChoiceItems(userNamesArray, checkedItems, (dialog, which, isChecked) -> {
                    Users user = allUsers.get(userIdsArray[which]);
                    Log.d("yoo", " " + user.getName());
                    if (isChecked) {
                        selectedUsers.add(user);
                    } else {
                        selectedUsers.remove(user);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    updateSelectedUsersTextView();

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSelectedUsersTextView() {
        List<String> selectedNames = new ArrayList<>();
        for (Users user : selectedUsers) {
            Log.d("yoo", " " + user.getName());
            selectedNames.add(user.getName());
        }

        StringBuilder commaSeparatedNames = new StringBuilder();
        for (int i = 0; i < selectedNames.size(); i++) {

            commaSeparatedNames.append(selectedNames.get(i));
            if (i < selectedNames.size() - 1) {
                commaSeparatedNames.append(", ");
            }
        }

        taggedUserDisplayTextView.setVisibility(View.VISIBLE);
        taggedUserDisplayTextView.setText(commaSeparatedNames.toString());

    }


    private void showCancelConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Action")
                .setMessage("Are you sure you want to cancel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard changes?");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }




}