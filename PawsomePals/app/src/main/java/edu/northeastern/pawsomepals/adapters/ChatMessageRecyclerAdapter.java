package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;
import edu.northeastern.pawsomepals.ui.chat.ChatImgUtil;


public class ChatMessageRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEXT_MESSAGE = 0;
    private static final int VIEW_TYPE_IMAGE_MESSAGE = 1;
    private static final int VIEW_TYPE_LOCATION_MESSAGE = 2;


    private Context context;
    private FirestoreRecyclerOptions<ChatMessageModel> options;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ChatMessageRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    public int getItemViewType(int position) {
        ChatMessageModel message = getItem(position);

        if (message.isPicture()) {
            return VIEW_TYPE_IMAGE_MESSAGE;
        } else if (message.isPlace()) {
            return VIEW_TYPE_LOCATION_MESSAGE;
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
        } else if (viewType == VIEW_TYPE_LOCATION_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_location_item, parent, false);
            return new LocationViewHolder(view);
        }
        // return null or throw an exception
        return null;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessageModel model) {
        if (holder.getItemViewType() == VIEW_TYPE_TEXT_MESSAGE) {
            ChatModelViewHolder textViewHolder = (ChatModelViewHolder) holder;
            // Set data to text view holder
            if (model.getSenderId().equals(ChatFirebaseUtil.currentUserId())) {
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
            if (model.getSenderId().equals(ChatFirebaseUtil.currentUserId())) {
                imageViewHolder.otherUserNameTextView.setVisibility(View.GONE);
                imageViewHolder.leftCardView.setVisibility(View.GONE);
                imageViewHolder.rightCardView.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getImage()).into(imageViewHolder.rightImageView);
//                imageViewHolder.rightImageView.setImageBitmap(ChatImgUtil.getBitmapFromURL(message.getImage()));
            } else {
                imageViewHolder.rightCardView.setVisibility(View.GONE);
                imageViewHolder.leftCardView.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getImage()).into(imageViewHolder.leftImageView);
//                imageViewHolder.leftImageView.setImageBitmap(ChatImgUtil.getBitmapFromURL(message.getImage()));
                imageViewHolder.otherUserNameTextView.setText(model.getSenderName());
                imageViewHolder.otherUserNameTextView.setVisibility(View.VISIBLE);
            }
        } else if (holder.getItemViewType() == VIEW_TYPE_LOCATION_MESSAGE) {
            LocationViewHolder locationViewHolder = (LocationViewHolder) holder;
            // Set data to image view holder
            if (model.getSenderId().equals(ChatFirebaseUtil.currentUserId())) {
                locationViewHolder.otherUserNameTextView.setVisibility(View.GONE);
                locationViewHolder.leftCardView.setVisibility(View.GONE);
                locationViewHolder.rightCardView.setVisibility(View.VISIBLE);
                locationViewHolder.ownLocationNameTextview.setText(model.getLocation().getLocationName());
                locationViewHolder.ownLocationAddressTextview.setText(model.getLocation().getLocationAddress());
            } else {
                locationViewHolder.rightCardView.setVisibility(View.GONE);
                locationViewHolder.leftCardView.setVisibility(View.VISIBLE);
                locationViewHolder.otherUserNameTextView.setText(model.getSenderName());
                locationViewHolder.otherUserNameTextView.setVisibility(View.VISIBLE);
                locationViewHolder.otherLocationNameTextview.setText(model.getLocation().getLocationName());
                locationViewHolder.otherLocationAddressTextview.setText(model.getLocation().getLocationAddress());
            }
        }
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        TextView otherUserNameTextView, leftChatTextView, rightChatTextView;


        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserNameTextView = itemView.findViewById(R.id.other_user_name);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextView = itemView.findViewById(R.id.right_chat_textview);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
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

    class LocationViewHolder extends RecyclerView.ViewHolder {
        CardView leftCardView, rightCardView;
        ImageView leftImageView, rightImageView;
        TextView otherUserNameTextView;
        TextView otherLocationNameTextview,otherLocationAddressTextview;
        TextView ownLocationNameTextview,ownLocationAddressTextview;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserNameTextView = itemView.findViewById(R.id.other_user_name);
            leftImageView = itemView.findViewById(R.id.img_friend);
            rightImageView = itemView.findViewById(R.id.img_own);
            leftCardView = itemView.findViewById(R.id.card_view_friend);
            rightCardView = itemView.findViewById(R.id.card_view_own);
            otherLocationNameTextview = itemView.findViewById(R.id.other_location_name);
            otherLocationAddressTextview = itemView.findViewById(R.id.other_location_address);
            ownLocationNameTextview = itemView.findViewById(R.id.own_location_name);
            ownLocationAddressTextview = itemView.findViewById(R.id.own_location_address);
        }
    }
}
