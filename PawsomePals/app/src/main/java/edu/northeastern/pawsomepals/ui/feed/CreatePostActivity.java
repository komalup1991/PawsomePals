package edu.northeastern.pawsomepals.ui.feed;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.layout.TaggingOptionsLayout;
import edu.northeastern.pawsomepals.utils.ActivityHelper;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class CreatePostActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CircleImageView userProfilePic;
    private TextView userNameTextView;
    private EditText captionEditTextView, postContentEditTextView;
    private Dialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String loggedInUserId;

    private TaggingOptionsLayout taggingOptionsLayout;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;

    private LatLng currentLatLng;
    private String locationTagged;

    private String usersTagged;
    private Post existingFeedItem;
    private String currentFeedItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        captionEditTextView = findViewById(R.id.captionEditTextView);
        postContentEditTextView = findViewById(R.id.postContentEditTextView);
        taggingOptionsLayout = findViewById(R.id.tag_location_layout);
        taggingOptionsLayout.bindView(this, new TaggingOptionsLayout.OnTaggedDataFetchListener() {

            @Override
            public void onLocation(LatLng latLng, String locationTagged) {
                currentLatLng = latLng;
                CreatePostActivity.this.locationTagged = locationTagged;
            }

            @Override
            public void onTaggedUsersGet(String usersTagged) {
                CreatePostActivity.this.usersTagged = usersTagged;
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

        existingFeedItem = (Post)getIntent().getSerializableExtra("existingFeedItem");
        if(existingFeedItem!=null){
            captionEditTextView.setText(existingFeedItem.getCaption());
            postContentEditTextView.setText(existingFeedItem.getPostContent());
            if (existingFeedItem.getUserTagged() != null) {
                taggingOptionsLayout.setTagPeopleTextView(existingFeedItem.getUserTagged());
            }

            if (existingFeedItem.getLocationTagged() != null) {
                taggingOptionsLayout.setTagLocationTextView(existingFeedItem.getLocationTagged());
            }

            usersTagged = existingFeedItem.getUserTagged();
            locationTagged = existingFeedItem.getLocationTagged();

            currentFeedItemId = existingFeedItem.getFeedItemId();
        } else {
            currentFeedItemId = UUID.randomUUID().toString();
        }

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
              //  finish();
            }
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

    private void createFeedMap() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String caption = captionEditTextView.getText().toString();
        String postContent = postContentEditTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> post = new HashMap<>();
        post.put("createdBy", loggedInUserId);
        post.put("caption", caption);
        post.put("postContent", postContent);
        post.put("userTagged", usersTagged);
        post.put("locationTagged", locationTagged);
        post.put("latLng", currentLatLng);
        post.put("createdAt", createdAt);
        post.put("username",userNameToSaveInFeed);
        post.put("userProfileImage",userProfileUrlToSaveInFeed);
        post.put("type",4);
        post.put("feedItemId", currentFeedItemId);
        if (existingFeedItem != null) {
            post.put("commentCount", existingFeedItem.getCommentCount());
            post.put("likeCount", existingFeedItem.getLikeCount());
            if (existingFeedItem.getLatLng() != null) {
                post.put("latLng", existingFeedItem.getLatLng());
            }
        }

        FirebaseUtil.createCollectionInFirestore(post, currentFeedItemId,FeedCollectionType.POSTS ,new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);
                ActivityHelper.setResult(CreatePostActivity.this,true);
                finish();
            }
        });
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



}