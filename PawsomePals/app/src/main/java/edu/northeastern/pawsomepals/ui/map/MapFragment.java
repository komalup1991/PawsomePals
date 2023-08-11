package edu.northeastern.pawsomepals.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.FeedCollectionType;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;

public class MapFragment extends Fragment implements OnMapReadyCallback, FirestoreDataLoader.FirestoreDataListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView mapView;
    private GoogleMap googleMap;

    private FusedLocationProviderClient fusedLocationClient;
    private Activity activity;
    private FeedItem selectedFeedItem;
    private NavController navController;
    private NavDirections action;
    private RadioButton radioButtonAll;

    private final Map<String, FeedItem> feedItemMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = requireActivity();
        selectedFeedItem = null;
        if (getArguments() != null) {
            selectedFeedItem = (FeedItem) requireArguments().getSerializable("feedItem");
        }

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.radioButtonAll) {
                    loadMarkersOnMapAll();
                } else if (id == R.id.radioButtonEvent) {
                    loadMarkersOnMap(FeedCollectionType.EVENTS);
                } else if (id == R.id.radioButtonPhotos) {
                    loadMarkersOnMap(FeedCollectionType.PHOT0VIDEO);
                } else if (id == R.id.radioButtonPosts) {
                    loadMarkersOnMap(FeedCollectionType.POSTS);
                } else if (id == R.id.radioButtonServices) {
                    loadMarkersOnMap(FeedCollectionType.SERVICES);
                }
            }
        });

        radioButtonAll = view.findViewById(R.id.radioButtonAll);
        radioButtonAll.setChecked(true);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    private void moveMapToCurrentLocation(double latitude, double longitude, int zoom) {
        if (googleMap == null) {
            return;
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        loadMarkersOnMapAll();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        if (selectedFeedItem != null) {
            if(Double.isNaN(selectedFeedItem.getLatLng().getLatitude())){
              Toast.makeText(getContext(),"Please check the coordinates",Toast.LENGTH_SHORT).show();
            }
            else{
                moveMapToCurrentLocation(selectedFeedItem.getLatLng().getLatitude(), selectedFeedItem.getLatLng().getLongitude(), 12);
            }
        }
        }


    private void loadMarkersOnMapAll() {
        loadMarkersOnMap(null);
    }

    private void loadMarkersOnMap(String feedCollectionType) {
        if (feedCollectionType == null) {
            // Set a listener to handle radio button selection changes
            FirestoreDataLoader.loadDataFromCollections(FirestoreDataLoader.getAllCollections(), this);
        } else {
            FirestoreDataLoader.loadDataFromCollections(new ArrayList<>() {{
                add(FirebaseFirestore.getInstance().collection(feedCollectionType));
            }}, this);
        }
    }

    @Override
    public void onDataLoaded(List<FeedItem> feedItems) {
        if (!isAdded() || !isVisible()) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (googleMap != null) {
                    googleMap.clear();
                    FeedItem firstItem = null;
                    for (FeedItem item : feedItems) {
                        if (item.getLatLng() != null) {
                            if (firstItem == null) {
                                firstItem = item;
                            }
                            feedItemMap.put(item.getFeedItemId(), item);
                            Marker newMarker = feedMarker(item);
                            newMarker.setTag(item.getFeedItemId());
                        }
                    }

                    if (firstItem != null) {
                        moveMapToCurrentLocation(firstItem.getLatLng().getLatitude(), firstItem.getLatLng().getLongitude(), 12);
                    }
                }
            }
        });
    }

    private Marker feedMarker(FeedItem feedItem) {
        BitmapDescriptor icon = bitmapFromVector(
                activity.getApplicationContext(),
                R.drawable.dogpawheart,
                80);


        MarkerOptions marker = new MarkerOptions();

        if (feedItem.getType() == 1) {
            marker.title(((PhotoVideo) feedItem).getCaption())
                    .icon(icon).snippet(feedItem.getUsername())
                    .position(new LatLng(feedItem.getLatLng().getLatitude(),
                            feedItem.getLatLng().getLongitude()));
        } else if (feedItem.getType() == 2) {
            marker.title(((Services) feedItem).getServiceName())
                    .icon(icon).snippet(feedItem.getUsername())
                    .position(new LatLng(feedItem.getLatLng().getLatitude(),
                            feedItem.getLatLng().getLongitude()));
        } else if (feedItem.getType() == 3) {
            marker.title(((Event) feedItem).getEventName())
                    .icon(icon).snippet(feedItem.getUsername())
                    .position(new LatLng(feedItem.getLatLng().getLatitude(),
                            feedItem.getLatLng().getLongitude()));
        } else if (feedItem.getType() == 4) {
            marker.title(((Post) feedItem).getCaption())
                    .icon(icon).snippet(feedItem.getUsername())
                    .position(new LatLng(feedItem.getLatLng().getLatitude(),
                            feedItem.getLatLng().getLongitude()));
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                FeedItem selectedItem = feedItemMap.get(marker.getTag());
                if (selectedItem != null) {
                    CustomInfoWindowAdapter infoWindowAdapter = new CustomInfoWindowAdapter((AppCompatActivity) requireActivity(), selectedItem);
                    googleMap.setInfoWindowAdapter(infoWindowAdapter);
                }
                marker.showInfoWindow();
                return true;
            }
        });

        return googleMap.addMarker(marker);
    }

    private BitmapDescriptor bitmapFromVector(Context context, int vectorResId, int sizeOfMarker) {
        Drawable vectorDrawable = ContextCompat.getDrawable(
                context, vectorResId);
        vectorDrawable.setBounds(
                0, 0, sizeOfMarker,
                sizeOfMarker);
        Bitmap bitmap = Bitmap.createBitmap(
                sizeOfMarker,
                sizeOfMarker,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedFeedItem == null) {
            requestLocationPermission();
        }
        mapView.onResume();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(), null)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            moveMapToCurrentLocation(location.getLatitude(), location.getLongitude(), 9);
                        }
                    }
                });
    }
}
