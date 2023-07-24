package edu.northeastern.pawsomepals.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.app.DatePickerDialog;

import android.widget.DatePicker;

import edu.northeastern.pawsomepals.R;

public class EditDogProfileActivity extends AppCompatActivity {

    private Spinner spinnerDogBreed;
    private Spinner spinnerMixedBreed;
    private CheckBox checkBoxMixedBreed;
    private EditText editTextDogName;
    private EditText editTextDOB;
    private RadioGroup radioGroupDogGender;
    private RadioGroup radioGroupDogSize;
    private RadioButton radioButtonSmall;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonLarge;
    private boolean isMixedBreedChecked = false;
    private List<String> dogBreedsList;
    private List<String> mixedBreedsList;
    private ProgressBar progressBar;
    private Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dog_profile);

        spinnerDogBreed = findViewById(R.id.spinnerDogBreed);
        spinnerMixedBreed = findViewById(R.id.spinnerMixedBreed);
        checkBoxMixedBreed = findViewById(R.id.checkBoxMixedBreed);
        editTextDogName = findViewById(R.id.editTextName);
        editTextDOB = findViewById(R.id.editTextDOB);
        radioGroupDogGender = findViewById(R.id.radioGroupGender);
        radioGroupDogSize = findViewById(R.id.radioGroupSize);
        radioButtonSmall = findViewById(R.id.radioButtonSmall);
        radioButtonMedium = findViewById(R.id.radioButtonMedium);
        radioButtonLarge = findViewById(R.id.radioButtonLarge);
        progressBar = findViewById(R.id.progressBar);
        calendar = Calendar.getInstance();

        progressBar.setVisibility(View.GONE);

        dogBreedsList = Arrays.asList("Breed 1", "Breed 2", "Breed 3", "Breed 4", "Breed 5");
        ArrayAdapter<String> breedAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dogBreedsList);
        spinnerDogBreed.setAdapter(breedAdapter);

        mixedBreedsList = Arrays.asList("Mixed Breed 1", "Mixed Breed 2", "Mixed Breed 3");
        ArrayAdapter<String> mixedBreedAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mixedBreedsList);
        spinnerMixedBreed.setAdapter(mixedBreedAdapter);

        checkBoxMixedBreed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMixedBreedChecked = isChecked;
            if (isChecked) {
                spinnerMixedBreed.setVisibility(View.VISIBLE);
            } else {
                spinnerMixedBreed.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDogProfile();
            }
        });

        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Update the EditText with the selected date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editTextDOB.setText(sdf.format(calendar.getTime()));
        }
    };

    private void saveDogProfile() {
        String dogName = editTextDogName.getText().toString();
        String dogBreed = spinnerDogBreed.getSelectedItem().toString();
        String dogMixedBreed = isMixedBreedChecked ? spinnerMixedBreed.getSelectedItem().toString() : "Not mixed";
        String dogGender = ((RadioButton) findViewById(radioGroupDogGender.getCheckedRadioButtonId())).getText().toString();
        String dogSize = ((RadioButton) findViewById(radioGroupDogSize.getCheckedRadioButtonId())).getText().toString();

        String dob = editTextDOB.getText().toString();

        Log.d("DogProfile", "Name: " + dogName);
        Log.d("DogProfile", "Breed: " + dogBreed);
        Log.d("DogProfile", "Mixed Breed: " + dogMixedBreed);
        Log.d("DogProfile", "Gender: " + dogGender);
        Log.d("DogProfile", "Size: " + dogSize);
        Log.d("DogProfile", "Date of Birth: " + dob);

        Toast.makeText(this, "Dog profile saved!", Toast.LENGTH_SHORT).show();


    }


}