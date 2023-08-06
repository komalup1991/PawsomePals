package edu.northeastern.pawsomepals.ui.feed;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.CommentAdapter;
import edu.northeastern.pawsomepals.models.Comment;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView commentRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addComment;
    private ImageView userImageProfile;
    private TextView postCommentButton;

    private String feedItemId, postType, IdField;
    private String createdBy;

    String firebaseUser;
    private Dialog progressDialog;
    private String userNameToSaveInFeed;
    private String userProfileUrlToSaveInFeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        userImageProfile = findViewById(R.id.image_profile);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        FirebaseUtil.fetchUserInfoFromFirestore(firebaseUser, new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                userNameToSaveInFeed = user.getName();
                userProfileUrlToSaveInFeed = user.getProfileImage();
                Glide.with(CommentActivity.this)
                        .load(user.getProfileImage())
                        .into(userImageProfile);


            }
        });

        feedItemId = getIntent().getStringExtra("feedItemId");
        postType = getIntent().getStringExtra("postType");

        createdBy = intent.getStringExtra("createdBy");

        commentRecyclerView = findViewById(R.id.recycler_view);
        commentRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentRecyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, feedItemId);
        commentRecyclerView.setAdapter(commentAdapter);

        addComment = findViewById(R.id.add_comment);
        userImageProfile = findViewById(R.id.image_profile);
        postCommentButton = findViewById(R.id.post);


        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(addComment.getText().toString())) {
                    Toast.makeText(CommentActivity.this, "No comment added!", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                    addComment.setText("");
                }
            }
        });

        getImage();
        readComments();

    }

    private void readComments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("comments").whereEqualTo("feedItemId", feedItemId).
                addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Comments", "Error getting comments.", error);
                        return;
                    }
                    List<Comment> commentList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Comment comment = document.toObject(Comment.class);
                        comment.setCreatedAt(TimeUtil.formatTime(comment.getCreatedAt()));
                        commentList.add(comment);
                    }
                    commentAdapter.setComments(commentList);
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void getImage() {
    }

    private void addComment() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String comment = addComment.getText().toString();
        String createdBy = firebaseUser;
        String createdAt = String.valueOf(dateFormat.format(System.currentTimeMillis()));

        Map<String, Object> comments = new HashMap<>();
        comments.put("createdBy", createdBy);
        comments.put("comment", comment);
        comments.put("feedItemId", feedItemId);
        comments.put("createdAt", createdAt);
        comments.put("username", userNameToSaveInFeed);
        comments.put("userProfileImage", userProfileUrlToSaveInFeed);

        FirebaseUtil.createCollectionInFirestore(comments, "comments", new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);

            }
        });
        FirebaseUtil.updateFeedWithCommentCount(postType, feedItemId);


    }


}