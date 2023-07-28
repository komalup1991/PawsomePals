package edu.northeastern.pawsomepals.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.models.BreedDetails;
import edu.northeastern.pawsomepals.models.BreedWeightDetails;

public class PawsomePalWebService {
    private static final String API_KEY = "live_fLrLBAIBG2wu5m1YEvBZvVAHsI6ZIjrSH5RXdGaKVNTNpvjrS5JTp1ohgfIPm3vg";
    private static final String BASE_URL = "https://api.thecatapi.com/";

    private UiThreadCallback uiThreadCallback;

    private Handler handler;

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
                String imperial = weightObject.getString("imperial");
                String metric = weightObject.getString("metric");

                String id = breedObject.getString("id");
                String name = breedObject.getString("name");
                String cfa_url = breedObject.getString("cfa_url");
                String vetstreet_url = breedObject.getString("vetstreet_url");
                String vcahospitals_url = breedObject.getString("vcahospitals_url");
                String temperament = breedObject.getString("temperament");
                String origin = breedObject.getString("origin");
                String country_codes = breedObject.getString("country_codes");
                String country_code = breedObject.getString("country_code");
                String description = breedObject.getString("description");
                String life_span = breedObject.getString("life_span");
                String indoor = breedObject.getString("indoor");
                String lap = breedObject.getString("lap");
                String alt_names = breedObject.getString("alt_names");
                String adaptability = breedObject.getString("adaptability");
                String affection_level = breedObject.getString("affection_level");
                String child_friendly = breedObject.getString("child_friendly");
                String dog_friendly = breedObject.getString("dog_friendly");
                String energy_level = breedObject.getString("energy_level");
                String grooming = breedObject.getString("grooming");
                String health_issues = breedObject.getString("health_issues");
                String intelligence = breedObject.getString("intelligence");
                String shedding_level = breedObject.getString("shedding_level");
                String social_needs = breedObject.getString("social_needs");
                String stranger_friendly = breedObject.getString("stranger_friendly");
                String vocalisation = breedObject.getString("vocalisation");
                String experimental = breedObject.getString("experimental");
                String hairless = breedObject.getString("hairless");
                String natural = breedObject.getString("natural");
                String rare = breedObject.getString("rare");
                String rex = breedObject.getString("rex");
                String suppressed_tail = breedObject.getString("suppressed_tail");
                String short_legs = breedObject.getString("short_legs");
                String wikipedia_url = breedObject.getString("wikipedia_url");
                String hypoallergenic = breedObject.getString("hypoallergenic");
                String reference_image_id = breedObject.getString("reference_image_id");

                BreedWeightDetails weight = new BreedWeightDetails(imperial, metric);
                BreedDetails breed = new BreedDetails(weight, id, name, vetstreet_url, temperament, origin, country_codes,
                        country_code, description, life_span, indoor, alt_names, adaptability, affection_level,
                        child_friendly, dog_friendly, energy_level, grooming, health_issues, intelligence, shedding_level,
                        social_needs, stranger_friendly, vocalisation, experimental, hairless, natural, rare, rex,
                        suppressed_tail, short_legs, wikipedia_url, hypoallergenic, reference_image_id);
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
