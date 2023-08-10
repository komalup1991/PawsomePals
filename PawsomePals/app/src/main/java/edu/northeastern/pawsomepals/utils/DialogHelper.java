package edu.northeastern.pawsomepals.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.FeedItem;

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

    public static void showMoreOptionsMenu(Context context, FeedItem feedItem, View view){
      //  Dialog moreOptionDialog = new Dialog(context);
      //  moreOptionDialog.setContentView(R.layout.dialog_more_options);
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.post_overflow_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    Toast.makeText(context,"dfdf",Toast.LENGTH_SHORT).show();
                    // Handle edit action
                    return true;
                } else if (itemId == R.id.action_delete) {
                    Toast.makeText(context,"dfdf",Toast.LENGTH_SHORT).show();
                    // Handle delete action
                    return true;
                }
                // Handle other menu items as needed
                return false;
            }
        });
        popupMenu.show();
    }
}


