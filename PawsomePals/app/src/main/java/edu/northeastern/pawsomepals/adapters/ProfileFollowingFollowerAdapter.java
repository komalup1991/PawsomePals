package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;

public class ProfileFollowingFollowerAdapter extends RecyclerView.Adapter<ProfileFollowingFollowerAdapter.ProfileFollowingFollowerViewHolder> {
    private List<Users> userProfiles;
    private final Context context;
    private final FirebaseFirestore firebaseFirestore;

    private static final int VIEW_TYPE_FOLLOWER_FOLLOWING_PROFILE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onUserClick(int position);
    }

    public ProfileFollowingFollowerAdapter(List<Users> userProfiles, Context context) {
        this.userProfiles = userProfiles;
        this.context = context;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void setFilteredList(List<Users> filteredList) {
        this.userProfiles = filteredList;
        notifyDataSetChanged();
    }

    public List<Users> getUserProfiles() {
        return userProfiles;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ProfileFollowingFollowerAdapter.ProfileFollowingFollowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_follower_following_item_row, parent, false);
        return new ProfileFollowingFollowerAdapter.ProfileFollowingFollowerViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileFollowingFollowerAdapter.ProfileFollowingFollowerViewHolder holder, int position) {
        Users userProfile = userProfiles.get(position);

        if (userProfiles.isEmpty()) {
            holder.hideViews();
        } else {
            holder.showViews();
            try {
                holder.bind(userProfile);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            setAnimation(holder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        return userProfiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return userProfiles.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_FOLLOWER_FOLLOWING_PROFILE;
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView emptyTextView;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyTextView = itemView.findViewById(R.id.emptyTextView);
        }
    }

    public void setUserProfiles(List<Users> userProfiles) {
        this.userProfiles = userProfiles;

    }

    private void setAnimation(View viewToAnimate, int position) {
        Animation slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        viewToAnimate.startAnimation(slideIn);
    }


    public class ProfileFollowingFollowerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageUser;
        private final TextView textUserName;
        private final LinearLayout layoutUserInfo;

        public ProfileFollowingFollowerViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageUser = itemView.findViewById(R.id.imageUser);
            textUserName = itemView.findViewById(R.id.textUserName);
            layoutUserInfo = itemView.findViewById(R.id.layoutUserInfo);

            layoutUserInfo.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onUserClick(position);
                    }
                }
            });
        }

        public void bind(Users userProfile) throws ParseException {

            textUserName.setText(userProfile.getName());

            String imageUrl = userProfile.getProfileImage();

            if (!Objects.isNull(imageUrl)) {
                if (!imageUrl.equals("") && !imageUrl.equals("null")) {
                    Glide.with(context)
                            .load(imageUrl)
                            .into(imageUser);
                } else {
                    Glide.with(context)
                            .load(R.drawable.dog)
                            .into(imageUser);
                }
            } else {
                Glide.with(context)
                        .load(R.drawable.dog)
                        .into(imageUser);
            }

        }

        private void hideViews() {
            // Hide views when there are no user profiles
            imageUser.setVisibility(View.GONE);
            textUserName.setVisibility(View.GONE);
        }

        private void showViews() {
            // Show views when user profiles are available
            imageUser.setVisibility(View.VISIBLE);
            textUserName.setVisibility(View.VISIBLE);
        }
    }
}

