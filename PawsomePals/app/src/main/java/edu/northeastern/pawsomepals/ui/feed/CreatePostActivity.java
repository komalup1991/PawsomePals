package edu.northeastern.pawsomepals.ui.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;

public class CreatePostActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CircleImageView userProfilePic;
    private TextView userNameTextView, tagPeopleTextView, addLocationTextView, taggedUserDisplayTextView;
    private EditText captionEditTextView, postContentEditTextView;
    private Dialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String loggedInUserId;
    private String postDocId;
    private Map<String, Users> allUsers;
    private List<Users> selectedUsers;
    private AutoCompleteTextView searchLocationDisplayTextView;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        allUsers = new HashMap<>();
        selectedUsers = new ArrayList<>();
        toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        captionEditTextView = findViewById(R.id.captionEditTextView);
        postContentEditTextView = findViewById(R.id.postContentEditTextView);
        tagPeopleTextView = findViewById(R.id.tagPeopleTextView);
        taggedUserDisplayTextView = findViewById(R.id.taggedUserDisplayTextView);
        addLocationTextView = findViewById(R.id.addLocationTextView);
        searchLocationDisplayTextView = findViewById(R.id.searchLocationDisplayTextView);

        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        loggedInUserId = currentUser.getUid();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Post");
        }
        fetchUserInfoFromFirestore(loggedInUserId);

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
                showProgressDialog("Your post is being saved...");
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

    private void uploadToFireStore() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String caption = captionEditTextView.getText().toString();
        String postContent = postContentEditTextView.getText().toString();
        String userTagged = taggedUserDisplayTextView.getText().toString();
        String locationTagged = searchLocationDisplayTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> post = new HashMap<>();
        post.put("createdBy", loggedInUserId);
        post.put("caption", caption);
        post.put("postContent", postContent);
        post.put("userTagged", userTagged);
        post.put("locationTagged", locationTagged);
        post.put("createdAt", createdAt);
        post.put("username",userNameToSaveInFeed);
        post.put("userProfileImage",userProfileUrlToSaveInFeed);
        post.put("type",4);

        //Add a new document with a generated ID
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        postDocId = documentReference.getId();
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
                            userNameToSaveInFeed = user.getName();
                            userProfileUrlToSaveInFeed = user.getProfileImage();

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