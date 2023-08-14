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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
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
    private Uri imageUri;

    private TaggingOptionsLayout taggingOptionsLayout;

    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;

    private Context context;
    private LatLng currentLatLng;
    private String locationTagged;
    private String usersTagged;
    private long selectedDate = -1; // Initialize with an invalid value
    private int selectedHour = -1;
    private int selectedMinute = 0;
    private Event existingFeedItem;

    private String currentFeedItemId;

    private boolean hasImageChanged = false;


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
        eventImageView = findViewById(R.id.eventImageView);
        selectPhoto = findViewById(R.id.addPhotoImageView);
        setEventTimeTextView = findViewById(R.id.setEventTimeTextView);

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

        existingFeedItem = (Event) getIntent().getSerializableExtra("existingFeedItem");

        if (SaveOrUpdateFeedUtil.isEditMode(existingFeedItem)) {
            eventNameEditText.setText(existingFeedItem.getEventName());
            eventDetailsEditText.setText(existingFeedItem.getEventDetails());
            selectPhoto.setVisibility(View.GONE);
            Glide.with(this).load(existingFeedItem.getImg()).centerCrop().into(eventImageView);
            if (existingFeedItem.getEventDate() != null) {
                setEventDateTextView.setText(existingFeedItem.getEventDate());
            }
            usersTagged = existingFeedItem.getUserTagged();
            locationTagged = existingFeedItem.getLocationTagged();


            if (existingFeedItem.getUserTagged() != null) {
                taggingOptionsLayout.setTagPeopleTextView(existingFeedItem.getUserTagged());
            }

            if (existingFeedItem.getLocationTagged() != null) {
                taggingOptionsLayout.setTagLocationTextView(existingFeedItem.getLocationTagged());
            }

            if (existingFeedItem.getLatLng() != null) {
                currentLatLng = new LatLng(existingFeedItem.getLatLng().getLatitude(),
                        existingFeedItem.getLatLng().getLongitude());
            }

            Calendar dateCalendar = getDateCalendar(existingFeedItem.getEventDate());
            selectedDate = dateCalendar.getTimeInMillis();

            if (existingFeedItem.getEventTime() != null) {
                setEventTimeTextView.setText(existingFeedItem.getEventTime());
            }
            Calendar timeCalendar = getTimeCalendar(existingFeedItem.getEventTime());
            selectedHour = timeCalendar.get(Calendar.HOUR_OF_DAY);
            selectedMinute = timeCalendar.get(Calendar.MINUTE);

            imageUri = Uri.parse(existingFeedItem.getImg());
            currentFeedItemId = existingFeedItem.getFeedItemId();
        } else {
            currentFeedItemId = UUID.randomUUID().toString();
        }


        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();
        loggedInUserId = currentUser.getUid();

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

        setEventDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        setEventTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
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
                if (imageUri == null) {
                    Toast.makeText(CreateEventsActivity.this, "Event Image is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedDate == -1) {
                    Toast.makeText(CreateEventsActivity.this, "Date is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedHour == -1) {
                    Toast.makeText(CreateEventsActivity.this, "Time is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (setEventTimeTextView.getText().toString().isEmpty()) {
                    Toast.makeText(CreateEventsActivity.this, "Time is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.US);
                Calendar selectedDateTime = Calendar.getInstance();
                Calendar currentDateTime = Calendar.getInstance();

                try {
                    selectedDateTime.setTime(dateTimeFormat.parse(setEventDateTextView.getText().toString()
                            + " " + setEventTimeTextView.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                if (selectedDateTime.before(currentDateTime)) {
                    Toast.makeText(getApplicationContext(), "Date and time should not be in the past.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SaveOrUpdateFeedUtil.handleSaveFeed(CreateEventsActivity.this, progressDialog, createEvent(existingFeedItem), imageUri, hasImageChanged);

                DialogHelper.showProgressDialog("Your Event is being saved...", progressDialog, CreateEventsActivity.this);
            }

        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelConfirmationDialog();
            }
        });
    }

    private Event createEvent(Event feedItem) {
        if (feedItem == null) {
            feedItem = new Event();
        }

        feedItem.setEventName(eventNameEditText.getText().toString());
        feedItem.setEventDetails(eventDetailsEditText.getText().toString());
        feedItem.setCreatedBy(loggedInUserId);
        feedItem.setUsername(userNameToSaveInFeed);
        feedItem.setUserTagged(usersTagged);
        feedItem.setEventDate(setEventDateTextView.getText().toString());
        feedItem.setEventTime(setEventTimeTextView.getText().toString());
        feedItem.setFeedItemId(currentFeedItemId);
        feedItem.setType(3);
        feedItem.setUserProfileImage(userProfileUrlToSaveInFeed);
        feedItem.setLocationTagged(locationTagged);
        if (existingFeedItem != null) {
            feedItem.setCommentCount(existingFeedItem.getCommentCount());
            feedItem.setLikeCount(existingFeedItem.getLikeCount());
            feedItem.setFavorite(existingFeedItem.isFavorite());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        feedItem.setCreatedAt(String.valueOf(dateFormat.format(System.currentTimeMillis())));
        if (currentLatLng != null) {
            feedItem.setLatLng(new edu.northeastern.pawsomepals.models.LatLng(currentLatLng.latitude, currentLatLng.longitude));
        }
        return feedItem;
    }

    private void showTimePicker() {
//        if (selectedDate == -1) {
//            // No valid date selected, show an error message or handle this case
//            return;
//        }

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTimeInMillis(System.currentTimeMillis());
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(timeCal.get(Calendar.HOUR_OF_DAY))
                .setMinute(timeCal.get(Calendar.MINUTE) + 5)
                .build();

        picker.show(getSupportFragmentManager(), "TIME_PICKER");

        picker.addOnPositiveButtonClickListener(view -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();

            Calendar selectedDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            selectedDateTime.setTimeInMillis(selectedDate);
            selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedDateTime.set(Calendar.MINUTE, selectedMinute);

            Calendar currentDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            currentDateTime.getTimeZone();
            selectedDateTime.setTimeZone(TimeZone.getDefault());

            if (selectedDateTime.before(currentDateTime)) {
                Toast.makeText(getApplicationContext(), "Date and time should not be a past value.", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            String formattedTime = sdf.format(selectedDateTime.getTime()); // Format in local time zone

            setEventTimeTextView.setText(formattedTime);
        });
    }


    private void showDatePicker() {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now()); // Restrict past dates

        MaterialDatePicker.Builder<Long> materialDatePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select date of the event")
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(System.currentTimeMillis());

        if (selectedDate > 0) {
            materialDatePicker.setSelection(selectedDate);
        }

        MaterialDatePicker<Long> picker = materialDatePicker.build();

        picker.show(getSupportFragmentManager(), "DATE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")); // Set to UTC
            selectedCalendar.setTimeInMillis(selection);

            selectedDate = selectedCalendar.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = sdf.format(selectedDate);
            setEventDateTextView.setText(date);
        });
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
                imageUri = ImageUtil.saveCameraImageToFile(data, this);
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(imageUri).centerCrop().into(eventImageView);
                hasImageChanged = true;
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                imageUri = data.getData();
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(imageUri).centerCrop().into(eventImageView);
                hasImageChanged = true;
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
        imageUri = null;
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

    Calendar getDateCalendar(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    Calendar getTimeCalendar(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        Date date = null;
        try {
            date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}