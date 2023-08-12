package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class ServicesFeedViewHolder extends RecyclerView.ViewHolder {
    CircleImageView userProfilePic;
    TextView usernameTextView;
    TextView timestampTextView;
    TextView serviceTypeTextView;
    TextView serviceNameTextView;
    TextView serviceDetailTextView;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
    ImageView userTaggedImageView,locationTaggedImageView,moreOptionImageView;
    FeedActionsLayout feedActionsLayout;

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
        feedActionsLayout = itemView.findViewById(R.id.feed_action);
        userTaggedImageView= itemView.findViewById(R.id.userTaggedImageView);
        locationTaggedImageView= itemView.findViewById(R.id.locationTaggedImageView);
        moreOptionImageView = itemView.findViewById(R.id.moreOptionImageView);
    }

    public void bindData(AppCompatActivity activity, Services services, OnItemActionListener onItemActionListener) {
        feedActionsLayout.bindView(activity, services);
        Glide.with(userProfilePic.getContext())
                .load(services.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(services.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(services.getCreatedAt()));
        String userTagged = services.getUserTagged();
        String locationTagged = services.getLocationTagged();

        if (userTagged != null && !userTagged.trim().isEmpty()) {
            userTaggedImageView.setVisibility(View.VISIBLE);
            userTaggedTextView.setVisibility(View.VISIBLE);
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
                    onItemActionListener.onLocationClick(services);
                }
            });
            locationTaggedTextView.setVisibility(View.VISIBLE);
            locationTaggedImageView.setVisibility(View.VISIBLE);
        } else {
            locationTaggedTextView.setVisibility(View.GONE);
            locationTaggedImageView.setVisibility(View.GONE);
        }

        serviceTypeTextView.setText(services.getServiceType());
        serviceNameTextView.setText(services.getServiceName());
        serviceDetailTextView.setText(services.getServiceNotes());

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(services.getCreatedBy());
            }
        });

        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(services.getCreatedBy());
            }
        });

        moreOptionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.showMoreOptionsMenu(activity, services, view);
            }
        });
    }
    }
