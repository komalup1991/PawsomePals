package edu.northeastern.pawsomepals.ui.map;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final AppCompatActivity activity;
    private final View infoWindowView;
    private final FeedItem feedItem;

    private final Drawable currentDrawable;


    public CustomInfoWindowAdapter(AppCompatActivity activity, FeedItem feedItem, Drawable resource) {
        this.activity = activity;
        infoWindowView = LayoutInflater.from(activity).inflate(R.layout.custom_info_window, null);
        this.feedItem = feedItem;
        this.currentDrawable = resource;
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

        if (feedItem.getType() == 1) {
            imageUrl = ((PhotoVideo) feedItem).getImg();
        } else if (feedItem.getType() == 2) {
            imageView.setVisibility(View.GONE);
        } else if (feedItem.getType() == 3) {
            imageUrl = ((Event) feedItem).getImg();
        } else if (feedItem.getType() == 4) {
            imageView.setVisibility(View.GONE);
        }

        if (currentDrawable != null) {
            imageView.setImageDrawable(currentDrawable);
        }
        return infoWindowView;
    }
}
