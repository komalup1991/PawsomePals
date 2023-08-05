package edu.northeastern.pawsomepals.ui.feed;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.layout.TagLocationLayout;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

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
    private Context context;

    private TagLocationLayout tagLocationLayout;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;

    private LatLng currentLatLng;
    private String locationTagged;

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
        tagLocationLayout = findViewById(R.id.tag_location_layout);
        tagLocationLayout.bindView(this, new TagLocationLayout.OnLocationFetchListener() {

            @Override
            public void onLocation(LatLng latLng, String locationTagged) {
                currentLatLng = latLng;
                CreatePostActivity.this.locationTagged = locationTagged;
            }
        });

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
        FirebaseUtil.fetchUserInfoFromFirestore(loggedInUserId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed=user.getName();
                userProfileUrlToSaveInFeed=user.getProfileImage();

                Glide.with(CreatePostActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                userNameTextView.setText(user.getName());
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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (captionEditTextView.getText().toString().isEmpty()) {
                    captionEditTextView.setError("This field is required");
                    Toast.makeText(CreatePostActivity.this, "Post Caption is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (postContentEditTextView.getText().toString().isEmpty()) {
                    postContentEditTextView.setError("This field is required");
                    Toast.makeText(CreatePostActivity.this, "Post Detail is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                createFeedMap();
                DialogHelper.showProgressDialog("Your post is being saved...",progressDialog,CreatePostActivity.this);
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

    private void createFeedMap() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String caption = captionEditTextView.getText().toString();
        String postContent = postContentEditTextView.getText().toString();
        String userTagged = taggedUserDisplayTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> post = new HashMap<>();
        post.put("createdBy", loggedInUserId);
        post.put("caption", caption);
        post.put("postContent", postContent);
        post.put("userTagged", userTagged);
        post.put("locationTagged", locationTagged);
        post.put("latLng", currentLatLng);
        post.put("createdAt", createdAt);
        post.put("username",userNameToSaveInFeed);
        post.put("userProfileImage",userProfileUrlToSaveInFeed);
        post.put("type",4);
        post.put("feedItemId", UUID.randomUUID().toString());

        FirebaseUtil.createCollectionInFirestore(post,"posts" ,new BaseDataCallback() {
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