package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;
import edu.northeastern.pawsomepals.ui.chat.ChatRoomActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoomModel, RecentChatRecyclerAdapter.ChatRoomModelViewHolder> {

    private Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomModelViewHolder holder, int position, @NonNull ChatRoomModel model) {
//        if (model.getChatStyle()!= null){
//            if (model.getChatStyle().equals(ChatStyle.ONEONONE.toString())) {
                ChatFirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(ChatFirebaseUtil.currentUserId());
                                Users otherUser = task.getResult().toObject(Users.class);
                                if (otherUser != null) {
                                    holder.userNameText.setText(otherUser.getName());
                                    Glide.with(this.context)
                                            .load(otherUser.getProfileImage())
                                            .into(holder.profilePic);
                                }
                                if (lastMessageSentByMe)
                                    holder.lastMessageText.setText("You:" + model.getLastMessage());
                                else
                                    holder.lastMessageText.setText(model.getLastMessage());
                                if (model.getLastMessageTimestamp() != null) {
                                    holder.lastMessageTime.setText(ChatFirebaseUtil.timestampToString(model.getLastMessageTimestamp()));
                                }
                                if (otherUser != null) {
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //navigate to chat activity;
                                            Intent intent = new Intent(context, ChatRoomActivity.class);
                                            ChatFirebaseUtil.passUserModelAsIntent(intent, otherUser);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        }
                                    });
                                }
                            }
                        });
                List<Users> users = new ArrayList<>();

//            } else  {
//                boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(ChatFirebaseUtil.currentUserId());
//                holder.userNameText.setText("Group Chat");
////            Glide.with(this.context)
////                    .load(otherUser.getProfileImage())
////                    .into(holder.profilePic);
//
//                if (lastMessageSentByMe)
//                    holder.lastMessageText.setText("You:" + model.getLastMessage());
//                else
//                    holder.lastMessageText.setText(model.getLastMessage());
//                if (model.getLastMessageTimestamp() != null) {
//                    holder.lastMessageTime.setText(ChatFirebaseUtil.timestampToString(model.getLastMessageTimestamp()));
//                }
//        }


//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //navigate to chat activity;
//                    Intent intent = new Intent(context, ChatRoomActivity.class);
//                    ChatFirebaseUtil.passGroupChatModelAsIntent(intent,);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(intent);
//                }
//            });

//        }

    }


    @NonNull
    @Override
    public ChatRoomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_search_chat_item, parent, false);
        return new ChatRoomModelViewHolder(view);
    }

    class ChatRoomModelViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText, lastMessageText, lastMessageTime;
        ImageView profilePic;

        public ChatRoomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.chat_name_text);
            lastMessageText = itemView.findViewById(R.id.chat_message_text);
            lastMessageTime = itemView.findViewById(R.id.chat_last_message_time);
            profilePic = itemView.findViewById(R.id.chat_profile_pic);
        }
    }
}
