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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ProfilePhotoAdapter;
import edu.northeastern.pawsomepals.models.PhotoVideo;

public class PhotosFragment extends Fragment {
    private RecyclerView recyclerViewPhotos;
    private ProfilePhotoAdapter profilePhotoAdapter;
    private final List<PhotoVideo> photos = new ArrayList<>();
    private TextView textNoPhotoProfiles;
    public PhotosFragment() {
        // Required empty public constructor
    }


    public static PhotosFragment newInstance(String profileId) {
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
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

        recyclerViewPhotos = view.findViewById(R.id.recyclerView);
        textNoPhotoProfiles = view.findViewById(R.id.textViewEmptyList);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerViewPhotos.setLayoutManager(layoutManager);

        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("profile_id");
            profilePhotoAdapter = new ProfilePhotoAdapter(photos, requireContext());

            recyclerViewPhotos.setAdapter(profilePhotoAdapter);

            fetchPhotos(userId);

            // Show/hide the TextView based on the availability of dog profiles
            if (photos != null && !photos.isEmpty()) {
                recyclerViewPhotos.setVisibility(View.VISIBLE);
                textNoPhotoProfiles.setVisibility(View.GONE);
            } else {
                recyclerViewPhotos.setVisibility(View.GONE);
                textNoPhotoProfiles.setText("No photos available");
                textNoPhotoProfiles.setVisibility(View.VISIBLE);
            }
        } else {
            // Handle the case when the arguments are null or not available
            recyclerViewPhotos.setVisibility(View.GONE);
            textNoPhotoProfiles.setText("No photos available");
            textNoPhotoProfiles.setVisibility(View.VISIBLE);
        }


    }

    private void fetchPhotos(String userIdValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("photovideo")
                .whereEqualTo("createdBy", userIdValue)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Fetch Photos", "Error fetching recipes", error);
                        return;
                    }
                    List<PhotoVideo> userPhotos = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        PhotoVideo userPhoto = document.toObject(PhotoVideo.class);
                        userPhotos.add(userPhoto);
                    }
                    profilePhotoAdapter.setPhotos(userPhotos);
                    if (userPhotos != null && !userPhotos.isEmpty() && !(userPhotos.size()==0)) {
                        recyclerViewPhotos.setVisibility(View.VISIBLE);
                        textNoPhotoProfiles.setVisibility(View.GONE);
                    } else {
                        recyclerViewPhotos.setVisibility(View.GONE);
                        textNoPhotoProfiles.setText("No recipes available");
                        textNoPhotoProfiles.setVisibility(View.VISIBLE);
                    }
                    profilePhotoAdapter.notifyDataSetChanged();
                });
    }
}
