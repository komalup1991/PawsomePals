package edu.northeastern.pawsomepals.ui.map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;

public class MapFragment extends Fragment implements OnMapReadyCallback, FirestoreDataLoader.FirestoreDataListener {

    private MapView mapView;
    private GoogleMap googleMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        loadMarkersOnMap();
    }

    private void loadMarkersOnMap() {
        List<CollectionReference> collections = new ArrayList<>();
        collections.add(FirebaseFirestore.getInstance().collection("events"));
        collections.add(FirebaseFirestore.getInstance().collection("posts"));
        collections.add(FirebaseFirestore.getInstance().collection("services"));
        collections.add(FirebaseFirestore.getInstance().collection("photovideo"));

        FirestoreDataLoader firestoreDataLoader = new FirestoreDataLoader(this, collections, "createdAt");
        firestoreDataLoader.loadDataFromCollections();
    }

    @Override
    public void onDataLoaded(List<FeedItem> feedItems) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (googleMap != null) {
                    for (FeedItem item : feedItems) {
                        if (item.getLatLng() != null) {
                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(item.getLatLng().getLatitude(), item.getLatLng().getLongitude())));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
}
