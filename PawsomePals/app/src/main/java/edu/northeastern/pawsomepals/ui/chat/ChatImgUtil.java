package edu.northeastern.pawsomepals.ui.chat;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ComponentActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class ChatImgUtil {
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 3;

    public static void showPhotoSelectionDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Upload Photo");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (checkCameraPermission(activity)) {
                            openCamera(activity);
                        } else {
                            requestCameraPermission(activity);
                        }
                        break;
                    case 1:
                        if (checkStoragePermission(activity)) {
                            openGallery(activity);
                        } else {
                            requestStoragePermission(activity);
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    public static boolean checkCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
    }

    public static boolean checkStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_GALLERY);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
        }
    }

    public static void openCamera(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            activity.startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
        } catch (ActivityNotFoundException e) {

        }
    }

    public static void openGallery(Activity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
    }

    public static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }


    public static Uri saveCameraImageToFile(Intent data, Activity activity) {
        Bundle extras = data.getExtras();
        Bitmap cameraImageBitmap = (Bitmap) extras.get("data");
        int targetWidth = 400;
        int targetHeight = 400;
//        int targetHeight = (int) (cameraImageBitmap.getHeight() * (targetWidth / (double) cameraImageBitmap.getWidth()));
        cameraImageBitmap = Bitmap.createScaledBitmap(cameraImageBitmap, targetWidth, targetHeight, true);
        String imageFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            cameraImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(imageFile);
    }
    public static void uploadImageToStorage(Uri cameraImageUri, Uri galleryImageUri, String postType, FirebaseUtil.DataCallback dataCallback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri uploadImageUri = null;
        if (cameraImageUri != null) {
            uploadImageUri = cameraImageUri;
        } else if (galleryImageUri != null) {
            uploadImageUri = galleryImageUri;
        }

        if (uploadImageUri != null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = postType + "_image_" + timestamp + ".jpg";
            StorageReference imageRef = storageRef.child(postType + "_images/" + imageName);
            UploadTask uploadTask = imageRef.putFile(uploadImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                            dataCallback.onImageUriReceived(downloadUri.toString());
                        }
                    } else {
                        dataCallback.onError(task.getException());
                    }
                }
            });
        } else {
            dataCallback.onDismiss();
        }
    }
}
