package edu.northeastern.pawsomepals.ui.feed;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
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
    private Uri selectedImageUri;
    private TextView setServingSizeTextView,setPrepTextView,setCookTextView;
    private Button saveButton,cancelButton;

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


        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoSelectionDialog();
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
                showTimePickerDialog("Prep Time", "How long does it take to prepare this recipe?", setPrepTextView);
            }
        });

        setCookTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog("Cook Time", "How long does it take to cook this recipe?", setCookTextView);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToFireStore();

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


        Recipe recipe = new Recipe(recipeTitle,recipeImg, recipeDescription, recipeIngredients,recipeServing,recipePrepTime,recipeCookTime);
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
                        Log.d("yoo", "DocumentSnapshot added with ID: " + documentReference.getId());
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

    private void showTimePickerDialog(String title, String message, TextView textView) {
        Dialog dialog = new Dialog(CreateRecipeActivity.this);
        dialog.setContentView(R.layout.dialog_time_picker);

        TextView titleTextView = dialog.findViewById(R.id.titleTextView);
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);
        NumberPicker hourTimePicker = dialog.findViewById(R.id.hourPicker);
        NumberPicker minTimePicker = dialog.findViewById(R.id.minutePicker);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        hourTimePicker.setMaxValue(23);
        hourTimePicker.setMinValue(0);
        hourTimePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return i + " hours";
            }
        });

        hourTimePicker.setWrapSelectorWheel(true);


        minTimePicker.setMaxValue(59);
        minTimePicker.setMinValue(0);

        minTimePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return i + " min";
            }
        });

        minTimePicker.setWrapSelectorWheel(true);
        titleTextView.setText(title);
        messageTextView.setText(message);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = hourTimePicker.getValue();
                int minutes = minTimePicker.getValue();


                String timeText = String.format(Locale.getDefault(), "%d hours %d minutes", hours, minutes);

                textView.setText(timeText);
                dialog.dismiss();
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
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                        new ActivityResultCallback<Uri>() {
                            @Override
                            public void onActivityResult(Uri uri) {
                                // Handle the returned Uri
                                selectedImageUri = uri;
                                // Upload the image to Firebase Storage
                                uploadImageToStorage();
                            }
                        });
                startActivityForResult(takePhotoIntent, REQUEST_CODE_CAMERA);
            }
        }
    }

    //  Use registerForActivityResult(ActivityResultContract, ActivityResultCallback) passing in a StartActivityForResult object for the ActivityResultContract.

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                recipeImageView.setImageBitmap(bitmap);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    recipeImageView.setImageBitmap(bitmap);
              //      uploadImageToStorage(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

        TextView titleTextView = dialog.findViewById(R.id.titleTextView);
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);
        SeekBar seekBar = dialog.findViewById(R.id.seekBar);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        TextView valueTextView = dialog.findViewById(R.id.valueTextView);

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
                int selectedValue = seekBar.getProgress() + 1;
                String servingsText = getString(R.string.servings_placeholder, selectedValue);
                setServingSizeTextView.setText(servingsText);
                dialog.dismiss();
            }
        });

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
        if (selectedImageUri != null) {
            // Create a unique filename for the image
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "recipe_image_" + timestamp + ".jpg";

            // Create a reference to the image file in Firebase Storage
            StorageReference imageRef = storageRef.child("recipe_images/" + imageName);

            // Upload the image file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(selectedImageUri);

            // Retrieve the download URL of the uploaded image
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            // Save the download URL in the recipe object or Firestore document
                            String imageUrl = downloadUri.toString();
                            // Update the 'img' field of the recipe object or Firestore document
                            // recipe.setImg(imageUrl);
                            // newRecipeRef.set(recipe)...

                            // You can display the image or perform further actions here
                            Glide.with(CreateRecipeActivity.this).load(downloadUri).into(recipeImageView);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(CreateRecipeActivity.this, "Error uploading image: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }





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
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

//    new Thread(new Runnable() {
//        @Override
//        public void run() {
//            // Get the image from the image view.
//            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//            // Upload the image to the server.
//        }
//    }).start();
}



