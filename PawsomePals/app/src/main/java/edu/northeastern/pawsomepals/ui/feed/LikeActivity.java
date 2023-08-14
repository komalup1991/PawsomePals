package edu.northeastern.pawsomepals.ui.feed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.CommentAdapter;
import edu.northeastern.pawsomepals.adapters.LikeAdapter;
import edu.northeastern.pawsomepals.models.Comment;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Like;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.ui.profile.ProfileFeedFragment;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
import edu.northeastern.pawsomepals.utils.FeedFilter;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class LikeActivity extends AppCompatActivity implements OnItemActionListener {
    private ImageView userImageProfile;
    private RecyclerView likesRecyclerView;
    private LikeAdapter likeAdapter;
    private FirebaseUser firebaseUser;
    private String feedItemId;
    private String postType;
    private Object createdBy;
    private List<Like> likeList;
    private FirebaseFirestore firebaseFirestore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        Toolbar toolbar = findViewById(R.id.toolbar);
        userImageProfile = findViewById(R.id.image_profile);

        Intent intent = getIntent();
        feedItemId = intent.getStringExtra("feedItemId");
        postType = intent.getStringExtra("postType");

        createdBy = intent.getStringExtra("createdBy");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Likes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        likeUserList();
        likesRecyclerView=findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        likesRecyclerView.setLayoutManager(linearLayoutManager);
        likeList = new ArrayList<>();
        likeAdapter = new LikeAdapter(this,likeList,feedItemId,this);
        likesRecyclerView.setAdapter(likeAdapter);
    }

    private void likeUserList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(postType)
                .whereEqualTo("feedItemId", feedItemId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String documentId = documentSnapshot.getId();

                            db.collection(postType)
                                    .document(documentId)
                                    .collection("likes")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            likeList.clear();
                                            for (QueryDocumentSnapshot document : value) {
                                                Like like = document.toObject(Like.class);
                                                like.setCreatedAt(TimeUtil.formatTime(like.getCreatedAt()));
                                                likeList.add(like);
                                            }
                                            likeAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("likes", "Error querying collection: " + e.getMessage());
                    }
                });
    }



                    @Override
    public void onRecipeClick(Recipe recipe) {

    }

    @Override
    public void onUserClick(String userId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profileId", userId);
        setResult(RESULT_OK, resultIntent);

        SharedPreferences sharedPreferences = this.getSharedPreferences("ProfileId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileId", userId);
        editor.apply();

        finish();
    }

    @Override
    public void onLocationClick(FeedItem feedItem) {

    }

    @Override
    public void onFeedFilterSpinnerClick(int feedfilter) {

    }
}