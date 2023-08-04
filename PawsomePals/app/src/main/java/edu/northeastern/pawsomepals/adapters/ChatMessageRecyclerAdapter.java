package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;


public class ChatMessageRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEXT_MESSAGE = 0;
    private static final int VIEW_TYPE_IMAGE_MESSAGE = 1;


    private Context context;
    private FirestoreRecyclerOptions<ChatMessageModel> options;

    public ChatMessageRecyclerAdapter(@NonNull FirestoreRecyclerOptions<RecyclerView.ViewHolder> options, Context context) {
        super(options);
        this.context = context;
    }
    @Override
    public int getItemViewType(int position) {
        ChatMessageModel message = options.getSnapshots().get(position);

        if (message.isPicture()) {
            return VIEW_TYPE_IMAGE_MESSAGE;
        } else {
            return VIEW_TYPE_TEXT_MESSAGE;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_TEXT_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_item, parent, false);
            return new ChatModelViewHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_image_item, parent, false);
            return new ImageViewHolder(view);
        }
        // return null or throw an exception
        return null;
//        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_item,parent,false);
//        return new ChatModelViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessageModel model) {
        ChatMessageModel message = options.getSnapshots().get(position);

        if (holder.getItemViewType() == VIEW_TYPE_TEXT_MESSAGE) {
            ChatModelViewHolder textViewHolder = (ChatModelViewHolder) holder;
            // Set data to text view holder
            if (model.getSenderId().equals(ChatFirebaseUtil.currentUserId())){
                textViewHolder.otherUserNameTextView.setVisibility(View.GONE);
                textViewHolder.leftChatLayout.setVisibility(View.GONE);
                textViewHolder.rightChatLayout.setVisibility(View.VISIBLE);
                textViewHolder.rightChatTextView.setText(model.getMessage());
            } else {
                textViewHolder.rightChatLayout.setVisibility(View.GONE);
                textViewHolder.leftChatLayout.setVisibility(View.VISIBLE);
                textViewHolder.leftChatTextView.setText(model.getMessage());
                textViewHolder.otherUserNameTextView.setText(model.getSenderName());
            }
        } else if (holder.getItemViewType() == VIEW_TYPE_IMAGE_MESSAGE) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            // Set data to image view holder
            if (model.getSenderId().equals(ChatFirebaseUtil.currentUserId())){
                imageViewHolder.otherUserNameTextView.setVisibility(View.GONE);
                imageViewHolder.leftCardView.setVisibility(View.GONE);
                imageViewHolder.rightCardView.setVisibility(View.VISIBLE);
                imageViewHolder.rightImageView.setImageBitmap(message.getImage());
            } else {
                imageViewHolder.rightChatLayout.setVisibility(View.GONE);
                imageViewHolder.leftChatLayout.setVisibility(View.VISIBLE);
                imageViewHolder.leftChatTextView.setText(model.getMessage());
                imageViewHolder.otherUserNameTextView.setText(model.getSenderName());
            }
            imageViewHolder.imageView.setImageBitmap(message.getImage());
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
    class ImageViewHolder extends RecyclerView.ViewHolder{
        CardView leftCardView, rightCardView;
        ImageView leftImageView, rightImageView;
        TextView otherUserNameTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserNameTextView = itemView.findViewById(R.id.other_user_name);
            leftImageView = itemView.findViewById(R.id.img_friend);
            rightImageView = itemView.findViewById(R.id.img_own);
            leftCardView = itemView.findViewById(R.id.card_view_friend);
            rightCardView = itemView.findViewById(R.id.card_view_own);
        }
    }

}
