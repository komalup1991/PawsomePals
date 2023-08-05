package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class PostFeedViewHolder extends RecyclerView.ViewHolder {

    CircleImageView userProfilePic;
    TextView usernameTextView;
    TextView timestampTextView;
    TextView postCaptionTextView;
    TextView postContentTextView;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
    FeedActionsLayout feedActionsLayout;

    public PostFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        userProfilePic = itemView.findViewById(R.id.userProfilePic);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        postCaptionTextView = itemView.findViewById(R.id.postCaptionTextView);
        postContentTextView = itemView.findViewById(R.id.postContentTextView);
        userTaggedTextView = itemView.findViewById(R.id.userTaggedTextView);
        locationTaggedTextView = itemView.findViewById(R.id.locationTaggedTextView);
        feedActionsLayout = itemView.findViewById(R.id.feed_action);
    }

    public void bindData(Activity activity, Post post) {
        feedActionsLayout.bindView(activity, post);
        Glide.with(userProfilePic.getContext())
                .load(post.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(post.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(post.getCreatedAt()));
        postCaptionTextView.setText(post.getCaption());
        postContentTextView.setText(post.getPostContent());
        userTaggedTextView.setText(post.getUserTagged());
        locationTaggedTextView.setText(post.getLocationTagged());
    }
}
