package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.ui.feed.CommentActivity;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class PhotoVideoFeedViewHolder extends RecyclerView.ViewHolder{
    CircleImageView userProfilePic ;
    TextView usernameTextView ;
    TextView timestampTextView ;
    TextView photoVideoCaptionTextView ;
    ImageView photoVideoImageView ;
    TextView userTaggedTextView ;
    TextView locationTaggedTextView ;
    ImageButton likeButton, commentButton, shareButton,favImageButton;
    TextView likeCountTextView, commentCountTextView;
    int likeCount = 0;
    private boolean isLiked, isFav;
    public PhotoVideoFeedViewHolder(@NonNull View itemView) {
        super(itemView);
         userProfilePic = itemView.findViewById(R.id.userProfilePic);
         usernameTextView = itemView.findViewById(R.id.usernameTextView);
         timestampTextView = itemView.findViewById(R.id.timestampTextView);
         photoVideoCaptionTextView = itemView.findViewById(R.id.photoVideoCaptionTextView);
         photoVideoImageView = itemView.findViewById(R.id.photoVideoImageView);
         userTaggedTextView = itemView.findViewById(R.id.userTaggedTextView);
         locationTaggedTextView = itemView.findViewById(R.id.locationTaggedTextView);
        likeButton = itemView.findViewById(R.id.likeButton);
        likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
        commentButton = itemView.findViewById(R.id.commentButton);
        commentCountTextView = itemView.findViewById(R.id.commentCountTextView);
        shareButton = itemView.findViewById(R.id.shareButton);
        favImageButton = itemView.findViewById(R.id.favImageButton);
        isLiked = false;
        isFav = false;

    }
    public void bindData(Activity activity, PhotoVideo photoVideo) {
        Glide.with(userProfilePic.getContext())
                .load(photoVideo.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(photoVideo.getUsername());
        timestampTextView.setText(photoVideo.getCreatedAt());
        userTaggedTextView.setText(photoVideo.getUserTagged());
        locationTaggedTextView.setText(photoVideo.getLocationTagged());
        photoVideoCaptionTextView.setText(photoVideo.getCaption());
        if(photoVideo.getCommentCount()!=null){
            commentCountTextView.
                    setText(String.valueOf(Math.toIntExact(photoVideo.getCommentCount())));}


        Glide.with(photoVideoImageView.getContext())
                .load(photoVideo.getImg())
                .into(photoVideoImageView);

        if (photoVideo.isFavorite()) {
            favImageButton.setImageResource(R.drawable.pawprintfull);
        }
        if (photoVideo.isLiked()) {
            likeButton.setImageResource(R.drawable.like);
        }

        favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFav) {
                    isFav = false;
                    favImageButton.setImageResource(R.drawable.pawprintempty);
                    FirebaseUtil.removeFavFromFirestore(photoVideo.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "photovideo");
                } else {
                    isFav = true;
                    favImageButton.setImageResource(R.drawable.pawprintfull);
                    FirebaseUtil.addFavToFirestore(photoVideo.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "photovideo");
                }
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLiked) {
                    // Unlike the post
                    isLiked = false;
                    likeCount--;
                    likeButton.setImageResource(R.drawable.likenew);
                    FirebaseUtil.removeLikeFromFirestore(photoVideo.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "photovideo");
                } else {
                    // Like the post
                    isLiked = true;
                    likeCount++;
                    likeButton.setImageResource(R.drawable.like);
                    FirebaseUtil.addLikeToFirestore(photoVideo.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "photovideo");
                }
                likeCountTextView.setText("(" + likeCount + ")");
            }
        });


        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CommentActivity.class);
                intent.putExtra("feedItemId", photoVideo.getFeedItemId());
                intent.putExtra("postType","photovideo");
                activity.startActivity(intent);
            }
        });




    }
}
