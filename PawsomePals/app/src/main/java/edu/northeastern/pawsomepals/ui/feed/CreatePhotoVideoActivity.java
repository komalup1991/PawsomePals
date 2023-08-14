package edu.northeastern.pawsomepals.ui.feed;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.layout.TaggingOptionsLayout;
import edu.northeastern.pawsomepals.utils.ActivityHelper;
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
    private TextView userNameTextView, tagPeopleTextView, taggedUserDisplayTextView;
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
    private ImageView selectPhoto, photoVideoImageView;
    private Uri imageUri;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;
    private Context context;
    private LatLng currentLatLng;
    private String locationTagged;
    private TaggingOptionsLayout taggingOptionsLayout;
    private String usersTagged;
    private PhotoVideo existingFeedItem;

    private String currentFeedItemId;
    private boolean hasImageChanged;


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
        photoVideoImageView = findViewById(R.id.eventImageView);
        selectPhoto = findViewById(R.id.addPhotoImageView);
        taggingOptionsLayout = findViewById(R.id.tag_location_layout);
        taggingOptionsLayout.bindView(this, new TaggingOptionsLayout.OnTaggedDataFetchListener() {
            @Override
            public void onLocation(LatLng latLng, String locationTagged) {
                currentLatLng = latLng;
                CreatePhotoVideoActivity.this.locationTagged = locationTagged;
            }

            @Override
            public void onTaggedUsersGet(String usersTagged) {
                CreatePhotoVideoActivity.this.usersTagged = usersTagged;

            }
        });
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
            actionBar.setTitle("Add Photo");
        }
        FirebaseUtil.fetchUserInfoFromFirestore(loggedInUserId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed = user.getName();
                userProfileUrlToSaveInFeed = user.getProfileImage();

                Glide.with(CreatePhotoVideoActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                userNameTextView.setText(user.getName());
            }
        });

        existingFeedItem = (PhotoVideo) getIntent().getSerializableExtra("existingFeedItem");
        if (existingFeedItem != null) {
            captionEditTextView.setText(existingFeedItem.getCaption());
            selectPhoto.setVisibility(View.GONE);
            Glide.with(this).load(existingFeedItem.getImg()).centerCrop().into(photoVideoImageView);
            if (existingFeedItem.getUserTagged() != null) {
                taggingOptionsLayout.setTagPeopleTextView(existingFeedItem.getUserTagged());
            }

            if (existingFeedItem.getLocationTagged() != null) {
                taggingOptionsLayout.setTagLocationTextView(existingFeedItem.getLocationTagged());
            }
            usersTagged = existingFeedItem.getUserTagged();
            locationTagged = existingFeedItem.getLocationTagged();
            if (existingFeedItem.getLatLng() != null) {
                currentLatLng = new LatLng(existingFeedItem.getLatLng().getLatitude(),
                        existingFeedItem.getLatLng().getLongitude());
            }

            currentFeedItemId = existingFeedItem.getFeedItemId();
        }


        else {
            currentFeedItemId = UUID.randomUUID().toString();
        }

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

                SaveOrUpdateFeedUtil.handleSaveFeed(CreatePhotoVideoActivity.this, progressDialog,
                        createPhoto(existingFeedItem), imageUri, hasImageChanged);

                DialogHelper.showProgressDialog("Your Photo/Video is being saved...", progressDialog, CreatePhotoVideoActivity.this);
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelConfirmationDialog();
               // finish();
            }
        });
    }

    private PhotoVideo createPhoto(PhotoVideo feedItem) {
        if (feedItem == null) {
            feedItem = new PhotoVideo();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String caption = captionEditTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        feedItem.setCreatedAt(createdAt);
        feedItem.setUserTagged(usersTagged);
        feedItem.setLocationTagged(locationTagged);
        feedItem.setUsername(userNameToSaveInFeed);
        if (currentLatLng != null) {
            feedItem.setLatLng(new edu.northeastern.pawsomepals.models.LatLng(currentLatLng.latitude, currentLatLng.longitude));
        }
        if (existingFeedItem != null) {
            feedItem.setCommentCount(existingFeedItem.getCommentCount());
            feedItem.setLikeCount(existingFeedItem.getLikeCount());
            feedItem.setFavorite(existingFeedItem.isFavorite());
        }
        feedItem.setCaption(caption);
        feedItem.setFeedItemId(currentFeedItemId);
        feedItem.setType(1);
        feedItem.setUserProfileImage(userProfileUrlToSaveInFeed);
        feedItem.setCreatedBy(loggedInUserId);
        return feedItem;
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

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                hasImageChanged = true;
                imageUri = ImageUtil.saveCameraImageToFile(data, this);
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(imageUri).centerCrop().into(photoVideoImageView);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                hasImageChanged = true;
                imageUri = data.getData();
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(imageUri).centerCrop().into(photoVideoImageView);
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
            }
        });
        dialog.show();
    }

    private void deleteImage() {
        photoVideoImageView.setImageDrawable(null);
        selectPhoto.setVisibility(View.VISIBLE);
        imageUri = null;
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
            DialogHelper.hideProgressDialog(progressDialog);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DialogHelper.hideProgressDialog(progressDialog);
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