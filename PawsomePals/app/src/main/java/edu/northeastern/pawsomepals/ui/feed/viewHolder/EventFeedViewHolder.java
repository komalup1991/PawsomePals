package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;

public class EventFeedViewHolder extends RecyclerView.ViewHolder {
    CircleImageView userProfilePic ;
    TextView usernameTextView ;
    TextView timestampTextView ;
    ImageView eventImage ;
    TextView userTaggedTextView ;
    TextView locationTaggedTextView ;
    TextView eventDetailsTextView ;
    TextView eventDateTextView ;
    TextView eventTimeTextView,eventNameTextView ;
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
    }
    public void bindData(Event event) {
        Glide.with(userProfilePic.getContext())
                .load(event.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(event.getUsername());
        timestampTextView.setText(event.getCreatedAt());
        userTaggedTextView.setText(event.getUserTagged());
        locationTaggedTextView.setText(event.getLocationTagged());
        Glide.with(userProfilePic.getContext())
                .load(event.getImg())
                .into(eventImage);
        eventDetailsTextView.setText(event.getEventDetails());
        eventDateTextView.setText(event.getEventDate());
        eventTimeTextView.setText(event.getEventTime());
        eventNameTextView.setText(event.getEventName());
    }
}
