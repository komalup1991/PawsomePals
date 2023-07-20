package edu.northeastern.pawsomepals.ui.feed;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;

public class CreateRecipeActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ImageView recipeImageView,selectPhoto,deleteImageView,editImageView;
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 3;
    private EditText recipeNameEditText;
    private EditText descriptionEditTextView;
    private EditText ingredientsEditTextView;
    private String currentPhotoPath;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private Bitmap cameraImage;
    private Uri galleryImageUri,cameraImageUri;
    private TextView setServingSizeTextView,setPrepTextView,setCookTextView,valueTextView;
    private Button saveButton,cancelButton;

    private Recipe recipe;
    private String recipeDocId;
    private boolean isEditImageDialogVisible = false;
    private boolean isDeleteConfirmationDialogVisible = false;
    private boolean isQuantityPickerDialogVisible = false;
    private int selectedValue;
    private boolean isPrepTimeDialogVisible = false;
    private boolean isCookTimeDialogVisible = false;
    private int selectedPrepHours ;
    private int selectedPrepMinutes;
    private int selectedCookHours;
    private int selectedCookMinutes;
    TextView recipeNameMandatoryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        //Firebase
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //UI
        toolbar = findViewById(R.id.toolbar);
        recipeNameEditText = findViewById(R.id.recipeNameEditText);
        recipeNameMandatoryTextView = findViewById(R.id.recipeNameMandatoryTextView);

        //photo upload
        recipeImageView = findViewById(R.id.recipeImageView);
        selectPhoto = findViewById(R.id.addPhotoImageView);
        deleteImageView = findViewById(R.id.deleteImageView);
        editImageView = findViewById(R.id.editImageView);

        descriptionEditTextView = findViewById(R.id.descriptionEditTextView);
        ingredientsEditTextView = findViewById(R.id.ingredientsEditTextView);
        setServingSizeTextView = findViewById(R.id.setServingSizeTextView);
        setPrepTextView = findViewById(R.id.setPrepTextView);
        setCookTextView = findViewById(R.id.setCookTextView);

        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Recipe");
        }

     //   saveButton.setEnabled(false);

        recipeNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
              //  saveButton.setEnabled(!editable.toString().isEmpty());

                if (editable.toString().isEmpty()) {
                    // Show the recipeNameMandatoryTextView (red asterisk) if the field is empty
                    recipeNameMandatoryTextView.setVisibility(View.VISIBLE);
                } else {
                    // Hide the recipeNameMandatoryTextView if the field has content
                    recipeNameMandatoryTextView.setVisibility(View.GONE);
                }
            }
        });


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

        setServingSizeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuantityPickerDialog();
            }
        });

        setPrepTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPrepTimeDialogVisible = true;
                showTimePickerDialog("Prep Time", "How long does it take to prepare this recipe?", setPrepTextView);
            }
        });

        setCookTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCookTimeDialogVisible = true;
                showTimePickerDialog("Cook Time", "How long does it take to cook this recipe?", setCookTextView);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recipeNameEditText.getText().toString().isEmpty()){
                    recipeNameMandatoryTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(CreateRecipeActivity.this, "This field is mandatory.", Toast.LENGTH_SHORT).show();}
                else{
                uploadToFireStore();
                uploadImageToStorage();
                }


            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO add confirmation dialog
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
                            recipeImageView.setImageDrawable(null);
                            selectPhoto.setVisibility(View.VISIBLE);
                            galleryImageUri = null;
                            cameraImageUri = null;
    }


    private void uploadToFireStore() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        String loggedInUserId = currentUser.getUid();
        String recipeTitle = recipeNameEditText.getText().toString();
        String recipeImg = "";
        String recipeDescription = descriptionEditTextView.getText().toString();
        String recipeIngredients = ingredientsEditTextView.getText().toString();
        String recipeServing = setServingSizeTextView.getText().toString();
        String recipePrepTime = setPrepTextView.getText().toString();
        String recipeCookTime = setCookTextView.getText().toString();


        recipe = new Recipe(recipeTitle,recipeImg, recipeDescription, recipeIngredients,recipeServing,recipePrepTime,recipeCookTime);
        Map<String,Object> recipeCollection = new HashMap<>();
        recipeCollection.put("createdBy",loggedInUserId);
        recipeCollection.put("title",recipe.getName());
        recipeCollection.put("img",recipe.getImg());
        recipeCollection.put("desc",recipe.getDesc());
        recipeCollection.put("ingredients",recipe.getIngredients());
        recipeCollection.put("serving",recipe.getServingSize());
        recipeCollection.put("prepTime",recipe.getPrepTime());
        recipeCollection.put("cookTime",recipe.getCookTime());
        //Add a new document with a generated ID
        db.collection("recipes")
                .add(recipeCollection)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                         recipeDocId = documentReference.getId();
                        Log.d("yoo", "DocumentSnapshot added with ID: " + documentReference.getId());
                      //  finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("yoo", "Error adding document", e);
                    }
                });
    }

    private void showTimePickerDialog(String title, String message, TextView textView) {
        Dialog dialog = new Dialog(CreateRecipeActivity.this);
        dialog.setContentView(R.layout.dialog_time_picker);

        TextView titleTextView = dialog.findViewById(R.id.titleTextView);
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);
        NumberPicker hourTimePicker = dialog.findViewById(R.id.hourPicker);
        NumberPicker minTimePicker = dialog.findViewById(R.id.minutePicker);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        hourTimePicker.setMinValue(0);
        hourTimePicker.setMaxValue(23);
     //   hourTimePicker.setValue(1);

        View editView = hourTimePicker.getChildAt(0);

        if (editView instanceof EditText) {
            // Remove default input filter
            ((EditText) editView).setFilters(new InputFilter[0]);
        }

        hourTimePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return i + " hours";
            }
        });
        hourTimePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        hourTimePicker.setWrapSelectorWheel(true);


        minTimePicker.setMaxValue(59);
        minTimePicker.setMinValue(0);

        View minEditView = minTimePicker.getChildAt(0);

        if (minEditView instanceof EditText) {
            // Remove default input filter
            ((EditText) minEditView).setFilters(new InputFilter[0]);
        }

        minTimePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return i + " min";
            }
        });
        minTimePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        minTimePicker.setWrapSelectorWheel(true);
        titleTextView.setText(title);
        messageTextView.setText(message);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = hourTimePicker.getValue();
                int minutes = minTimePicker.getValue();
                String timeText = String.format(Locale.getDefault(), "%d hours %d mins", hours, minutes);
                textView.setText(timeText);
                if (textView == setPrepTextView) {
                    selectedPrepHours = hours;
                    selectedPrepMinutes = minutes;
                } else if (textView == setCookTextView) {
                    selectedCookHours = hours;
                    selectedCookMinutes = minutes;
                }
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (textView == setPrepTextView) {
                    isPrepTimeDialogVisible = false;
                } else if (textView == setCookTextView) {
                    isCookTimeDialogVisible = false;
                }
            }
        });

        dialog.show();
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

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA ) {
                galleryImageUri = null;
                cameraImageUri=saveCameraImageToFile(data);
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(cameraImageUri).centerCrop().into(recipeImageView);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                cameraImageUri = null;
                galleryImageUri = data.getData();
                selectPhoto.setVisibility(View.GONE);
                Glide.with(this).load(galleryImageUri).centerCrop().into(recipeImageView);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showQuantityPickerDialog() {
        Dialog dialog = new Dialog(CreateRecipeActivity.this);
        dialog.setContentView(R.layout.dialog_quantity_picker);

        SeekBar seekBar = dialog.findViewById(R.id.seekBar);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        valueTextView = dialog.findViewById(R.id.valueTextView);

        // Set initial value for number picker
        int currentValue;
        try {
            currentValue = Integer.parseInt(setServingSizeTextView.getText().toString());
        } catch (NumberFormatException e) {
            currentValue = 1; // Set a default value if parsing fails
        }

        seekBar.setProgress(currentValue - 1);
        valueTextView.setText(String.valueOf(currentValue));
        // Update the value text view when the seek bar progress changes
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int selectedValue = progress + 1;
                valueTextView.setText(String.valueOf(selectedValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedValue = seekBar.getProgress() + 1;

                String servingsText = getString(R.string.servings_placeholder, selectedValue);
                setServingSizeTextView.setText(servingsText);
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isQuantityPickerDialogVisible = false;
            }
        });

        isQuantityPickerDialogVisible = true;
        dialog.show();
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
    private void uploadImageToStorage() {
        Uri uploadImageUri = null;
        if (cameraImageUri!=null) uploadImageUri=cameraImageUri;
        else if(galleryImageUri!=null) uploadImageUri=galleryImageUri;

        if (uploadImageUri != null) {
            // Create a unique filename for the image
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "recipe_image_" + timestamp + ".jpg";

            // Create a reference to the image file in Firebase Storage
            StorageReference imageRef = storageRef.child("recipe_images/" + imageName);

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
                          //  Glide.with(CreateRecipeActivity.this).load(downloadUri).into(recipeImageView);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(CreateRecipeActivity.this, "Error uploading image: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }





    }

    private void updateDbWithImg(String imageUrl) {
        DocumentReference recipeRef = db.collection("recipes").document(recipeDocId);

        recipeRef
                .update("img",imageUrl )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("yoo", "DocumentSnapshot successfully updated!");
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String recipeName = recipeNameEditText.getText().toString();
        String description = descriptionEditTextView.getText().toString();
        String ingredients = ingredientsEditTextView.getText().toString();

        outState.putString("recipeName", recipeName);
        outState.putString("description", description);
        outState.putString("ingredients", ingredients);

        // Save the selected image URI
        outState.putParcelable("selectedImageUri", galleryImageUri);
        outState.putParcelable("cameraImageUri", cameraImageUri);

        //visibility restore
        outState.putInt("addPhotoImageViewVisibility",selectPhoto.getVisibility());

        outState.putBoolean("isEditImageDialogVisible", isEditImageDialogVisible);
        outState.putBoolean("isDeleteConfirmationDialogVisible", isDeleteConfirmationDialogVisible);

        outState.putBoolean("isQuantityPickerDialogVisible", isQuantityPickerDialogVisible);
        outState.putInt("selectedValue", selectedValue);

        if (valueTextView != null) {
            outState.putString("valueTextViewText", valueTextView.getText().toString());
        }

        outState.putBoolean("isPrepTimeDialogVisible", isPrepTimeDialogVisible);
        outState.putBoolean("isCookTimeDialogVisible", isCookTimeDialogVisible);
        outState.putInt("selectedPrepHours", selectedPrepHours);
        outState.putInt("selectedPrepMinutes", selectedPrepMinutes);
        outState.putInt("selectedCookHours", selectedCookHours);
        outState.putInt("selectedCookMinutes", selectedCookMinutes);



    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String recipeName = savedInstanceState.getString("recipeName");
        String description = savedInstanceState.getString("description");
        String ingredients = savedInstanceState.getString("ingredients");

        recipeNameEditText.setText(recipeName);
        descriptionEditTextView.setText(description);
        ingredientsEditTextView.setText(ingredients);

        // Restore the selected image URI
        galleryImageUri = savedInstanceState.getParcelable("selectedImageUri");
        cameraImageUri = savedInstanceState.getParcelable("cameraImageUri");

        if (galleryImageUri != null) {
            Glide.with(this).load(galleryImageUri).centerCrop().into(recipeImageView);
        }

        else if (cameraImageUri != null) {
            Glide.with(this).load(cameraImageUri).centerCrop().into(recipeImageView);
        }

        //Restore visibility
        selectPhoto.setVisibility(savedInstanceState.getInt("addPhotoImageViewVisibility"));
        isEditImageDialogVisible = savedInstanceState.getBoolean("isEditImageDialogVisible");
        if (isEditImageDialogVisible) {
            showEditImageDialog();
        }
        isDeleteConfirmationDialogVisible = savedInstanceState.getBoolean("isDeleteConfirmationDialogVisible");
        if (isDeleteConfirmationDialogVisible) {
            showDeleteConfirmationDialog();
        }

        isQuantityPickerDialogVisible = savedInstanceState.getBoolean("isQuantityPickerDialogVisible", false);
        selectedValue = savedInstanceState.getInt("selectedValue");
        if (isQuantityPickerDialogVisible) {
            showQuantityPickerDialog();}
            // Restore the value displayed in the valueTextView
        String savedValueText = savedInstanceState.getString("valueTextViewText");
        if (savedValueText != null && valueTextView != null) {
            valueTextView.setText(savedValueText);
        }
        // Update the setServingSizeTextView
        String servingsText = getString(R.string.servings_placeholder, selectedValue);
        setServingSizeTextView.setText(servingsText);

        isPrepTimeDialogVisible = savedInstanceState.getBoolean("isPrepTimeDialogVisible");
        isCookTimeDialogVisible = savedInstanceState.getBoolean("isCookTimeDialogVisible");
        selectedPrepHours = savedInstanceState.getInt("selectedPrepHours");
        selectedPrepMinutes = savedInstanceState.getInt("selectedPrepMinutes");
        selectedCookHours = savedInstanceState.getInt("selectedCookHours");
        selectedCookMinutes = savedInstanceState.getInt("selectedCookMinutes");
        String prepTimeText = String.format(Locale.getDefault(), "%d hours %d mins", selectedPrepHours, selectedPrepMinutes);
        String cookTimeText = String.format(Locale.getDefault(), "%d hours %d mins", selectedCookHours, selectedCookMinutes);
        setPrepTextView.setText(prepTimeText);
        setCookTextView.setText(cookTimeText);


        if (isPrepTimeDialogVisible) {
            showTimePickerDialog("Prep Time", "How long does it take to prepare this recipe?", setPrepTextView);
        }
        if (isCookTimeDialogVisible) {
            showTimePickerDialog("Cook Time", "How long does it take to cook this recipe?", setCookTextView);
        }


    }


}



