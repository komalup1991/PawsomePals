package edu.northeastern.pawsomepals.adapters;

import static edu.northeastern.pawsomepals.ui.chat.CreateNewGroupChat.ADD_DATA_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.northeastern.pawsomepals.R;

public class ChatCreateGroupUserImgAdapter extends RecyclerView.Adapter<ChatCreateGroupUserImgAdapter.GroupUserImgHolder> {
    private final Context context;
    private List<String> usersList;
    private ChatCreateGroupUserImgAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ChatCreateGroupUserImgAdapter.OnItemClickListener listener) {
        mListener = listener;
    }


    public ChatCreateGroupUserImgAdapter(Context context, List<String> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public GroupUserImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_create_chat_user_img_item, parent, false);
        return new GroupUserImgHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupUserImgHolder holder, int position) {
        Glide.with(context)
                .load(usersList.get(position))
                .into(holder.profileImg);
        bindHolder(holder);

    }
    private void bindHolder(final GroupUserImgHolder holder){
        holder.dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition();
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
    public void updateData(List<String> data,int position) {
        if(position == ADD_DATA_POSITION ){
            usersList = data;
            notifyDataSetChanged();
        } else if (position >= 0){
        notifyItemRemoved(position);
        usersList = data;
        notifyDataSetChanged();}
    }
    class GroupUserImgHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImg;
        private final ImageButton dismissButton;

        public GroupUserImgHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.item_user_img);
            dismissButton = itemView.findViewById(R.id.dismiss_button);
        }
    }

}