package edu.northeastern.pawsomepals.ui.feed;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;

import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.FeedItemWithImage;
import edu.northeastern.pawsomepals.utils.ActivityHelper;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;

public class SaveOrUpdateFeedUtil {

    public static void handleSaveFeed(Activity activity, Dialog progressDialog, FeedItem existingFeedItem, Uri imageUri, boolean hasImageChanged) {
        if (isEditMode(existingFeedItem)) {
            if (hasImageChanged) {
                uploadImageAndUpdateFeedData(activity, (FeedItemWithImage) existingFeedItem, progressDialog, imageUri);
            } else {
                createFeedMap(activity, existingFeedItem, progressDialog, null);
            }
        } else {
            uploadImageAndUpdateFeedData(activity, (FeedItemWithImage) existingFeedItem, progressDialog, imageUri);
        }
    }

    public static boolean isEditMode(FeedItem existingFeedItem) {
        return existingFeedItem != null;
    }

    private static boolean didImageUpdate(FeedItem existingFeedItem, Uri imageUri) {
        if (!doesFeedHaveImage(existingFeedItem)) {
            return false;
        }
        return !((FeedItemWithImage) existingFeedItem).getImg().equals(imageUri.toString());
    }

    private static boolean doesFeedHaveImage(FeedItem item) {
        return item instanceof FeedItemWithImage;
    }

    private static void createFeedMap(Activity activity, FeedItem feedItem, Dialog progressDialog, String imageUrl) {
        if (imageUrl != null) {
            ((FeedItemWithImage) feedItem).setImg(imageUrl);
        }
        FirebaseUtil.createCollectionInFirestoreNew(feedItem, FirebaseUtil.getPostType(feedItem), new BaseDataCallback() {
            @Override
            public void onDismiss() {
                DialogHelper.hideProgressDialog(progressDialog);
                ActivityHelper.setResult(activity, true);
                activity.finish();
            }
        });
    }

    private static void uploadImageAndUpdateFeedData(Activity activity, FeedItemWithImage feedItem, Dialog progressDialog, Uri imageUri) {
        FirebaseUtil.uploadImageToStorage(imageUri,
                "event", new BaseDataCallback() {

                    @Override
                    public void onImageUriReceived(String imageUrl) {
                        createFeedMap(activity, feedItem, progressDialog, imageUrl);
                    }

                    @Override
                    public void onDismiss() {
                        DialogHelper.hideProgressDialog(progressDialog);
                        activity.finish();
                    }

                    @Override
                    public void onError(Exception exception) {
                    }
                });
    }


}
