package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.Services;

public class ServicesFeedViewHolder extends RecyclerView.ViewHolder{
    CircleImageView userProfilePic ;
    TextView usernameTextView ;
    TextView timestampTextView ;
    TextView serviceTypeTextView ;
    TextView serviceNameTextView ;
    TextView serviceDetailTextView ;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
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
    }
    public void bindData(Services services) {
        Glide.with(userProfilePic.getContext())
                .load(services.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(services.getUsername());
        timestampTextView.setText(services.getCreatedAt());
        userTaggedTextView.setText(services.getUserTagged());
        locationTaggedTextView.setText(services.getLocationTagged());
        serviceTypeTextView.setText(services.getServiceType());
        serviceNameTextView.setText(services.getServiceName());
        serviceDetailTextView.setText(services.getServiceNotes());
    }
}
