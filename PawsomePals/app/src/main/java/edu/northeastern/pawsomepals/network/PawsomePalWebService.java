package edu.northeastern.pawsomepals.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.models.BreedDetails;
import edu.northeastern.pawsomepals.models.BreedHeightDetails;
import edu.northeastern.pawsomepals.models.BreedImageDetails;
import edu.northeastern.pawsomepals.models.BreedWeightDetails;

public class PawsomePalWebService {
    private static final String API_KEY = "live_fLrLBAIBG2wu5m1YEvBZvVAHsI6ZIjrSH5RXdGaKVNTNpvjrS5JTp1ohgfIPm3vg";
    private static final String BASE_URL = "https://api.thedogapi.com/";

    private final UiThreadCallback uiThreadCallback;

    private final Handler handler;

    public PawsomePalWebService(UiThreadCallback uiThreadCallback) {
        this.uiThreadCallback = uiThreadCallback;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public interface UiThreadCallback {
        void onGetAllBreedsDetails(List<BreedDetails> breeds);

        void onGetBreedsName(List<String> breedNames);

        void onEmptyResult();

        void onError();
    }

    public void getBreedsDetails() {
        String uri = BASE_URL + "v1/breeds";
        makeNetworkCallForAllBreedsDetails(uri);
    }

    public void getBreedsNames() {
        String uri = BASE_URL + "v1/breeds";
        makeNetworkCallForBreedsNames(uri);
    }

    public void makeNetworkCallForAllBreedsDetails(String uri) {
        new NetworkThread(uri, new NetworkThread.NetworkCallback() {
            @Override
            public void processResponse(String responseData) {
                List<BreedDetails> breeds = handleResponseForAllBreedsDetails(responseData);
                postToUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uiThreadCallback.onGetAllBreedsDetails(breeds);
                    }
                });
            }

            @Override
            public void onError() {
                postErrorToUiThread();
            }

            @Override
            public void onEmptyResult() {
                postEmptyResultToUiThread();
            }
        }).start();
    }

    public void makeNetworkCallForBreedsNames(String uri) {
        new NetworkThread(uri, new NetworkThread.NetworkCallback() {
            @Override
            public void processResponse(String responseData) {
                List<String> breedNames = handleResponseForBreedsNames(responseData);
                postToUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uiThreadCallback.onGetBreedsName(breedNames);
                    }
                });
            }

            @Override
            public void onError() {
                postErrorToUiThread();
            }

            @Override
            public void onEmptyResult() {
                postEmptyResultToUiThread();
            }
        }).start();
    }

    private List<BreedDetails> handleResponseForAllBreedsDetails(String responseData) {
        List<BreedDetails> breeds = new ArrayList<>();
        try {
            JSONArray breedsArray = new JSONArray(responseData);
            for (int i = 0; i < breedsArray.length(); i++) {
                JSONObject breedObject = breedsArray.getJSONObject(i);

                JSONObject weightObject = breedObject.getJSONObject("weight");
                String imperialWeight = weightObject.has("imperial") ? weightObject.getString("imperial") : "";
                String metricWeight = weightObject.has("metric") ? weightObject.getString("metric") : "";

                JSONObject heightObject = breedObject.getJSONObject("height");
                String imperialHeight = heightObject.has("imperial") ? heightObject.getString("imperial") : "";
                String metricHeight = heightObject.has("metric") ? heightObject.getString("metric") : "";

                String id = breedObject.has("id") ? breedObject.getString("id") : "";
                String name = breedObject.has("name") ? breedObject.getString("name") : "";
                String bred_for = breedObject.has("bred_for") ? breedObject.getString("bred_for") : "";
                String breed_group = breedObject.has("breed_group") ? breedObject.getString("breed_group") : "";
                String life_span = breedObject.has("life_span") ? breedObject.getString("life_span") : "";
                String temperament = breedObject.has("temperament") ? breedObject.getString("temperament") : "";
                String origin = breedObject.has("origin") ? breedObject.getString("origin") : "";
                String reference_image_id = breedObject.has("reference_image_id") ? breedObject.getString("reference_image_id") : "";

                BreedImageDetails image;
                if (breedObject.has("image")) {
                    JSONObject imageObject = breedObject.getJSONObject("image");
                    String imageId = imageObject.has("id") ? imageObject.getString("id") : "";
                    String imageWidth = imageObject.has("width") ? imageObject.getString("width") : "";
                    String imageHeight = imageObject.has("height") ? imageObject.getString("height") : "";
                    String imageUrl = imageObject.has("url") ? imageObject.getString("url") : "";
                    image = new BreedImageDetails(imageId, imageWidth, imageHeight, imageUrl);
                } else {
                    image = new BreedImageDetails("", "", "", "");
                }

                BreedWeightDetails weight = new BreedWeightDetails(imperialWeight, metricWeight);

                BreedHeightDetails height = new BreedHeightDetails(imperialHeight, metricHeight);

                BreedDetails breed = new BreedDetails(weight, height, id, name, bred_for, breed_group, life_span, temperament, origin, reference_image_id, image);


                breeds.add(breed);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing errors
        }
        return breeds;
    }

    private List<String> handleResponseForBreedsNames(String responseData) {
        List<String> breedNames = new ArrayList<>();
        try {
            JSONArray breedsArray = new JSONArray(responseData);
            for (int i = 0; i < breedsArray.length(); i++) {
                JSONObject breedObject = breedsArray.getJSONObject(i);

                String name = breedObject.getString("name");

                breedNames.add(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing errors
        }
        return breedNames;
    }

    private void postToUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void postErrorToUiThread() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                uiThreadCallback.onError();
            }
        });
    }

    private void postEmptyResultToUiThread() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                uiThreadCallback.onEmptyResult();
            }
        });
    }
}
