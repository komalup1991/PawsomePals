package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.pawsomepals.R;

public class LikeViewHolder extends RecyclerView.ViewHolder {
    public ImageView image_profile;
    public TextView username;
    public TextView createdAtTextView;
    public Button followFollowingButton;
    public LikeViewHolder(@NonNull View itemView) {
        super(itemView);
        image_profile = itemView.findViewById(R.id.image_profile);
        username = itemView.findViewById(R.id.username);
        createdAtTextView = itemView.findViewById(R.id.createdAtTextView);
        followFollowingButton = itemView.findViewById(R.id.followFollowingButton);
    }
}
