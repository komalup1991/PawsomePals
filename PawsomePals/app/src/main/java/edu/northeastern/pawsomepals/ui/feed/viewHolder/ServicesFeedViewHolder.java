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
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.CommentActivity;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class ServicesFeedViewHolder extends RecyclerView.ViewHolder{
    CircleImageView userProfilePic ;
    TextView usernameTextView ;
    TextView timestampTextView ;
    TextView serviceTypeTextView ;
    TextView serviceNameTextView ;
    TextView serviceDetailTextView ;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
    ImageButton likeButton, commentButton, shareButton,favImageButton;
    TextView likeCountTextView, commentCountTextView;
    int likeCount = 0;
    private boolean isLiked, isFav;
    public ServicesFeedViewHolder(@NonNull View itemView) {
        super(itemView);
         userProfilePic = itemView.findViewById(R.id.userProfilePic);
         usernameTextView = itemView.findViewById(R.id.usernameTextView);
         timestampTextView = itemView.findViewById(R.id.timestampTextView);
         serviceTypeTextView = itemView.findViewById(R.id.serviceTypeTextView);
         serviceNameTextView = itemView.findViewById(R.id.serviceNameTextView);
         serviceDetailTextView = itemView.findViewById(R.id.serviceDetailTextView);
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
    public void bindData(Activity activity, Services services) {
        Glide.with(userProfilePic.getContext())
                .load(services.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(services.getUsername());
        timestampTextView.setText(services.getCreatedAt());
        if(services.getCommentCount()!=null){
            commentCountTextView.
                    setText(String.valueOf(Math.toIntExact(services.getCommentCount())));}
        userTaggedTextView.setText(services.getUserTagged());
        locationTaggedTextView.setText(services.getLocationTagged());
        serviceTypeTextView.setText(services.getServiceType());
        serviceNameTextView.setText(services.getServiceName());
        serviceDetailTextView.setText(services.getServiceNotes());
        if (services.isFavorite()) {
            favImageButton.setImageResource(R.drawable.pawprintfull);
        }
        if (services.isLiked()) {
            likeButton.setImageResource(R.drawable.like);
        }

        favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFav) {
                    isFav = false;
                    favImageButton.setImageResource(R.drawable.pawprintempty);
                    FirebaseUtil.removeFavFromFirestore(services.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "services");
                } else {
                    isFav = true;
                    favImageButton.setImageResource(R.drawable.pawprintfull);
                    FirebaseUtil.addFavToFirestore(services.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "services");
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
                    FirebaseUtil.removeLikeFromFirestore(services.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "services");
                } else {
                    // Like the post
                    isLiked = true;
                    likeCount++;
                    likeButton.setImageResource(R.drawable.like);
                    FirebaseUtil.addLikeToFirestore(services.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "services");
                }
                likeCountTextView.setText("(" + likeCount + ")");
            }
        });


        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CommentActivity.class);
                intent.putExtra("feedItemId",services.getFeedItemId());
                intent.putExtra("postType","services");
                activity.startActivity(intent);
            }
        });

    }
}
