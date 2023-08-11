package edu.northeastern.pawsomepals.ui.feed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.layout.TaggingOptionsLayout;
import edu.northeastern.pawsomepals.utils.ActivityHelper;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class CreateServicesActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CircleImageView userProfilePic;
    private TextView userNameTextView;
    private EditText serviceNameEditTextView,notesOnServiceEditTextView;
    private Spinner serviceTypeSpinnerOptions;
    private Dialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String loggedInUserId;
    private String serviceDocId;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;
    private TaggingOptionsLayout taggingOptionsLayout;
    private LatLng currentLatLng;
    private String locationTagged;
    private String usersTagged;
    private Services existingFeedItem;

    private String currentFeedItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_services);
        toolbar = findViewById(R.id.toolbar);
        userProfilePic = findViewById(R.id.userProfilePic);
        userNameTextView = findViewById(R.id.userNameTextView);
        serviceTypeSpinnerOptions = findViewById(R.id.serviceTypeSpinnerOptions);
        serviceNameEditTextView = findViewById(R.id.serviceNameEditTextView);
        notesOnServiceEditTextView = findViewById(R.id.notesOnServiceEditTextView);
        taggingOptionsLayout = findViewById(R.id.tag_location_layout);
        taggingOptionsLayout.bindView(this, new TaggingOptionsLayout.OnTaggedDataFetchListener() {
            @Override
            public void onLocation(LatLng latLng, String locationTagged) {
                currentLatLng = latLng;
                CreateServicesActivity.this.locationTagged = locationTagged;
            }

            @Override
            public void onTaggedUsersGet(String usersTagged) {
                CreateServicesActivity.this.usersTagged = usersTagged;
            }
        });
        existingFeedItem = (Services) getIntent().getSerializableExtra("existingFeedItem");
        if (existingFeedItem != null) {
            serviceNameEditTextView.setText(existingFeedItem.getServiceName());
            notesOnServiceEditTextView.setText(existingFeedItem.getServiceNotes());
          //  serviceTypeSpinnerOptions.setSelection(existingFeedItem.getServiceType());
            if (existingFeedItem.getUserTagged() != null) {
                taggingOptionsLayout.setTagPeopleTextView(existingFeedItem.getUserTagged());
            }

            if (existingFeedItem.getLocationTagged() != null) {
                taggingOptionsLayout.setTagLocationTextView(existingFeedItem.getLocationTagged());
            }

            usersTagged = existingFeedItem.getUserTagged();
            locationTagged = existingFeedItem.getLocationTagged();
            currentFeedItemId = existingFeedItem.getFeedItemId();
            if (existingFeedItem.getLatLng() != null) {
                currentLatLng = new LatLng(existingFeedItem.getLatLng().getLatitude(),
                        existingFeedItem.getLatLng().getLongitude());
            }
        } else {
            currentFeedItemId = UUID.randomUUID().toString();
        }

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
            actionBar.setTitle("Add Service");
        }
        FirebaseUtil.fetchUserInfoFromFirestore(loggedInUserId, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed=user.getName();
                userProfileUrlToSaveInFeed=user.getProfileImage();

                Glide.with(CreateServicesActivity.this)
                        .load(user.getProfileImage())
                        .into(userProfilePic);
                userNameTextView.setText(user.getName());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceNameEditTextView.getText().toString().isEmpty()) {
                    serviceNameEditTextView.setError("This field is required");
                    Toast.makeText(CreateServicesActivity.this, "Service Name is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (notesOnServiceEditTextView.getText().toString().isEmpty()) {
                    notesOnServiceEditTextView.setError("This field is required");
                    Toast.makeText(CreateServicesActivity.this, "Service Detail is mandatory.", Toast.LENGTH_SHORT).show();
                    return;
                }

                createFeedMap();
                DialogHelper.showProgressDialog("Your post is being saved...",progressDialog,CreateServicesActivity.this);
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

        String serviceType = serviceTypeSpinnerOptions.getSelectedItem().toString();
        String serviceName = serviceNameEditTextView.getText().toString();
        String serviceNotes = notesOnServiceEditTextView.getText().toString();
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> services = new HashMap<>();
        services.put("createdBy", loggedInUserId);
        services.put("serviceType", serviceType);
        services.put("serviceName", serviceName);
        services.put("serviceNotes", serviceNotes);
        services.put("userTagged", usersTagged);
        services.put("locationTagged", locationTagged);
        services.put("latLng", currentLatLng);
        services.put("createdAt", createdAt);
        services.put("username",userNameToSaveInFeed);
        services.put("userProfileImage",userProfileUrlToSaveInFeed);
        services.put("type",2);
        services.put("feedItemId", currentFeedItemId);
        if (existingFeedItem != null) {
            services.put("commentCount", existingFeedItem.getCommentCount());
            services.put("likeCount", existingFeedItem.getLikeCount());
        }

        FirebaseUtil.createCollectionInFirestore(services,currentFeedItemId,FeedCollectionType.SERVICES ,new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);
                ActivityHelper.setResult(CreateServicesActivity.this,true);
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