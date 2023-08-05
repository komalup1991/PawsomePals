package edu.northeastern.pawsomepals.ui.feed.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.ui.feed.CommentActivity;

public class TagLocationLayout extends LinearLayout {

    private TextView locationLabelTextView;
    private TextView searchTextView;

    private ActivityResultLauncher<Intent> startAutocomplete = null;

    public TagLocationLayout(Context context) {
        super(context);
        init(context);
    }

    public TagLocationLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TagLocationLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TagLocationLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        addLocationLabel(context);
        addSearchText(context);
        addLocationLabel(context);
        addSearchText(context);
    }

    private void addLocationLabel(Context context) {
        locationLabelTextView = new TextView(context);
        locationLabelTextView.setTextColor(context.getColor(R.color.darkPink));
        locationLabelTextView.setText(context.getString(R.string.add_location));
        locationLabelTextView.setAllCaps(true);
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        locationLabelTextView.setTypeface(boldTypeface);
        locationLabelTextView.setPadding(0, 0, 0, 16);
        addView(locationLabelTextView);
    }

    private void addSearchText(Context context) {
        searchTextView = new TextView(context);
        searchTextView.setHint("Tag a location");
        float textSizeSp = 16;
        searchTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        searchTextView.setBackground(AppCompatResources.getDrawable(context, R.drawable.border));
        addView(searchTextView);
    }

    public void bindView(AppCompatActivity activity, OnLocationFetchListener onLocationFetchListener) {
        if (startAutocomplete == null) {
            startAutocomplete = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                Place place = Autocomplete.getPlaceFromIntent(intent);
                                String displayText = place.getName() + "\n" + place.getAddress();
                                onLocationFetchListener.onLocation(place.getLatLng(), displayText);
                                searchTextView.setText(displayText);

                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            // The user canceled the operation.
                        }
                    });
        }
        searchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                        Place.Field.ADDRESS, Place.Field.LAT_LNG);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(activity);
                startAutocomplete.launch(intent);
            }
        });
    }

    public interface OnLocationFetchListener {
        void onLocation(LatLng latLng, String locationTagged);
    }
}