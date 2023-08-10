package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Like;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.LikeViewHolder;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class LikeAdapter extends RecyclerView.Adapter<LikeViewHolder> {
    private final Context context;
    private List<Like> likeList;
    private final String postId;
    private FirebaseUser firebaseUser;

    public LikeAdapter(Context context, List<Like> likeList, String postId) {
        this.context = context;
        this.likeList = likeList;
        this.postId = postId;
    }

    @NonNull
    @Override
    public LikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.like_item, parent, false);
        return new LikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LikeViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Like like = likeList.get(position);
        FirebaseUtil.fetchUserInfoFromFirestore(like.getCreatedBy(), new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {

                Glide.with(context)
                        .load(user.getProfileImage())
                        .into(holder.image_profile);
                holder.username.setText(user.getName());
                holder.createdAtTextView.setText(like.getCreatedAt());
            }
        });
    }

    @Override
    public int getItemCount() {
        return likeList.size();
    }

//    public void setLikes(List<Like> likes) {
//        likeList = likes;
//    }
}
