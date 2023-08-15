package edu.northeastern.pawsomepals.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.ui.feed.CreateEventsActivity;
import edu.northeastern.pawsomepals.ui.feed.CreatePhotoVideoActivity;
import edu.northeastern.pawsomepals.ui.feed.CreatePostActivity;
import edu.northeastern.pawsomepals.ui.feed.CreateRecipeActivity;
import edu.northeastern.pawsomepals.ui.feed.CreateServicesActivity;
import edu.northeastern.pawsomepals.ui.feed.FeedFragmentViewType;
import edu.northeastern.pawsomepals.ui.profile.ProfileFeedFragment;

public class DialogHelper {

    public static void showCancelConfirmationDialog(Context context, Activity activity) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm Action")
                .setMessage("Are you sure you want to cancel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void hideProgressDialog(Dialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static void showProgressDialog(String s, Dialog progressDialog, Activity activity) {
        if (progressDialog == null) {
            progressDialog = new Dialog(activity);
            progressDialog.setContentView(R.layout.custom_progress_dialog);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView progressMessageTextView = progressDialog.findViewById(R.id.progressMessageTextView);
        if (progressMessageTextView != null) {
            progressMessageTextView.setText(s);
        }

        progressDialog.show();
    }

    public static PopupMenu getPopupMenu(AppCompatActivity activity, View view, FeedItem feedItem, FeedFragmentViewType feedFragmentViewType) {
        Context context = activity.getBaseContext();
        PopupMenu popupMenu = new PopupMenu(activity.getBaseContext(), view);
        popupMenu.inflate(R.menu.post_overflow_menu);
        MenuItem editMenuItem = popupMenu.getMenu().findItem(R.id.action_edit);

        if (editMenuItem != null) {
            editMenuItem.setVisible(feedItem.getCreatedBy().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        }

        MenuItem favMenuItem = popupMenu.getMenu().findItem(R.id.action_goToFav);
        if (favMenuItem != null && feedFragmentViewType == FeedFragmentViewType.FAVOURITE) {
            favMenuItem.setVisible(false);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    Intent intent = null;

                    if(feedItem.getType()==1){
                        intent = new Intent(context, CreatePhotoVideoActivity.class);}
                    else if (feedItem.getType()==2) {
                        intent = new Intent(context, CreateServicesActivity.class);}
                    else if (feedItem.getType()==3) {
                        intent = new Intent(context, CreateEventsActivity.class);}
                    else if (feedItem.getType()==4) {
                        intent = new Intent(context, CreatePostActivity.class);}
                    else if (feedItem.getType()==5) {
                        intent = new Intent(context, CreateRecipeActivity.class);}

                    intent.putExtra("existingFeedItem", feedItem);
                    activity.startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_goToFav) {
                    ProfileFeedFragment feedFragment = new ProfileFeedFragment();
                    Bundle args = new Bundle();

                    args.putString("profileId", feedItem.getCreatedBy());
                    args.putString("tabText", "Favourites");
                    args.putSerializable("feed_view_type", FeedFragmentViewType.FAVOURITE);
                    feedFragment.setArguments(args);

                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container_view, feedFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                    return true;
                }
                return false;
            }
        });

        return popupMenu;
    }
}


