package edu.northeastern.pawsomepals.utils;

import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.Recipe;

public interface OnItemActionListener {
    void onRecipeClick(Recipe recipe);

    void onUserClick(String userId);

    void onLocationClick(FeedItem feedItem);
    void onFeedFilterSpinnerClick(int feedfilter);
}
