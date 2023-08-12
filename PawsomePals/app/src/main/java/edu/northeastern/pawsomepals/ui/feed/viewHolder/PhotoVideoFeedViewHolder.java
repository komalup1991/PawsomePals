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
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class PhotoVideoFeedViewHolder extends RecyclerView.ViewHolder {
    CircleImageView userProfilePic;
    TextView usernameTextView;
    TextView timestampTextView;
    TextView photoVideoCaptionTextView;
    ImageView photoVideoImageView;
    TextView userTaggedTextView;
    ImageView userTaggedImageView,locationTaggedImageView,moreOptionImageView;
    TextView locationTaggedTextView;
    FeedActionsLayout feedActionsLayout;

    public PhotoVideoFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        userProfilePic = itemView.findViewById(R.id.userProfilePic);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        photoVideoCaptionTextView = itemView.findViewById(R.id.photoVideoCaptionTextView);
        photoVideoImageView = itemView.findViewById(R.id.eventImageView);
        userTaggedTextView = itemView.findViewById(R.id.userTaggedTextView);
        userTaggedImageView= itemView.findViewById(R.id.userTaggedImageView);
        locationTaggedTextView = itemView.findViewById(R.id.locationTaggedTextView);
        locationTaggedImageView = itemView.findViewById(R.id.locationTaggedImageView);
        feedActionsLayout = itemView.findViewById(R.id.feed_action);
        moreOptionImageView = itemView.findViewById(R.id.moreOptionImageView);

    }

    public void bindData(AppCompatActivity activity, PhotoVideo photoVideo, OnItemActionListener onItemActionListener) {
        feedActionsLayout.bindView(activity, photoVideo);
        Glide.with(userProfilePic.getContext())
                .load(photoVideo.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(photoVideo.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(photoVideo.getCreatedAt()));
        String userTagged = photoVideo.getUserTagged();
        String locationTagged = photoVideo.getLocationTagged();
        if (userTagged != null && !userTagged.isEmpty() && !(userTagged.trim().equals("null"))) {
            userTaggedImageView.setVisibility(View.VISIBLE);
            userTaggedTextView.setVisibility(View.VISIBLE);
            userTaggedTextView.setText(userTagged);
        } else {
            userTaggedImageView.setVisibility(View.GONE);
            userTaggedTextView.setVisibility(View.GONE);
        }

        if (locationTagged != null && !locationTagged.isEmpty() && !(locationTagged.trim().equals("null"))) {
            locationTaggedTextView.setVisibility(View.VISIBLE);
            locationTaggedTextView.setVisibility(View.VISIBLE);
            locationTaggedTextView.setText(locationTagged);
            locationTaggedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemActionListener.onLocationClick(photoVideo);
                }
            });
        } else {
            locationTaggedImageView.setVisibility(View.GONE);
            locationTaggedTextView.setVisibility(View.GONE);
        }

        photoVideoCaptionTextView.setText(photoVideo.getCaption());

        Glide.with(photoVideoImageView.getContext())
                .load(photoVideo.getImg())
                .into(photoVideoImageView);

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(photoVideo.getCreatedBy());
            }
        });

        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(photoVideo.getCreatedBy());
            }
        });
        moreOptionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.showMoreOptionsMenu(activity, photoVideo, view);
            }
        });
    }
}
