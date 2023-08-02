package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.CommentActivity;

public class ServicesFeedViewHolder extends RecyclerView.ViewHolder{
    CircleImageView userProfilePic ;
    TextView usernameTextView ;
    TextView timestampTextView ;
    TextView serviceTypeTextView ;
    TextView serviceNameTextView ;
    TextView serviceDetailTextView ;
    TextView userTaggedTextView;
    TextView locationTaggedTextView;
    ImageButton likeButton, commentButton, shareButton;
    TextView likeCountTextView, commentCountTextView;
    int likeCount = 0;
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
        likeButton = itemView.findViewById(R.id.likeButton);
        likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
        commentButton = itemView.findViewById(R.id.commentButton);
        commentCountTextView = itemView.findViewById(R.id.commentCountTextView);
        shareButton = itemView.findViewById(R.id.shareButton);
    }
    public void bindData(Activity activity, Services services) {
        Glide.with(userProfilePic.getContext())
                .load(services.getUserProfileImage())
                .into(userProfilePic);
        usernameTextView.setText(services.getUsername());
        timestampTextView.setText(services.getCreatedAt());
        if(services.getCommentCount()!=null){
            commentCountTextView.
                    setText(String.valueOf(Math.toIntExact(services.getCommentCount())));}
        userTaggedTextView.setText(services.getUserTagged());
        locationTaggedTextView.setText(services.getLocationTagged());
        serviceTypeTextView.setText(services.getServiceType());
        serviceNameTextView.setText(services.getServiceName());
        serviceDetailTextView.setText(services.getServiceNotes());
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
                intent.putExtra("postId",services.getServiceId());
                intent.putExtra("postType","services");
                intent.putExtra("IdField","serviceId");
                activity.startActivity(intent);
            }
        });

    }
}
