package edu.northeastern.pawsomepals.ui.feed;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Locale;

import edu.northeastern.pawsomepals.R;

public class CreateEventsActivity extends AppCompatActivity {
    private TextView setEventDateTextView, setEventTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_events);
        setEventDateTextView = findViewById(R.id.setEventDateTextView);
        setEventDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        setEventTimeTextView = findViewById(R.id.setEventTimeTextView);
        setEventTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder().setInputMode(INPUT_MODE_CLOCK).build();
        picker.show(getSupportFragmentManager(), "TIME_PICKER");
        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder time = new StringBuilder();
                time.append(picker.getHour()).append(picker.getMinute());
                setEventTimeTextView.setText(time);
            }
        });


    }

    private void showDatePicker() {

        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date of the event").build();
        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
//        materialDatePicker.
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String date = sdf.format(selection);
                setEventDateTextView.setText(date.toString());

               // Log.d("DATE KOMAL = ", "sel = "  + date);
            }
        });

    }
}