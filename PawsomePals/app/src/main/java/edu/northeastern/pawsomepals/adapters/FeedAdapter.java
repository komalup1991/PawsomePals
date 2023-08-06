package edu.northeastern.pawsomepals.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.EventFeedViewHolder;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.PhotoVideoFeedViewHolder;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.PostFeedViewHolder;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.RecipeRecyclerViewHolder;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.ServicesFeedViewHolder;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<FeedItem> feedItems;
    private Context context;

    private LocationClickListener locationClickListener;

    public FeedAdapter(List<FeedItem> feedItems, Context context, LocationClickListener locationClickListener) {
        this.feedItems = feedItems;
        this.context = context;
        this.locationClickListener = locationClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case FeedItem.TYPE_RECIPE_HEADER -> {
                view = layoutInflater.inflate(R.layout.recipe_recycler_view_layout, parent, false);
                return new RecipeRecyclerViewHolder(view);
            }
            case FeedItem.TYPE_POST -> {
                view = layoutInflater.inflate(R.layout.feed_post_item, parent, false);
                return new PostFeedViewHolder(view);
            }
            case FeedItem.TYPE_PHOTO_VIDEO -> {
                view = layoutInflater.inflate(R.layout.feed_photovideo_item, parent, false);
                return new PhotoVideoFeedViewHolder(view);
            }
            case FeedItem.TYPE_EVENT -> {
                view = layoutInflater.inflate(R.layout.feed_event_item, parent, false);
                return new EventFeedViewHolder(view);
            }
            case FeedItem.TYPE_SERVICE -> {
                view = layoutInflater.inflate(R.layout.feed_services_item, parent, false);
                return new ServicesFeedViewHolder(view);
            }
            case FeedItem.TYPE_RECIPE -> {
                view = layoutInflater.inflate(R.layout.recipe_all_layout, parent, false);
                return new RecipeAllAdapter.RecipeAllViewHolder(view);
            }
            default -> throw new UnsupportedOperationException("Invalid item");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedItem feedItem = this.feedItems.get(position);
        switch (feedItem.getType()) {
            case FeedItem.TYPE_RECIPE_HEADER ->
                    ((RecipeRecyclerViewHolder) holder).bindRecylerViewData();
            case FeedItem.TYPE_POST ->
                    ((PostFeedViewHolder) holder). bindData (((Activity)context), (Post) feedItem, locationClickListener);
            case FeedItem.TYPE_PHOTO_VIDEO ->
                    ((PhotoVideoFeedViewHolder) holder).bindData(((Activity)context), (PhotoVideo) feedItem, locationClickListener);
            case FeedItem.TYPE_EVENT ->
                    ((EventFeedViewHolder) holder).bindData(((Activity)context), (Event) feedItem, locationClickListener);
            case FeedItem.TYPE_SERVICE ->
                    ((ServicesFeedViewHolder) holder).bindData(((Activity)context), (Services) feedItem, locationClickListener);
            case FeedItem.TYPE_RECIPE ->
                    ((RecipeAllAdapter.RecipeAllViewHolder) holder).bindData(((Activity)context), (Recipe) feedItem, null);
        }
    }



    @Override
    public int getItemViewType(int position) {
        return this.feedItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return this.feedItems.size();
    }

    public interface LocationClickListener {
        void onClick(FeedItem feedItem);
    }

}
