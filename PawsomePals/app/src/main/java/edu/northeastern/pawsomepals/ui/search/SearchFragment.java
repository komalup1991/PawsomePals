package edu.northeastern.pawsomepals.ui.search;


import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.northeastern.pawsomepals.R;

import edu.northeastern.pawsomepals.adapters.SearchDogAdapter;
import edu.northeastern.pawsomepals.adapters.SearchRecipeAdapter;
import edu.northeastern.pawsomepals.adapters.SearchUserAdapter;
import edu.northeastern.pawsomepals.models.BreedDetails;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.network.BaseUiThreadCallback;
import edu.northeastern.pawsomepals.network.PawsomePalWebService;
import edu.northeastern.pawsomepals.ui.feed.RecipeDetailActivity;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;


public class SearchFragment extends Fragment {

    ImageButton searchButton;

    EditText searchInput;

    RecyclerView searchRecyclerView;
    List<Recipe> searchRecipeList;

    List<Users> searchUserList;

    List<Dogs> searchDogList;

    SearchRecipeAdapter searchRecipeAdapter;

    SearchUserAdapter searchUserAdapter;

    SearchDogAdapter searchDogAdapter;

    private ArrayAdapter<String> autoCompleteAdapter;

    private String selectedSearchType = "";

    private SearchRecipeAdapter.OnItemActionListener onItemActionListenerRecipe;

    private SearchUserAdapter.OnItemActionListener onItemActionListenerUser;

    private SearchDogAdapter.OnItemActionListener onItemActionListenerDog;

    private PawsomePalWebService pawsomePalWebService;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchButton = view.findViewById(R.id.dog_search_btn);

        searchInput = view.findViewById(R.id.search);

        searchRecyclerView = view.findViewById(R.id.search_recycler_view);
        initializeItemActionListener();
        searchRecipeAdapter = new SearchRecipeAdapter(new ArrayList<Recipe>(), onItemActionListenerRecipe);
        searchUserAdapter = new SearchUserAdapter(new ArrayList<Users>(), onItemActionListenerUser);
        searchDogAdapter = new SearchDogAdapter(new ArrayList<Dogs>(), onItemActionListenerDog);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        searchRecyclerView.setLayoutManager(layoutManager);

        CardView cardView = view.findViewById(R.id.golden_retriever);

        Button dogBtn = view.findViewById(R.id.dog_btn);
        Button userBtn = view.findViewById(R.id.user_btn);
        Button recipeBtn = view.findViewById(R.id.recipe_btn);
        Button historyBtn = view.findViewById(R.id.history_button);

