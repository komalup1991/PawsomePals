package edu.northeastern.pawsomepals.ui.feed.viewHolder;


import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.FeedAdapter;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class EventFeedViewHolder extends RecyclerView.ViewHolder {
    CircleImageView userProfilePic;
    TextView usernameTextView;
    TextView timestampTextView;
    ImageView eventImage;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
    TextView eventDetailsTextView;
    TextView eventDateTextView;
    TextView eventTimeTextView, eventNameTextView;
    ImageButton favImageButton;
    FeedActionsLayout feedActionsLayout;

    public EventFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        userProfilePic = itemView.findViewById(R.id.userProfilePic);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        eventImage = itemView.findViewById(R.id.eventImage);
        userTaggedTextView = itemView.findViewById(R.id.userTaggedTextView);
        locationTaggedTextView = itemView.findViewById(R.id.locationTaggedTextView);
        eventDetailsTextView = itemView.findViewById(R.id.eventDetailsTextView);
        eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
        eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
        eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        favImageButton = itemView.findViewById(R.id.favImageButton);
        feedActionsLayout = itemView.findViewById(R.id.feed_action);
    }

    public void bindData(Activity activity, Event event, FeedAdapter.LocationClickListener onLocationClickListener) {
        feedActionsLayout.bindView(activity, event);
        Glide.with(userProfilePic.getContext())
                .load(event.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(event.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(event.getCreatedAt()));

        String userTagged = event.getUserTagged();
        String locationTagged = event.getLocationTagged();
        String eventImg = event.getImg();
        String eventDetails = event.getEventDetails();
        String eventDate = event.getEventDate();
        String eventTime = event.getEventTime();
        String eventName = event.getEventName();
        eventNameTextView.setText(eventName);

        if (userTagged != null && !userTagged.isEmpty() && !(userTagged.trim().equals("null"))) {
            userTaggedTextView.setText(userTagged);
        } else {
            userTaggedTextView.setText("No User Tagged!!! ☹️");
        }

        if (locationTagged != null && !locationTagged.isEmpty() && !(locationTagged.trim().equals("null"))) {
            locationTaggedTextView.setText(locationTagged);
            locationTaggedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLocationClickListener.onClick(event);
                }
            });
        } else {
            locationTaggedTextView.setText("No Location Tagged!!! ☹️");
        }
        if (eventDetails != null && !eventDetails.isEmpty() && !(eventDetails.trim().equals("null"))) {
            eventDetailsTextView.setText(eventDetails);
        } else {
            eventDetailsTextView.setText("No event details yet!");
        }
        if (eventDate.equals("Set Date")) {
            eventDateTextView.setText("TBD");
        } else {
            eventDateTextView.setText(eventDate);

        }
        if (eventTime.equals("Set Time")) {
            eventTimeTextView.setText("TBD");

        } else {
            eventTimeTextView.setText(eventTime);
        }

        if (eventImg != null && !eventImg.isEmpty() && !(eventImg.trim().equals("null"))) {
            Glide.with(userProfilePic.getContext())
                    .load(eventImg)
                    .transform(new FitCenter(), new GranularRoundedCorners(0, 0, 25, 25))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(eventImage);
        } else {
            eventImage.setVisibility(View.GONE);
        }
    }
}
