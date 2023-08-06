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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.layout.TaggingOptionsLayout;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.ImageUtil;

public class CreateEventsActivity extends AppCompatActivity {
    private TextView setEventDateTextView, setEventTimeTextView;
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 3;
    private Toolbar toolbar;
    private CircleImageView userProfilePic;
    private String eventsDocId;
    private TextView userNameTextView, tagPeopleTextView, taggedUserDisplayTextView;
    private EditText eventNameEditText, eventDetailsEditText;
    private Dialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private String loggedInUserId;
    private Map<String, Users> allUsers;
    private List<Users> selectedUsers;
    private ImageView selectPhoto, eventImageView;
    private Uri galleryImageUri, cameraImageUri;

    private TaggingOptionsLayout taggingOptionsLayout;

    int selectedHour = 0;
    int selectedMinute = 0;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;

    private Context context;
    private LatLng currentLatLng;
    private String locationTagged;
    private String usersTagged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getBaseContext();
        setContentView(R.layout.activity_create_events);
        setEventDateTextView = findViewById(R.id.setEventDateTextView);
        allUsers = new HashMap<>();
        selectedUsers = new ArrayList<>();
        toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDetailsEditText = findViewById(R.id.eventDetailsEditText);
        eventImageView = findViewById(R.id.photoVideoImageView);
        selectPhoto = findViewById(R.id.addPhotoImageView);
        taggingOptionsLayout = findViewById(R.id.tag_location_layout);
        taggingOptionsLayout.bindView(this, new TaggingOptionsLayout.OnTaggedDataFetchListener() {
            @Override
            public void onLocation(LatLng latLng, String locationTagged) {
                currentLatLng = latLng;
                CreateEventsActivity.this.locationTagged = locationTagged;
            }

            @Override
            public void onTaggedUsersGet(String usersTagged) {
                CreateEventsActivity.this.usersTagged = usersTagged;

            }
        });
        ImageView deleteImageView = findViewById(R.id.deleteImageView);
        ImageView editImageView = findViewById(R.id.editImageView);


        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();
        loggedInUserId = currentUser.getUid();

        setEventDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        setEventTimeTextView = findViewById(R.id.setEventTimeTextView);
        setEventTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });


        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Event");
        }
        FirebaseUtil.fetchUserInfoFromFirestore(loggedInUserId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed = user.getName();
                userProfileUrlToSaveInFeed = user.getProfileImage();

                Glide.with(CreateEventsActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                userNameTextView.setText(user.getName());
            }
        });

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.showPhotoSelectionDialog(CreateEventsActivity.this);
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
                if (eventNameEditText.getText().toString().isEmpty()) {
                    eventNameEditText.setError("This field is required");
                    Toast.makeText(CreateEventsActivity.this, "Event Name is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (eventDetailsEditText.getText().toString().isEmpty()) {
                    eventDetailsEditText.setError("This field is required");
                    Toast.makeText(CreateEventsActivity.this, "Event Detail is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cameraImageUri==null && galleryImageUri==null ) {
                    eventDetailsEditText.setError("This field is required");
                    Toast.makeText(CreateEventsActivity.this, "Event Image is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUtil.uploadImageToStorage(cameraImageUri, galleryImageUri,
                        "event", new BaseDataCallback() {

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
                DialogHelper.showProgressDialog("Your Event is being saved...", progressDialog, CreateEventsActivity.this);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.showCancelConfirmationDialog(context, CreateEventsActivity.this);
                //     finish();
            }
        });
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .build();

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedHour = picker.getHour();
                selectedMinute = picker.getMinute();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a z", Locale.US);
                String formattedTime = sdf.format(calendar.getTime());

                setEventTimeTextView.setText(formattedTime);
            }
        });


    }

    private void showDatePicker() {

        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date of the event").build();
        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                String date = sdf.format(selection);
                setEventDateTextView.setText(date);
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
                        if (ImageUtil.checkCameraPermission(CreateEventsActivity.this)) {
                            ImageUtil.openCamera(CreateEventsActivity.this);
                        } else {
                            ImageUtil.requestCameraPermission(CreateEventsActivity.this);
                        }
                        break;
                    case 1:
                        if (ImageUtil.checkStoragePermission(CreateEventsActivity.this)) {
                            ImageUtil.openGallery(CreateEventsActivity.this);
                        } else {
                            ImageUtil.requestStoragePermission(CreateEventsActivity.this);
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
                boolean isEditImageDialogVisible = false;
            }
        });
        boolean isEditImageDialogVisible = true;
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                galleryImageUri = null;
                cameraImageUri = ImageUtil.saveCameraImageToFile(data, this);
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(cameraImageUri).centerCrop().into(eventImageView);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                cameraImageUri = null;
                galleryImageUri = data.getData();
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(galleryImageUri).centerCrop().into(eventImageView);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ImageUtil.checkCameraPermission(CreateEventsActivity.this)) {
                    ImageUtil.openCamera(CreateEventsActivity.this);
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
        eventImageView.setImageDrawable(null);
        selectPhoto.setVisibility(View.VISIBLE);
        galleryImageUri = null;
        cameraImageUri = null;
    }


    private void createFeedMap(String imageUrlFromFirebaseStorage) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String eventName = eventNameEditText.getText().toString();
        String eventDetails = eventDetailsEditText.getText().toString();
        String eventDate = setEventDateTextView.getText().toString();
        String eventTime = setEventTimeTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));
        Map<String, Object> events = new HashMap<>();
        events.put("createdBy", loggedInUserId);
        events.put("eventName", eventName);
        events.put("eventTime", eventTime);
        events.put("eventDate", eventDate);
        events.put("eventDetails", eventDetails);
        events.put("userTagged", usersTagged);
        events.put("locationTagged", locationTagged);
        events.put("latLng", currentLatLng);
        events.put("createdAt", createdAt);
        events.put("username", userNameToSaveInFeed);
        events.put("userProfileImage", userProfileUrlToSaveInFeed);
        events.put("type", 3);
        events.put("feedItemId", UUID.randomUUID().toString());
        events.put("img", imageUrlFromFirebaseStorage);

        FirebaseUtil.createCollectionInFirestore(events, "events", new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);
                finish();
            }
        });
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