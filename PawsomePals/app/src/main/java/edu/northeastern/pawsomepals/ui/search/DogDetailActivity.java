package edu.northeastern.pawsomepals.ui.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.pawsomepals.R;

public class DogDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);

        Intent intent = getIntent();
        String dogName = intent.getStringExtra("name");
        String dogImage = intent.getStringExtra("image");

        Log.d("dogname", dogName);

        TextView dogNameText = findViewById(R.id.DogName);
        CircleImageView dogImageCircle = findViewById(R.id.imageProfile);

        dogNameText.setText(dogName);
        Glide.with(this)
                .load(dogImage)
                .into(dogImageCircle);
    }
}