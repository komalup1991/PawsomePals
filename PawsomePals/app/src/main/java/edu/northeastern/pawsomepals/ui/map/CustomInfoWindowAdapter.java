package edu.northeastern.pawsomepals.ui.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final Context context;
    private final View infoWindowView;
    private final FeedItem feedItem;


    public CustomInfoWindowAdapter(Context context, FeedItem feedItem) {
        this.context = context;
        infoWindowView = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
        this.feedItem = feedItem;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null; // Return null to use getInfoContents
    }

    @Override
    public View getInfoContents(Marker marker) {
        String imageUrl = null;
        ImageView imageView = infoWindowView.findViewById(R.id.imageView);
        TextView titleTextView = infoWindowView.findViewById(R.id.titleTextView);
        TextView snippetTextView = infoWindowView.findViewById(R.id.snippetTextView);
        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());

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
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder) // You can use a placeholder image
                    .into(imageView);
        }
        return infoWindowView;
    }
}

