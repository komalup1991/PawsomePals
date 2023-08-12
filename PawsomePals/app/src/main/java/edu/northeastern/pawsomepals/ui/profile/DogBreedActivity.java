package edu.northeastern.pawsomepals.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileAllDogBreedsAdapter;
import edu.northeastern.pawsomepals.models.BreedDetails;
import edu.northeastern.pawsomepals.network.BaseUiThreadCallback;
import edu.northeastern.pawsomepals.network.PawsomePalWebService;

public class DogBreedActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private RecyclerView recyclerView;
    private ProfileAllDogBreedsAdapter profileAllDogBreedsAdapter;
    private List<BreedDetails> dogBreeds = new ArrayList<>();
    private SearchView searchView;
    private PawsomePalWebService pawsomePalWebService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_breed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setTitle("Dog's Breeds");
        }


        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(verticalLayoutManager);

        profileAllDogBreedsAdapter = new ProfileAllDogBreedsAdapter(dogBreeds, this);

        recyclerView.setAdapter(profileAllDogBreedsAdapter);

        populateAllDogBreeds();

        progressBar.setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateAllDogBreeds() {
        PawsomePalWebService.UiThreadCallback uiThreadCallback = new BaseUiThreadCallback() {

            public void onGetAllBreedsDetails(List<BreedDetails> breeds) {

                dogBreeds.addAll(breeds);
                profileAllDogBreedsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DogBreedActivity.this, "Error while fetching breeds.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmptyResult() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DogBreedActivity.this, "Error while fetching breeds.", Toast.LENGTH_SHORT).show();
            }

        };

        pawsomePalWebService = new PawsomePalWebService(uiThreadCallback);
        pawsomePalWebService.getBreedsDetails();
    }

    private void filterList(String newText) {
        List<BreedDetails> filteredList = new ArrayList<>();
        for (BreedDetails breedDetail : dogBreeds) {
            if (breedDetail.getName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(breedDetail);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(DogBreedActivity.this, "No breed found", Toast.LENGTH_SHORT).show();
        } else {
            profileAllDogBreedsAdapter.setFilteredList(filteredList);
        }
    }
}