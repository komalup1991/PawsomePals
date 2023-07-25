package edu.northeastern.pawsomepals.ui.feed;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;

public class CreateEventsActivity extends AppCompatActivity {
    private TextView setEventDateTextView, setEventTimeTextView;
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 3;
    private Toolbar toolbar;
    private CircleImageView userProfilePic;
    private String eventsDocId;
    private TextView userNameTextView, tagPeopleTextView, addLocationTextView, taggedUserDisplayTextView;
    private EditText eventNameEditText,eventDetailsEditText;
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
    int selectedHour = 0;
    int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_events);
        setEventDateTextView = findViewById(R.id.setEventDateTextView);
        allUsers = new HashMap<>();
        selectedUsers = new ArrayList<>();
        toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDetailsEditText = findViewById(R.id.eventDetailsEditText);
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
            actionBar.setTitle("Add Post");
        }
        fetchUserInfoFromFirestore(loggedInUserId);

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoSelectionDialog();
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

                uploadToFireStore();
                uploadImageToStorage();
                showProgressDialog("Your Photo/Video post is being saved...");
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

    private void showTimePicker() {

        //    MaterialTimePicker picker = new MaterialTimePicker.Builder().setInputMode(INPUT_MODE_CLOCK).build();
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

    private void uploadImageToStorage() {
        Uri uploadImageUri = null;
        if (cameraImageUri != null) uploadImageUri = cameraImageUri;
        else if (galleryImageUri != null) uploadImageUri = galleryImageUri;

        if (uploadImageUri != null) {
            // Create a unique filename for the image
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "event_image_" + timestamp + ".jpg";

            // Create a reference to the image file in Firebase Storage
            StorageReference imageRef = storageRef.child("event_images/" + imageName);

            // Upload the image file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(uploadImageUri);

            // Retrieve the download URL of the uploaded image
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            String imageUrl = downloadUri.toString();
                            updateDbWithImg(imageUrl);
                        }
                    } else {

                        Toast.makeText(CreateEventsActivity.this, "Error uploading image: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
        else{
            hideProgressDialog();
            finish();
        }


    }

    private void updateDbWithImg(String imageUrl) {
        if (imageUrl != null) {
            DocumentReference eventRef = db.collection("events").document(eventsDocId);

            eventRef
                    .update("img", imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("yoo", "DocumentSnapshot successfully updated!");
                            hideProgressDialog();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("yoo", "Error updating document", e);
                        }
                    });
        }
        else{
            hideProgressDialog();
            finish();
        }
    }


    private void showEditImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Photo");
        builder.setItems(new CharSequence[]{"Take New Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (checkCameraPermission()) {
                            openCamera();
                        } else {
                            requestCameraPermission();
                        }
                        break;
                    case 1:
                        if (checkStoragePermission()) {
                            openGallery();
                        } else {
                            requestStoragePermission();
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

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
        } catch (ActivityNotFoundException e) {
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                galleryImageUri = null;
                cameraImageUri = saveCameraImageToFile(data);
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

    private Uri saveCameraImageToFile(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap cameraImageBitmap = (Bitmap) extras.get("data");

        // Save the cameraImageBitmap to a file and return its URI
        String imageFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            cameraImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(imageFile);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkCameraPermission()) {
                    openCamera();
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

    private void showPhotoSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Photo");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (checkCameraPermission()) {
                            openCamera();
                        } else {
                            requestCameraPermission();
                        }
                        break;
                    case 1:
                        if (checkStoragePermission()) {
                            openGallery();
                        } else {
                            requestStoragePermission();
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    private void uploadToFireStore() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String eventName = eventNameEditText.getText().toString();
        String eventDetails = eventDetailsEditText.getText().toString();
        String eventDate = setEventDateTextView.getText().toString();
        String eventTime = setEventTimeTextView.getText().toString();
        String userTagged = taggedUserDisplayTextView.getText().toString();
        String locationTagged = searchLocationDisplayTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> events = new HashMap<>();
        events.put("createdBy", loggedInUserId);
        events.put("eventName", eventName);
        events.put("eventTime", eventTime);
        events.put("eventDate", eventDate);
        events.put("eventDetails", eventDetails);
        events.put("userTagged", userTagged);
        events.put("locationTagged", locationTagged);
        events.put("createdAt", createdAt);

        //Add a new document with a generated ID
        db.collection("events")
                .add(events)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        eventsDocId = documentReference.getId();
                        Log.d("yoo", "DocumentSnapshot added with ID: " + documentReference.getId());
                        hideProgressDialog();
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("yoo", "Error adding document", e);
                    }
                });
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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

    private void showProgressDialog(String s) {
        if (progressDialog == null) {
            progressDialog = new Dialog(this);
            progressDialog.setContentView(R.layout.custom_progress_dialog);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView progressMessageTextView = progressDialog.findViewById(R.id.progressMessageTextView);
        if (progressMessageTextView != null) {
            progressMessageTextView.setText(s);
        }

        progressDialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    private void fetchUserInfoFromFirestore(String userId) {
        db.collection("user")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            Users user = userDocument.toObject(Users.class);

                            Glide.with(this)
                                    .load(user.getProfileImage())
                                    .into(userProfilePic);
                            userNameTextView.setText(user.getName());

                        }
                    } else {
                        Log.e("yoo", "Error getting user's info.", task.getException());
                    }
                });
    }

}