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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;

public class TaggingOptionsLayout extends LinearLayout {

    private TextView tagPeopleTextView;
    private TextView tagLocationSearchTextView;
    private final Map<String, Users> allUsers = new HashMap<>();
    private final List<Users> selectedUsers = new ArrayList<>();

    public TextView getTagPeopleTextView() {
        return tagPeopleTextView;
    }

    public void setTagPeopleTextView(String tagPeopleTextView) {
        this.tagPeopleTextView.setText(tagPeopleTextView);
    }

    public TextView getTagLocationSearchTextView() {
        return tagLocationSearchTextView;
    }

    public void setTagLocationTextView(String tagLocationSearchTextView) {
        this.tagLocationSearchTextView.setText(tagLocationSearchTextView);
    }

    private ActivityResultLauncher<Intent> startAutocomplete = null;

    public TaggingOptionsLayout(Context context) {
        super(context);
        init(context);
    }

    public TaggingOptionsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TaggingOptionsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TaggingOptionsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        addView(addLocationLabel(context, R.string.tag_people));
        tagPeopleTextView = addSearchText(context, R.id.tag_people_search_tv, "Tag people");
        addView(tagPeopleTextView);
        addView(addLocationLabel(context, R.string.add_location));
        tagLocationSearchTextView = addSearchText(context, R.id.tag_people_search_tv, "Tag a location");
        addView(tagLocationSearchTextView);
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

    private TextView addSearchText(Context context, @IdRes int id, String hint) {
        TextView tv = new TextView(context);
        tv.setId(id);
        tv.setHint(hint);
        float textSizeSp = 16;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        tv.setBackground(AppCompatResources.getDrawable(context, R.drawable.border));
        return tv;
    }

    public void bindView(AppCompatActivity activity, OnTaggedDataFetchListener onTaggedDataFetchListener) {
        fetchAllUsersFromFirestore();
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

        tagPeopleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserSelectionDialog(onTaggedDataFetchListener);
            }
        });
    }

    public interface OnTaggedDataFetchListener {
        void onLocation(LatLng latLng, String locationTagged);
        void onTaggedUsersGet(String usersTagged);
    }

    private void showUserSelectionDialog(OnTaggedDataFetchListener onTaggedDataFetchListener) {
        List<String> userNames = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        boolean[] checkedItems = new boolean[allUsers.size()];

        int i = 0;
        for (Map.Entry<String, Users> entry : allUsers.entrySet()) {
            Users user = entry.getValue();
            if (user.getName() != null) {
                userNames.add(user.getName());
                userIds.add(user.getUserId());
            }
            checkedItems[i++] = selectedUsers.contains(user);
        }

        String[] userNamesArray = userNames.toArray(new String[userNames.size()]);
        String[] userIdsArray = userIds.toArray(new String[userNames.size()]);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.getContext());
        builder.setTitle("Select Users")
                .setMultiChoiceItems(userNamesArray, checkedItems, (dialog, which, isChecked) -> {
                    Users user = allUsers.get(userIdsArray[which]);
                    if (isChecked) {
                        selectedUsers.add(user);
                    } else {
                        selectedUsers.remove(user);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    updateSelectedUsersTextView(onTaggedDataFetchListener);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchAllUsersFromFirestore() {
        FirebaseFirestore.getInstance().collection("user")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        allUsers.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Users user = document.toObject(Users.class);
                            if (!user.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                allUsers.putIfAbsent(user.getUserId(), user);
                            }
                        }
                    }
                });
    }

    private void updateSelectedUsersTextView(OnTaggedDataFetchListener onTaggedDataFetchListener) {
        List<String> selectedNames = new ArrayList<>();
        for (Users user : selectedUsers) {
            selectedNames.add(user.getName());
        }

        StringBuilder commaSeparatedNames = new StringBuilder();
        for (int i = 0; i < selectedNames.size(); i++) {
            commaSeparatedNames.append(selectedNames.get(i));
            if (i < selectedNames.size() - 1) {
                commaSeparatedNames.append(", ");
            }
        }
        onTaggedDataFetchListener.onTaggedUsersGet(commaSeparatedNames.toString());
        tagPeopleTextView.setText(commaSeparatedNames.toString());


    }
}