        selectedSearchType = "dogs";
        searchRecyclerView.setAdapter(searchDogAdapter);
        dogBtn.setBackgroundColor(getResources().getColor(R.color.white));
        dogBtn.setTextColor(getResources().getColor(R.color.colorSecondary));


        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.search);
        autoCompleteTextView.requestFocus();

        autoCompleteAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        autoCompleteTextView.setAdapter(autoCompleteAdapter);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuery = autoCompleteAdapter.getItem(position);
                searchInput.setText(selectedQuery);
                cardView.setVisibility(View.GONE);
                performSearch(selectedSearchType);
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = charSequence.toString().trim();
                if (!input.isEmpty()) {
                    fetchAutocompleteSuggestions(input);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardView.setVisibility(View.GONE);

                performSearch(selectedSearchType);
                saveSearchHistory(searchInput.getText().toString());
            }
        });

        dogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dogBtn.setBackgroundColor(getResources().getColor(R.color.white));
                userBtn.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                recipeBtn.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                dogBtn.setTextColor(getResources().getColor(R.color.colorSecondary));
                userBtn.setTextColor(getResources().getColor(R.color.white));
                recipeBtn.setTextColor(getResources().getColor(R.color.white));
                selectedSearchType = "dogs";
                searchDogAdapter.clearData();;
                searchRecyclerView.setAdapter(searchDogAdapter);

                dogBtn.setSelected(true);
                userBtn.setSelected(false);
                recipeBtn.setSelected(false);

            }
        });

        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dogBtn.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                userBtn.setBackgroundColor(getResources().getColor(R.color.white));
                recipeBtn.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                userBtn.setTextColor(getResources().getColor(R.color.colorSecondary));
                dogBtn.setTextColor(getResources().getColor(R.color.white));
                recipeBtn.setTextColor(getResources().getColor(R.color.white));
                selectedSearchType = "users";
                searchUserAdapter.clearData();
                searchRecyclerView.setAdapter(searchUserAdapter);


            }
        });

        recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dogBtn.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                userBtn.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                recipeBtn.setBackgroundColor(getResources().getColor(R.color.white));
                recipeBtn.setTextColor(getResources().getColor(R.color.colorSecondary));
                dogBtn.setTextColor(getResources().getColor(R.color.white));
                userBtn.setTextColor(getResources().getColor(R.color.white));
                selectedSearchType = "recipes";
                searchRecipeAdapter.clearData();
                searchRecyclerView.setAdapter(searchRecipeAdapter);

            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<String> searchHistory = getSearchHistory();
                if (!searchHistory.isEmpty()) {
                    showSearchHistoryDialog(searchHistory);
                } else {
                    Toast.makeText(requireContext(), "No search history available", Toast.LENGTH_SHORT).show();
                }

            }
        });

        pawsomePalWebService = new PawsomePalWebService(uiThreadCallback);

        pawsomePalWebService.getBreedsDetails();


    }

    PawsomePalWebService.UiThreadCallback uiThreadCallback = new BaseUiThreadCallback() {

        public void onGetAllBreedsDetails(List<BreedDetails> breeds) {
            if (!isAdded() || !isVisible()) {
                return;
            }
            int random = getRandomNumber(0, breeds.size() - 1);
            Log.d("breeds", breeds.get(0).toString());
            BreedDetails randomBreed = breeds.get(random);
            ImageView breedImage = getView().findViewById(R.id.breed_image);
            TextView breedName = getView().findViewById(R.id.breed_name);

            String imageUrl = randomBreed.getImage().getUrl();
            Log.d("image", imageUrl);

            breedName.setText(randomBreed.getName());
            Glide.with(requireContext())
                    .load(randomBreed.getImage().getUrl())
                    .into(breedImage);


        }


    };


    private void performSearch(String searchType) {
        String inputSearch = searchInput.getText().toString().trim();
        if (inputSearch.isEmpty()) {
            showToast("Please enter a valid search");
        }
        else{

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query;


        if (selectedSearchType.equals("dogs")) {

            query = db.collection("dogs")
                    .orderBy("name")
                    .startAt(inputSearch)
                    .endAt(inputSearch.toLowerCase() + "\uf8ff")
                    .limit(10);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        searchDogList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Dogs dog1 = documentSnapshot.toObject(Dogs.class);
                            searchDogList.add(dog1);
                        }

                        searchDogAdapter.setDogs(searchDogList);
                        searchDogAdapter.notifyDataSetChanged();
                    }
                }
            });

        } else if (selectedSearchType.equals("users")) {

            query = db.collection("user")
                    .orderBy("name")
                    .startAt(inputSearch.toUpperCase())
                    .endAt(inputSearch.toLowerCase() + "\uf8ff")
                    .limit(10);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        searchUserList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Users user1 = documentSnapshot.toObject(Users.class);
                            searchUserList.add(user1);
                        }
                        searchUserAdapter.setUsers(searchUserList);
                        searchUserAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else if (selectedSearchType.equals("recipes")) {

            query = db.collection("recipes")
                    .orderBy("title")
                    .startAt(inputSearch.toUpperCase())
                    .endAt(inputSearch.toLowerCase() + "\uf8ff")
                    .limit(10);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        searchRecipeList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Recipe recipe1 = documentSnapshot.toObject(Recipe.class);
                            searchRecipeList.add(recipe1);
                        }

                        searchRecipeAdapter.setRecipes(searchRecipeList);
                        searchRecipeAdapter.notifyDataSetChanged();
                    } else {
                        // Handle the error if the search fails
                    }
                }
            });
        }
    }

}

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void initializeItemActionListener() {
        onItemActionListenerDog = new SearchDogAdapter.OnItemActionListener() {
            @Override
            public void onDogsClick(Dogs dogs) {
                Intent intent = new Intent(requireContext(), DogDetailActivity.class);
                intent.putExtra("dogs", dogs);
                intent.putExtra("name", dogs.getName());
                intent.putExtra("image",dogs.getProfileImage());
                startActivity(intent);
            }
        };

        onItemActionListenerRecipe = new SearchRecipeAdapter.OnItemActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe", recipe);
                startActivity(intent);

            }
        };

        onItemActionListenerUser = new SearchUserAdapter.OnItemActionListener() {
            @Override
            public void onUserClick(Users user) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ProfileId", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileId", user.getUserId());
                editor.apply();

                //Navigate to Profile Fragment
                ProfileFragment profileFragment = new ProfileFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container_view, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        };
    }

    private void fetchAutocompleteSuggestions(String input) {
        if (selectedSearchType.isEmpty()) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query suggestionsQuery;

        if (selectedSearchType.equals("dogs")) {
            suggestionsQuery = db.collection("dogs")
                    .orderBy("name")
                    .startAt(input.toUpperCase())
                    .endAt(input.toLowerCase() + "\uf8ff")
                    .limit(10);

        } else if (selectedSearchType.equals("users")) {
            suggestionsQuery = db.collection("user")
                    .orderBy("name")
                    .startAt(input.toUpperCase())
                    .endAt(input.toLowerCase() + "\uf8ff")
                    .limit(10);

        } else if (selectedSearchType.equals("recipes")) {
            suggestionsQuery = db.collection("recipes")
                    .orderBy("title")
                    .startAt(input.toUpperCase())
                    .endAt(input.toLowerCase() + "\uf8ff")
                    .limit(10);

        } else {
            return;
        }

        suggestionsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> suggestions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (selectedSearchType.equals("dogs")) {
                            String dogName = document.getString("name");
                            suggestions.add(dogName);
                        } else if (selectedSearchType.equals("users")) {
                            String userName = document.getString("name");
                            suggestions.add(userName);
                        } else if (selectedSearchType.equals("recipes")) {
                            String recipeTitle = document.getString("title");
                            suggestions.add(recipeTitle);
                        }
                    }
                    autoCompleteAdapter.clear();
                    autoCompleteAdapter.addAll(suggestions);
                    autoCompleteAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void saveSearchHistory(String searchTerm) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE);
        Set<String> historySet = sharedPreferences.getStringSet("history", new HashSet<>());
        historySet.add(searchTerm);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("history", historySet);
        editor.apply();
    }

    private List<String> getSearchHistory() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE);
        Set<String> historySet = sharedPreferences.getStringSet("history", new HashSet<>());
        return new ArrayList<>(historySet);
    }

    private void showSearchHistoryDialog(List<String> searchHistory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Search History");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, searchHistory);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                String selectedQuery = searchHistory.get(position);
                searchInput.setText(selectedQuery);
                performSearch(selectedSearchType);
            }
        });
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }



}
