package edu.northeastern.pawsomepals.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.ui.feed.layout.TaggingOptionsLayout;


public class ChatLocationUtil extends LinearLayout {
    private TextView tagLocationSearchTextView;
    private ActivityResultLauncher<Intent> startAutocomplete = null;

    public ChatLocationUtil(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        addView(addLocationLabel(context, R.string.add_location));
        tagLocationSearchTextView = addSearchText(context, R.id.tag_people_search_tv, "Tag a location");
        addView(tagLocationSearchTextView);
    }
    private TextView addSearchText(Context context, @IdRes int id, String hint) {
        TextView tv = new TextView(context);
        tv.setId(id);
        tv.setHint(hint);
        float textSizeSp = 16;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        tv.setBackground(AppCompatResources.getDrawable(context, R.drawable.border));
        return tv;
    }
    private TextView addLocationLabel(Context context, @StringRes int stringRes) {
        TextView tv = new TextView(context);
        tv.setTextColor(context.getColor(R.color.darkPink));
        tv.setText(context.getString(stringRes));
        tv.setAllCaps(true);
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        tv.setTypeface(boldTypeface);
        tv.setPadding(0, 0, 0, 16);
        return tv;
    }

    public void bindView(AppCompatActivity activity, TaggingOptionsLayout.OnTaggedDataFetchListener onTaggedDataFetchListener) {
        if (startAutocomplete == null) {
            startAutocomplete = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                Place place = Autocomplete.getPlaceFromIntent(intent);
                                String displayText = place.getName() + "\n" + place.getAddress();
                                onTaggedDataFetchListener.onLocation(place.getLatLng(), displayText);
                                tagLocationSearchTextView.setText(displayText);

                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            // The user canceled the operation.
                        }
                    });
        }
        tagLocationSearchTextView.setOnClickListener(new View.OnClickListener() {
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


}
