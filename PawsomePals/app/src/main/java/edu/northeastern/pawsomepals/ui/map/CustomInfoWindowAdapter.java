package edu.northeastern.pawsomepals.ui.map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.login.HomeActivity;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter,GoogleMap.OnMarkerClickListener {
    private final AppCompatActivity activity;
    private final View infoWindowView;
    private final FeedItem feedItem;


    public CustomInfoWindowAdapter(AppCompatActivity activity, FeedItem feedItem) {
        this.activity = activity;
        infoWindowView = LayoutInflater.from(activity).inflate(R.layout.custom_info_window, null);
        this.feedItem = feedItem;
    }


    @Override
    public View getInfoContents(Marker marker) {
        return null; // Return null to use getInfoContents
    }

    @Override
    public View getInfoWindow(Marker marker) {
        String imageUrl = null;
        ImageView imageView = infoWindowView.findViewById(R.id.imageView);
        TextView titleTextView = infoWindowView.findViewById(R.id.titleTextView);
        TextView snippetTextView = infoWindowView.findViewById(R.id.snippetTextView);
        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "here", Toast.LENGTH_SHORT).show();
                showOptionsDialog();
            }
        });

        infoWindowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (feedItem.getType() == 1) {
             imageUrl =((PhotoVideo) feedItem).getImg();
        } else if (feedItem.getType() == 2) {
            imageView.setVisibility(View.GONE);
        } else if (feedItem.getType() == 3) {
            imageUrl =((Event) feedItem).getImg();
        } else if (feedItem.getType() == 4) {
            imageView.setVisibility(View.GONE);
        }


        if (imageUrl != null) {
            Glide.with(activity)
                    .load(imageUrl)
                    .placeholder(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder) // You can use a placeholder image
                    .into(imageView);
        }
        return infoWindowView;
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose an Option")
                .setItems(new CharSequence[]{"Open in Google Maps", "View Feed Item"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            // Open in Google Maps option
                            openLocationInGoogleMaps();
                        } else if (which == 1) {
                            // View Feed Item option
                            viewFeedItem();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openLocationInGoogleMaps() {
        LatLng location = new LatLng(feedItem.getLatLng().getLatitude(), feedItem.getLatLng().getLongitude());
        Uri gmmIntentUri = Uri.parse("geo:" + location.latitude + "," + location.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(mapIntent);
        } else {
            Toast.makeText(activity, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewFeedItem() {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra("feedId", feedItem.getFeedItemId());
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }
}

