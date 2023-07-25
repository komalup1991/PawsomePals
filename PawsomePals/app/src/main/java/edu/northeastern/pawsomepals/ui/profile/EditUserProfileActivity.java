package edu.northeastern.pawsomepals.ui.profile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.login.HomeActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditUserProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int PERMISSIONS_REQUEST_STORAGE = 4;
    private boolean shouldShowDatePicker = false;
    private boolean datePickerOpened = false;
    private boolean isGenderSelected = false;

    private ImageView imageProfile;
    private EditText editTextName, editTextDOB;
    private RadioGroup radioGroupGender;
    private Button btnSave;
    private Uri photoUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference profileImageRef;
    private FirebaseUser currentUser;
    private String userId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        profileImageRef = firebaseStorage.getReference().child("user_profile_images");

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        imageProfile = findViewById(R.id.imageProfile);
        editTextName = findViewById(R.id.editTextName);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        editTextDOB = findViewById(R.id.editTextDOB);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        if (!checkStoragePermission()) {
            requestStoragePermission();
        }

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isGenderSelected = true; // Set the flag when the user selects a gender
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooserDialog();
            }
        });

        editTextDOB.setInputType(InputType.TYPE_NULL);
        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFirebaseStorage();
            }
        });
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
                    Toast.makeText(this, "Camera permissions denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Storage permissions granted, proceed with image pick
                    handleImagePickFromGallery();
                } else {
                    // Storage permissions denied, show a message or handle accordingly
                    Toast.makeText(this, "Storage permissions denied.", Toast.LENGTH_SHORT).show();
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
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            // Request camera permissions if not granted
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_STORAGE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
    }



    private void saveDataToFirebaseStorage() {
        String name = editTextName.getText().toString().trim();
        String gender = getSelectedGender();
        String dob = editTextDOB.getText().toString().trim();

        // Check if all the required fields are filled
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(dob) ||
                photoUri == null) {
            Toast.makeText(this, "Please fill all the fields and add a profile picture.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "user_image_" + timestamp + ".jpg";

        StorageReference photoRef = profileImageRef.child("user_images/" + imageName);
        UploadTask uploadTask = photoRef.putFile(photoUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return photoRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the download URL of the uploaded profile picture
                Uri downloadUri = task.getResult();

                // Create a map to store the user data
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("dob", dob);
                userData.put("gender", gender);
                userData.put("profileImage", downloadUri.toString());

                // Save the user data to Firebase Firestore
                firebaseFirestore.collection("user")
                        .document(firebaseAuth.getUid())
                        .update(userData)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                            navigateToEditDogProfileActivity();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to upload profile picture. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void navigateToEditDogProfileActivity() {
        Intent intent = new Intent(EditUserProfileActivity.this, HomeActivity.class);
        startActivity(intent);
    }


    private void openImageChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Handle the selected date
                        editTextDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private String getSelectedGender() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        if (radioButton != null) {
            return radioButton.getText().toString();
        }
        return "";
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getParcelable("photoUri");
            if (photoUri != null) {
                imageProfile.setImageURI(photoUri);
            }
            // Restore other values if needed
            String name = savedInstanceState.getString("name");
            String dob = savedInstanceState.getString("dob");
            String gender = savedInstanceState.getString("gender");
            shouldShowDatePicker = savedInstanceState.getBoolean("shouldShowDatePicker");
            isGenderSelected = savedInstanceState.getBoolean("isGenderSelected");

            editTextName.setText(name);
            editTextDOB.setText(dob);

            // Restore gender selection
            if (isGenderSelected) {
                int radioButtonId = (gender != null && gender.equals("Male")) ? R.id.radioButtonMale : R.id.radioButtonFemale;
                radioGroupGender.check(radioButtonId);
            }

            // Check if the DatePicker should be shown
            if (shouldShowDatePicker) {
                // Get the year, month, and day from the existing date value in editTextDOB
                String[] dateParts = dob.split("/");
                if (dateParts.length == 3) {
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-indexed
                    int year = Integer.parseInt(dateParts[2]);
                    showDatePickerWithValues(year, month, day);
                }
            }

            // Restore the isGenderSelected flag
            isGenderSelected = savedInstanceState.getBoolean("isGenderSelected");
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("photoUri", photoUri);

        String name = editTextName.getText().toString();
        String dob = editTextDOB.getText().toString();
        String gender = getSelectedGender();
        String datePickerValue = editTextDOB.getText().toString();

        outState.putString("name", name);
        outState.putString("dob", dob);
        outState.putString("gender", gender);
        outState.putString("datePickerValue", datePickerValue);
        outState.putBoolean("shouldShowDatePicker", editTextDOB.hasFocus() && !datePickerOpened);
        outState.putBoolean("isGenderSelected", isGenderSelected); // Save the isGenderSelected flag
    }

    private void showDatePickerWithValues(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Handle the selected date
                        editTextDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

        //datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Handle image capture from the camera
                if (data != null && data.getExtras() != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    imageProfile.setImageBitmap(bitmap);

                    // Convert the Bitmap to a URI and set it to the photoUri
                    photoUri = getImageUriFromBitmap(bitmap);
                } else {
                    // Handle the case when data is null or the image capture failed
                    Toast.makeText(this, "Failed to capture image from camera.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                // Handle image pick from the gallery
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    photoUri = selectedImageUri;
                    imageProfile.setImageURI(photoUri);
                }
            }
        }
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }



}
