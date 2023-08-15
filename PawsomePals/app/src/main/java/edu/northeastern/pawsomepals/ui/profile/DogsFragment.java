package edu.northeastern.pawsomepals.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfileDogAdapter;
import edu.northeastern.pawsomepals.models.Dogs;

public class DogsFragment extends Fragment {
    private RecyclerView recyclerViewDogs;
    private ProfileDogAdapter dogProfileAdapter;
    private final List<Dogs> dogProfiles = new ArrayList<>();
    private TextView textNoDogProfiles;
    public DogsFragment() {
        // Required empty public constructor
    }


    public static DogsFragment newInstance(String profileId, boolean isUserProfile) {
        DogsFragment fragment = new DogsFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_user_profile", isUserProfile);
        args.putString("profile_id", profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewDogs = view.findViewById(R.id.recyclerView);
        textNoDogProfiles = view.findViewById(R.id.textViewEmptyList);

        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewDogs.setLayoutManager(verticalLayoutManager);

        Bundle args = getArguments();
        if (args != null) {
            boolean isUserProfile = args.getBoolean("is_user_profile");
            String userId = args.getString("profile_id");
            dogProfileAdapter = new ProfileDogAdapter(dogProfiles, requireContext(), isUserProfile);

            recyclerViewDogs.setAdapter(dogProfileAdapter);

            fetchProfiles(userId);

            // Show/hide the TextView based on the availability of dog profiles
            if (dogProfiles != null && !dogProfiles.isEmpty()) {
                recyclerViewDogs.setVisibility(View.VISIBLE);
                textNoDogProfiles.setVisibility(View.GONE);
            } else {
                recyclerViewDogs.setVisibility(View.GONE);
                textNoDogProfiles.setText("No dog profiles available");
                textNoDogProfiles.setVisibility(View.VISIBLE);
            }
        } else {
            // Handle the case when the arguments are null or not available
            recyclerViewDogs.setVisibility(View.GONE);
            textNoDogProfiles.setText("No dog profiles available");
            textNoDogProfiles.setVisibility(View.VISIBLE);
        }


    }

    private void fetchProfiles(String userIdValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("dogs")
                .whereEqualTo("userId", userIdValue)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Fetch Dog Profiles", "Error fetching dog profiles", error);
                        return;
                    }
                    List<Dogs> dogUserProfiles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Dogs dogProfile = document.toObject(Dogs.class);
                        dogProfile.setDogId(document.getId());
                        dogProfile.setIsDeleted(false);
                        dogProfile.setExpandable(false);
                        dogUserProfiles.add(dogProfile);

                    }
                    dogProfileAdapter.setDogProfiles(dogUserProfiles);
                    if (dogUserProfiles != null && !dogUserProfiles.isEmpty() && !(dogUserProfiles.size()==0)) {
                        recyclerViewDogs.setVisibility(View.VISIBLE);
                        textNoDogProfiles.setVisibility(View.GONE);
                    } else {
                        recyclerViewDogs.setVisibility(View.GONE);
                        textNoDogProfiles.setText("No dog profiles available");
                        textNoDogProfiles.setVisibility(View.VISIBLE);
                    }
                    dogProfileAdapter.notifyDataSetChanged();
                });
    }

}