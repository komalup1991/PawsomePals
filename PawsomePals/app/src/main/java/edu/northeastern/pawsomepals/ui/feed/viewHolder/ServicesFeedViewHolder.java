package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
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
    }

    public void bindData(Activity activity, Services services) {
        feedActionsLayout.bindView(activity, services);
        Glide.with(userProfilePic.getContext())
                .load(services.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(services.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(services.getCreatedAt()));
        userTaggedTextView.setText(services.getUserTagged());
        locationTaggedTextView.setText(services.getLocationTagged());
        serviceTypeTextView.setText(services.getServiceType());
        serviceNameTextView.setText(services.getServiceName());
        serviceDetailTextView.setText(services.getServiceNotes());
    }
}
