package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatUserCardViewModel;
import edu.northeastern.pawsomepals.ui.chat.ChatFragment;

public class ChatGroupMemberViewsAdapter extends RecyclerView.Adapter<ChatGroupMemberViewsAdapter.GroupMemberViewsHolder> {
    private final Context context;
    private final List<ChatUserCardViewModel> usersList;
    private final ChatFragment.ProfilePicClickListener onItemActionListener;


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
        private final ImageView profileImg;
        private final TextView userName;

        public GroupMemberViewsHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.item_user_img);
            userName = itemView.findViewById(R.id.item_user_name);
        }
    }

}


