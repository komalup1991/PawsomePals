package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ChatUserRecyclerAdapter extends FirestoreRecyclerAdapter<Users,ChatUserRecyclerAdapter.UserModelViewHolder> {

    private Context context;

    public ChatUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Users> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull Users model) {
        holder.userNameText.setText(model.getName());
        //set up image
        Glide.with(this.context)
                .load(model.getUserImg())
                .into(holder.profilePic);
        //Check if the result is "me"
        if (model.getUserId().equals(ChatFirebaseUtil.currentUserId())){
            holder.userNameText.setText(model.getName()+"(Me)");
        }
        //Listener to open new ChatRoomActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to chat activity;
                Intent intent = new Intent(context, ChatRoomActivity.class);
                ChatFirebaseUtil.passUserModelAsIntent(intent,model);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_search_user_item,parent,false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView userNameText;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.chat_user_name_text);
            profilePic = itemView.findViewById(R.id.chat_profile_pic);
        }
    }
}
