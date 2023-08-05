package edu.northeastern.pawsomepals.ui.feed.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.ui.feed.CommentActivity;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class FeedActionsLayout extends LinearLayout {

    ImageButton likeButton, commentButton, shareButton;
    TextView likeCountTextView, commentCountTextView;
    ImageButton favImageButton;

    public FeedActionsLayout(Context context) {
        super(context);
        init(context);
    }

    public FeedActionsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FeedActionsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FeedActionsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_actions_layout, this, true);
        likeButton = view.findViewById(R.id.likeButton);
        likeCountTextView = view.findViewById(R.id.likeCountTextView);
        commentButton = view.findViewById(R.id.commentButton);
        commentCountTextView = view.findViewById(R.id.commentCountTextView);
        shareButton = view.findViewById(R.id.shareButton);
        favImageButton = view.findViewById(R.id.favImageButton);
    }

    public void bindView(Activity activity, FeedItem feedItem) {
        if (feedItem.getCommentCount() != null) {
            commentCountTextView.
                    setText(String.valueOf(Math.toIntExact(feedItem.getCommentCount())));
        }
        likeCountTextView.
                setText(String.valueOf(Math.toIntExact(feedItem.getLikeCount())));
        if (feedItem.isLiked()) {
            likeButton.setImageResource(R.drawable.like);
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (feedItem.isLiked()) {
                    feedItem.setLiked(false);
                    feedItem.setLikeCount(feedItem.getLikeCount() - 1);
                    likeCountTextView.setText(String.valueOf(feedItem.getLikeCount()));
                    likeButton.setImageResource(R.drawable.likenew);
                    FirebaseUtil.removeLikeFromFirestore(feedItem.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), getPostType(feedItem));
                } else {
                    feedItem.setLiked(true);
                    feedItem.setLikeCount(feedItem.getLikeCount() + 1);
                    likeCountTextView.setText(String.valueOf(feedItem.getLikeCount()));
                    likeButton.setImageResource(R.drawable.like);
                    FirebaseUtil.addLikeToFirestore(feedItem.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), getPostType(feedItem));
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CommentActivity.class);
                intent.putExtra("feedItemId", feedItem.getFeedItemId());
                intent.putExtra("postType", getPostType(feedItem));
                activity.startActivity(intent);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (feedItem.isFavorite()) {
            favImageButton.setImageResource(R.drawable.pawprintfull);
        }

        favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (feedItem.isFavorite()) {
                    feedItem.setFavorite(false);
                    favImageButton.setImageResource(R.drawable.pawprintempty);
                    FirebaseUtil.removeFavFromFirestore(feedItem.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), getPostType(feedItem));
                } else {
                    feedItem.setFavorite(true);
                    favImageButton.setImageResource(R.drawable.pawprintfull);
                    FirebaseUtil.addFavToFirestore(feedItem.getFeedItemId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), getPostType(feedItem));
                }
            }
        });
    }

    private String getPostType(FeedItem feedItem) {
        return switch (feedItem.getType()) {
            case FeedItem.TYPE_PHOTO_VIDEO -> "photovideo";
            case FeedItem.TYPE_SERVICE -> "services";
            case FeedItem.TYPE_EVENT -> "events";
            case FeedItem.TYPE_POST -> "posts";
            case FeedItem.TYPE_RECIPE -> "recipes";
            default -> throw new IllegalStateException("Unexpected value: " + feedItem.getType());
        };
    }
}
