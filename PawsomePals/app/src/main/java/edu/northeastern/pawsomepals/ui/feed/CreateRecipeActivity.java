package edu.northeastern.pawsomepals.ui.feed;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.ImageUtil;

public class CreateRecipeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 3;
    TextView recipeNameMandatoryTextView;
    private CircleImageView userProfilePic;
    private TextView userNameTextView;
    private ImageView recipeImageView;
    private ImageView selectPhoto;
    private EditText recipeNameEditText;
    private EditText descriptionEditTextView;
    private EditText ingredientsEditTextView;
    private String currentPhotoPath;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri galleryImageUri, cameraImageUri;
    private TextView setServingSizeTextView, setPrepTextView, setCookTextView, valueTextView;
    private String recipeDocId;
    private boolean isEditImageDialogVisible = false;
    private boolean isDeleteConfirmationDialogVisible = false;
    private boolean isQuantityPickerDialogVisible = false;
    private int selectedValue;
    private boolean isPrepTimeDialogVisible = false;
    private boolean isCookTimeDialogVisible = false;
    private int selectedPrepHours;
    private int selectedPrepMinutes;
    private int selectedCookHours;
    private int selectedCookMinutes;
    private Dialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    String loggedInUserId;
    private Context context;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);


        //Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        loggedInUserId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        //UI
        Toolbar toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        recipeNameEditText = findViewById(R.id.recipeNameEditText);
        recipeNameMandatoryTextView = findViewById(R.id.recipeNameMandatoryTextView);

        //photo upload
        recipeImageView = findViewById(R.id.recipeImageView);
        selectPhoto = findViewById(R.id.addPhotoImageView);
        ImageView deleteImageView = findViewById(R.id.deleteImageView);
        ImageView editImageView = findViewById(R.id.editImageView);


        descriptionEditTextView = findViewById(R.id.descriptionEditTextView);
        ingredientsEditTextView = findViewById(R.id.ingredientsEditTextView);
        setServingSizeTextView = findViewById(R.id.setServingSizeTextView);
        setPrepTextView = findViewById(R.id.setPrepTextView);
        setCookTextView = findViewById(R.id.setCookTextView);

        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Recipe");
        }
        FirebaseUtil.fetchUserInfoFromFirestore(loggedInUserId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed=user.getName();
                userProfileUrlToSaveInFeed=user.getProfileImage();

                Glide.with(CreateRecipeActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                userNameTextView.setText(user.getName());
            }
        });

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.showPhotoSelectionDialog(CreateRecipeActivity.this);
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

                if (recipeNameEditText.getText().toString().isEmpty()) {
                    recipeNameEditText.setError("This field is required");
                    Toast.makeText(CreateRecipeActivity.this, "Recipe Name is mandatory.", Toast.LENGTH_SHORT).show();
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
                DialogHelper.showProgressDialog("Your Recipe is being saved...", progressDialog, CreateRecipeActivity.this);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.showCancelConfirmationDialog(context, CreateRecipeActivity.this);
             //   finish();
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
                        if (ImageUtil.checkCameraPermission(CreateRecipeActivity.this)) {
                            ImageUtil.openCamera(CreateRecipeActivity.this);
                        } else {
                            ImageUtil.requestCameraPermission(CreateRecipeActivity.this);
                        }
                        break;
                    case 1:
                        if (ImageUtil.checkStoragePermission(CreateRecipeActivity.this)) {
                            ImageUtil.openGallery(CreateRecipeActivity.this);
                        } else {
                            ImageUtil.requestStoragePermission(CreateRecipeActivity.this);
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


    private void createFeedMap(String imageUrlFromFirebaseStorage) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String recipeTitle = recipeNameEditText.getText().toString();
        String recipeDescription = descriptionEditTextView.getText().toString();
        String recipeIngredients = ingredientsEditTextView.getText().toString();
        String recipeServing = setServingSizeTextView.getText().toString();
        String recipePrepTime = setPrepTextView.getText().toString();
        String recipeCookTime = setCookTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> recipeCollection = new HashMap<>();
        recipeCollection.put("createdBy", loggedInUserId);
        recipeCollection.put("title", recipeTitle);
        recipeCollection.put("desc", recipeDescription);
        recipeCollection.put("ingredients", recipeIngredients);
        recipeCollection.put("serving", recipeServing);
        recipeCollection.put("prepTime", recipePrepTime);
        recipeCollection.put("cookTime", recipeCookTime);
        recipeCollection.put("createdAt", createdAt);
        recipeCollection.put("img", imageUrlFromFirebaseStorage);

        FirebaseUtil.createCollectionInFirestore(recipeCollection,"recipes" ,new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);
                finish();
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
        View editView = hourTimePicker.getChildAt(0);

        if (editView instanceof EditText) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                galleryImageUri = null;
                cameraImageUri = ImageUtil.saveCameraImageToFile(data,this);
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
        outState.putInt("addPhotoImageViewVisibility", selectPhoto.getVisibility());

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
        } else if (cameraImageUri != null) {
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
            showQuantityPickerDialog();
        }
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



