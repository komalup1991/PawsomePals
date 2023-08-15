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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;
import edu.northeastern.pawsomepals.ui.chat.ChatFragment;
import edu.northeastern.pawsomepals.ui.chat.ChatRoomActivity;


public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoomModel, RecentChatRecyclerAdapter.ChatRoomModelViewHolder> {

    private final Context context;
    private final ChatFragment.ProfilePicClickListener listener;
    private final DataUpdateListener dataUpdateListener;
    public interface DataUpdateListener {
        void onDataUpdated();
    }

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context, ChatFragment.ProfilePicClickListener listener,DataUpdateListener dataUpdateListener) {
        super(options);
        this.context = context;
        this.listener = listener;
        this.dataUpdateListener = dataUpdateListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomModelViewHolder holder, int position, @NonNull ChatRoomModel model) {
        List<Users> users = new ArrayList<>();

        if (model.getChatStyle() != null) {
            if (model.getChatStyle().toString().equals(ChatStyle.GROUP.toString())) {

                List<DocumentReference> references = ChatFirebaseUtil.getGroupFromChatRoom(model.getUserIds());
                for (DocumentReference reference : references) {
                    reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Users user = task.getResult().toObject(Users.class);
                                if (user != null)
                                    users.add(user);
                                if (users.size() > 1) {
                                    boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(ChatFirebaseUtil.currentUserId());
                                    holder.userNameText.setText(model.getChatRoomName());
                                    holder.profilePic.setImageResource(R.drawable.group_chat_icon);

                                    if (lastMessageSentByMe)
                                        holder.lastMessageText.setText("You:" + model.getLastMessage());
                                    else
                                        holder.lastMessageText.setText(model.getLastMessage());

                                    if (model.getLastMessageTimestamp() != null) {
                                        holder.lastMessageTime.setText(ChatFirebaseUtil.timestampToString(model.getLastMessageTimestamp()));
                                    }

                                }
//                                if (users.size() == references.size()) {
//                                    StringBuilder builder = new StringBuilder();
//                                    for (Users theUser : users) {
//                                        builder.append(theUser.getName());
//                                        builder.append(" ");
//                                    }
//                                    holder.userNameText.setText(builder.toString());
//                                }
                            }
                        }
                    });
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //navigate to chat activity;
                        Intent intent = new Intent(context, ChatRoomActivity.class);
                        ChatFirebaseUtil.passGroupChatModelAsIntent(intent, users,model.getChatRoomName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            } else {
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
                                    holder.profilePic.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(listener != null){
                                                listener.onItemClicked(otherUser.getUserId());
                                            }

                                        }
                                    });
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
                                            ChatFirebaseUtil.passChatStyleFromIntent(intent, ChatStyle.ONEONONE);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        }
                                    });
                                }
                            }
                        });
            }

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
        }

    }

    @NonNull
    @Override
    public ChatRoomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_search_chat_item, parent, false);
        return new ChatRoomModelViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        startListening();

        if(dataUpdateListener != null) {
            dataUpdateListener.onDataUpdated();
        }
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
