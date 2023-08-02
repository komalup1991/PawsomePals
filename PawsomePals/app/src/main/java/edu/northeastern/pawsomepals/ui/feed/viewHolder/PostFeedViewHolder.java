package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.ui.feed.CommentActivity;

public class PostFeedViewHolder extends RecyclerView.ViewHolder {

    CircleImageView userProfilePic;
    TextView usernameTextView;
    TextView timestampTextView;
    TextView postCaptionTextView;
    TextView postContentTextView;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
    ImageButton likeButton, commentButton, shareButton;
    TextView likeCountTextView, commentCountTextView;
    int likeCount = 0;

    public PostFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        userProfilePic = itemView.findViewById(R.id.userProfilePic);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        postCaptionTextView = itemView.findViewById(R.id.postCaptionTextView);
        postContentTextView = itemView.findViewById(R.id.postContentTextView);
        userTaggedTextView = itemView.findViewById(R.id.userTaggedTextView);
        locationTaggedTextView = itemView.findViewById(R.id.locationTaggedTextView);
        likeButton = itemView.findViewById(R.id.likeButton);
        likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
        commentButton = itemView.findViewById(R.id.commentButton);
        commentCountTextView = itemView.findViewById(R.id.commentCountTextView);
        shareButton = itemView.findViewById(R.id.shareButton);
    }

    public void bindData(Activity activity,Post post) {
        Glide.with(userProfilePic.getContext())
                .load(post.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(post.getUsername());
        timestampTextView.setText(post.getCreatedAt());
        postCaptionTextView.setText(post.getCaption());
        postContentTextView.setText(post.getPostContent());
        userTaggedTextView.setText(post.getUserTagged());
        locationTaggedTextView.setText(post.getLocationTagged());
        if(post.getCommentCount()!=null){
            commentCountTextView.
                    setText(String.valueOf(Math.toIntExact(post.getCommentCount())));}

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeCount++;
                likeCountTextView.setText("(" + likeCount + ")");
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CommentActivity.class);
                intent.putExtra("postId",post.getPostId());
                intent.putExtra("postType","posts");
                intent.putExtra("IdField","postId");
                activity.startActivity(intent);
            }
        });
    }
}
