package edu.northeastern.pawsomepals.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import edu.northeastern.pawsomepals.models.FeedItemWithImage;
import edu.northeastern.pawsomepals.models.PhotoVideo;
import edu.northeastern.pawsomepals.models.Post;
import edu.northeastern.pawsomepals.models.Services;
import edu.northeastern.pawsomepals.ui.feed.FeedCollectionType;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;
import edu.northeastern.pawsomepals.ui.login.HomeActivity;

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
            if (Double.isNaN(selectedFeedItem.getLatLng().getLatitude())) {
                Toast.makeText(getContext(), "Please check the coordinates", Toast.LENGTH_SHORT).show();
            } else {
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
                    Marker firstMarker = null;
                    for (FeedItem item : feedItems) {
                        if (item.getLatLng() != null) {
                            if (firstItem == null) {
                                firstItem = item;
                            }
                            feedItemMap.put(item.getFeedItemId(), item);
                            Marker newMarker = feedMarker(item);
                            newMarker.setTag(item.getFeedItemId());
                            if (firstMarker == null) {
                                firstMarker = newMarker;
                            }
                        }
                    }

                    if (firstItem != null && selectedFeedItem == null) {
                        showCustomInfoWithImageFetch(firstItem, firstMarker);
                        moveMapToCurrentLocation(firstItem.getLatLng().getLatitude(), firstItem.getLatLng().getLongitude(), 10);
                    }
                }
            }
        });
    }

    private Marker feedMarker(FeedItem feedItem) {
        BitmapDescriptor icon = bitmapFromVector(activity.getApplicationContext(), R.drawable.dogpawheart, 80);


        MarkerOptions marker = new MarkerOptions();
        //String snippet = username + "\n" + "Location: " + latLng.toString(); // Customize this formatting as needed

        marker.position(new LatLng(feedItem.getLatLng().getLatitude(), feedItem.getLatLng().getLongitude()));

//        if (feedItem.getType() == 1) {
//            marker.title(((PhotoVideo) feedItem).getCaption())
//                    .icon(icon)
//                    .snippet("By: "+ feedItem.getUsername()+"\n"+feedItem.getLocationTagged())
//                    .position(new LatLng(feedItem.getLatLng().getLatitude(), feedItem.getLatLng().getLongitude()));
//        } else if (feedItem.getType() == 2) {
//            marker.title(((Services) feedItem).getServiceName())
//                    .icon(icon)
//                    .snippet("By: "+feedItem.getUsername()+"\n"+feedItem.getLocationTagged())
//                    .position(new LatLng(feedItem.getLatLng().getLatitude(), feedItem.getLatLng().getLongitude()));
//        } else if (feedItem.getType() == 3) {
//            marker.title(((Event) feedItem).getEventName())
//                    .icon(icon)
//                    .snippet("By: "+feedItem.getUsername()+"\n"+feedItem.getLocationTagged())
//                    .position(new LatLng(feedItem.getLatLng().getLatitude(), feedItem.getLatLng().getLongitude()));
//        } else if (feedItem.getType() == 4) {
//            marker.title(((Post) feedItem).getCaption())
//                    .icon(icon)
//                    .snippet("By: "+feedItem.getUsername()+"\n"+feedItem.getLocationTagged())
//                    .position(new LatLng(feedItem.getLatLng().getLatitude(), feedItem.getLatLng().getLongitude()));
//        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                FeedItem selectedItem = feedItemMap.get(marker.getTag());
                if (selectedItem instanceof FeedItemWithImage) {
                    showCustomInfoWithImageFetch(selectedItem, marker);
                } else {
                    showCustomInfo(selectedItem, marker, null);
                }
                return true;
            }
        });

        return googleMap.addMarker(marker);
    }

    private void showCustomInfoWithImageFetch(FeedItem feedItem, Marker marker) {
        if (feedItem instanceof FeedItemWithImage) {
            Glide.with(mapView).load(((FeedItemWithImage) feedItem).getImg()).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    showCustomInfo(feedItem, marker, resource);
                    return true;
                }
            }).preload();
        } else {
            showCustomInfo(feedItem, marker, null);
        }
    }

    private void showCustomInfo(FeedItem selectedItem, Marker marker, Drawable resource) {
        if (selectedItem != null) {
            CustomInfoWindowAdapter infoWindowAdapter = new CustomInfoWindowAdapter((AppCompatActivity) requireActivity(), selectedItem, resource);
            googleMap.setInfoWindowAdapter(infoWindowAdapter);
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    showOptionsDialog(selectedItem, marker);
                }
            });
        }
        marker.showInfoWindow();
    }

    private BitmapDescriptor bitmapFromVector(Context context, int vectorResId, int sizeOfMarker) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, sizeOfMarker, sizeOfMarker);
        Bitmap bitmap = Bitmap.createBitmap(sizeOfMarker, sizeOfMarker, Bitmap.Config.ARGB_8888);
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(), null).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    moveMapToCurrentLocation(location.getLatitude(), location.getLongitude(), 9);
                }
            }
        });
    }

    private void showOptionsDialog(FeedItem selectedItem, Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View customView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_map, null);
        builder.setView(customView);

        Button openMapsButton = customView.findViewById(R.id.open_maps_button);
        Button viewItemButton = customView.findViewById(R.id.view_item_button);

        openMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationInGoogleMaps(selectedItem);
            }
        });
        viewItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFeedItem(marker);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openLocationInGoogleMaps(FeedItem selectedItem) {
        LatLng location = new LatLng(selectedItem.getLatLng().getLatitude(), selectedItem.getLatLng().getLongitude());
        Uri gmmIntentUri = Uri.parse("geo:" + location.latitude + "," + location.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(mapIntent);
        } else {
            Toast.makeText(activity, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewFeedItem(Marker marker) {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra("feedId", marker.getTag().toString());
        activity.startActivity(intent);
        activity.finish();
    }

}
