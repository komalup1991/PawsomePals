package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;

public class PhotoVideoFeedViewHolder extends RecyclerView.ViewHolder{
    CircleImageView userProfilePic ;
    TextView usernameTextView ;
    TextView timestampTextView ;
    TextView photoVideoCaptionTextView ;
    ImageView photoVideoImageView ;
    TextView userTaggedTextView ;
    TextView locationTaggedTextView ;
    public PhotoVideoFeedViewHolder(@NonNull View itemView) {
        super(itemView);
         userProfilePic = itemView.findViewById(R.id.userProfilePic);
         usernameTextView = itemView.findViewById(R.id.usernameTextView);
         timestampTextView = itemView.findViewById(R.id.timestampTextView);
         photoVideoCaptionTextView = itemView.findViewById(R.id.photoVideoCaptionTextView);
         photoVideoImageView = itemView.findViewById(R.id.photoVideoImageView);
         userTaggedTextView = itemView.findViewById(R.id.userTaggedTextView);
         locationTaggedTextView = itemView.findViewById(R.id.locationTaggedTextView);

    }
    public void bindData(PhotoVideo photoVideo) {
        Glide.with(userProfilePic.getContext())
                .load(photoVideo.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(photoVideo.getUsername());
        timestampTextView.setText(photoVideo.getCreatedAt());
        userTaggedTextView.setText(photoVideo.getUserTagged());
        locationTaggedTextView.setText(photoVideo.getLocationTagged());
        photoVideoCaptionTextView.setText(photoVideo.getCaption());
        Glide.with(userProfilePic.getContext())
                .load(photoVideo.getImg())
                .into(photoVideoImageView);



    }
}
