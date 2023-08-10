package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import edu.northeastern.pawsomepals.R;

import edu.northeastern.pawsomepals.models.ChatUserCardViewModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.chat.ChatFragment;

public class ChatGroupMemberViewsAdapter extends RecyclerView.Adapter<ChatGroupMemberViewsAdapter.GroupMemberViewsHolder> {
    private Context context;
    private List<ChatUserCardViewModel> usersList;
    private ChatFragment.ProfilePicClickListener onItemActionListener;


    public ChatGroupMemberViewsAdapter(Context context, List<ChatUserCardViewModel> usersList, ChatFragment.ProfilePicClickListener onItemActionListener) {
        this.context = context;
        this.usersList = usersList;
        this.onItemActionListener = onItemActionListener;
    }

    @NonNull
    @Override
    public GroupMemberViewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_group_member_cardview, parent, false);
        return new GroupMemberViewsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberViewsHolder holder, int position) {
        Glide.with(context)
                .load(usersList.get(position).getUserImg())
                .into(holder.profileImg);
        holder.userName.setText(usersList.get(position).getUserName());
        holder.profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int latestPosition = holder.getAdapterPosition();
                if (onItemActionListener != null) {
                    if (latestPosition != RecyclerView.NO_POSITION) {
                        onItemActionListener.onItemClicked(usersList.get(latestPosition).getUserId());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class GroupMemberViewsHolder extends RecyclerView.ViewHolder {
        private ImageView profileImg;
        private TextView userName;

        public GroupMemberViewsHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.item_user_img);
            userName = itemView.findViewById(R.id.item_user_name);
        }
    }

}


