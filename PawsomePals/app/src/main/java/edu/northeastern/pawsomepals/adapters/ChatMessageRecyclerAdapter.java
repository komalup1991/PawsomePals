package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;

public class ChatMessageRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatMessageRecyclerAdapter.ChatModelViewHolder> {

    private Context context;
    private List<String> otherUserName;

    public ChatMessageRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context,List<String> otherUserName) {
        super(options);
        this.context = context;
        if (otherUserName != null)
            this.otherUserName = otherUserName;
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_item,parent,false);
        return new ChatModelViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
       if (model.getSenderId().equals(ChatFirebaseUtil.currentUserId())){
           holder.otherUserNameTextView.setVisibility(View.GONE);
           holder.leftChatLayout.setVisibility(View.GONE);
           holder.rightChatLayout.setVisibility(View.VISIBLE);
           holder.rightChatTextView.setText(model.getMessage());
       } else {
           holder.rightChatLayout.setVisibility(View.GONE);
           holder.leftChatLayout.setVisibility(View.VISIBLE);
           holder.leftChatTextView.setText(model.getMessage());
           holder.otherUserNameTextView.setText(this.otherUserName.toString());
       }
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftChatLayout, rightChatLayout;
        TextView otherUserNameTextView,leftChatTextView, rightChatTextView;


        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserNameTextView = itemView.findViewById(R.id.other_user_name);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextView = itemView.findViewById(R.id.right_chat_textview);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
        }
    }
}
