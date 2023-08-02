package edu.northeastern.pawsomepals.utils;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.pawsomepals.models.Users;

public class FirebaseUtil {
    public interface DataCallback {
        void onUserReceived(Users user);

        void onImageUriReceived(String imageUrl);

        void onError(Exception exception);

        void onDismiss();
    }

    public static void fetchUserInfoFromFirestore(String userId, DataCallback dataCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().size() > 0) {
                        Users user = task.getResult().getDocuments().get(0).toObject(Users.class);
                        dataCallback.onUserReceived(user);
                    } else {
                        Log.e("yoo", "Error getting user's info.", task.getException());
                    }
                });
    }

    public static void uploadImageToStorage(Uri cameraImageUri, Uri galleryImageUri, String postType, DataCallback dataCallback) {
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

    public static void updateFeedWithCommentCount(String postType,String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(postType)
                .whereEqualTo("feedItemId", postId)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            Integer currentCommentCount = documentSnapshot.getLong("commentCount") != null
                                    ? documentSnapshot.getLong("commentCount").intValue() : 0;
                            int newCommentCount = currentCommentCount + 1;
                            documentSnapshot.getReference().update("commentCount", newCommentCount)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });
                        } else {
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }

    public static void createCollectionInFirestore(Map<String, Object> feedTypeObj, String feedType, DataCallback dataCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //Add a new document with a generated ID
        db.collection(feedType)
                .add(feedTypeObj)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("yoo", "DocumentSnapshot added with ID: " + documentReference.getId());
                        dataCallback.onDismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("yoo", "Error adding document", e);
                    }
                });
    }


}
