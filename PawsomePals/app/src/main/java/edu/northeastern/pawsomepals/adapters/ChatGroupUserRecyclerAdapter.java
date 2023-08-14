package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;
import edu.northeastern.pawsomepals.ui.chat.ChatRoomActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatGroupUserRecyclerAdapter extends FirestoreRecyclerAdapter<Users, ChatGroupUserRecyclerAdapter.UserModelViewHolder> {

    private Context context;
    private OnItemClickListener mListener;
    private List<String> checkedUserList;

    public void updateData(List<String> checkedUserList) {
        this.checkedUserList = checkedUserList;
//        startListening();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public ChatGroupUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Users> options, Context context, List<String> checkedUserList) {
        super(options);
        this.context = context;
        this.checkedUserList = checkedUserList;
    }


    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull Users model) {
        if(holder.addCheckBox.isChecked()){
            holder.addCheckBox.setChecked(true);
        }else{
            holder.addCheckBox.setChecked(false);
        }

        if (!checkedUserList.isEmpty()) {
            if (checkedUserList.contains(model.getUserId())){
                holder.addCheckBox.setChecked(true);
            }else{
                holder.addCheckBox.setChecked(false);
            }
        }else{
            holder.addCheckBox.setChecked(false);
        }
        holder.userNameText.setText(model.getName());
        //set up image
        Glide.with(this.context)
                .load(model.getProfileImage())
                .into(holder.profilePic);
        //Check if the result is "me"
        if (model.getUserId().equals(ChatFirebaseUtil.currentUserId())) {
            holder.userNameText.setText(model.getName() + "(Me)");
        }
        bindHolder(holder, model);
    }

    private void bindHolder(final UserModelViewHolder holder, final Users model) {
        holder.addCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                final int position = holder.getAdapterPosition();

                Log.d("butt", "click " + position);

                if (mListener != null) {
                    mListener.onItemClick(position, isChecked);
                }
                startListening();
            }
        });

    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_search_user_item, parent, false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameText;
        private ImageView profilePic;
        private CheckBox addCheckBox;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.chat_name_text);
            profilePic = itemView.findViewById(R.id.chat_profile_pic);
            addCheckBox = itemView.findViewById(R.id.addUserCheckBox);
        }
    }
}
