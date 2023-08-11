package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class PostFeedViewHolder extends RecyclerView.ViewHolder {

    CircleImageView userProfilePic;
    TextView usernameTextView;
    TextView timestampTextView;
    TextView postCaptionTextView;
    TextView postContentTextView;
    TextView userTaggedTextView;
    ImageView userTaggedImageView,locationTaggedImageView,moreOptionImageView;
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
        userTaggedImageView= itemView.findViewById(R.id.userTaggedImageView);
        locationTaggedImageView= itemView.findViewById(R.id.locationTaggedImageView);
        moreOptionImageView = itemView.findViewById(R.id.moreOptionImageView);

    }

    public void bindData(AppCompatActivity activity, Post post, OnItemActionListener onItemActionListener) {
        feedActionsLayout.bindView(activity, post);
        Glide.with(userProfilePic.getContext())
                .load(post.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(post.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(post.getCreatedAt()));
        postCaptionTextView.setText(post.getCaption());
        postContentTextView.setText(post.getPostContent());
        String userTagged = post.getUserTagged();
        String locationTagged = post.getLocationTagged();

        if (userTagged != null && !userTagged.isEmpty() && !(userTagged.trim().equals("null"))) {
            userTaggedTextView.setText(userTagged);
        } else {
            userTaggedImageView.setVisibility(View.GONE);
            userTaggedTextView.setVisibility(View.GONE);
        }

        if (locationTagged != null && !locationTagged.isEmpty() && !(locationTagged.trim().equals("null"))) {
            locationTaggedTextView.setText(locationTagged);
            locationTaggedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onLocationClick(post);
                }
            });
        } else {
            locationTaggedTextView.setVisibility(View.GONE);
            locationTaggedImageView.setVisibility(View.GONE);
        }

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(post.getCreatedBy());
            }
        });

        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(post.getCreatedBy());
            }
        });

        moreOptionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.showMoreOptionsMenu(activity, post, view);
            }
        });
    }
}
