package edu.northeastern.pawsomepals.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;

import edu.northeastern.pawsomepals.adapters.RecipeAdapter;
import edu.northeastern.pawsomepals.adapters.SearchDogAdapter;
import edu.northeastern.pawsomepals.adapters.SearchRecipeAdapter;
import edu.northeastern.pawsomepals.adapters.SearchUserAdapter;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.RecipeDetailActivity;
import edu.northeastern.pawsomepals.ui.profile.UserProfileActivity;

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

        Button dogBtn = view.findViewById(R.id.dog_btn);
        Button userBtn = view.findViewById(R.id.user_btn);
        Button recipeBtn = view.findViewById(R.id.recipe_btn);

        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.search);
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
                searchInput.setText(selectedQuery); // Set the selected suggestion in the EditText
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
                if (selectedSearchType.isEmpty()) {
                    showToast("Please select a search type");
                    return;
                }

                performSearch(selectedSearchType);
            }
        });

        dogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedSearchType = "dogs";
                searchRecyclerView.setAdapter(searchDogAdapter);

            }
        });

        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedSearchType = "users";
                searchRecyclerView.setAdapter(searchUserAdapter);

            }
        });

        recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedSearchType = "recipes";
                searchRecyclerView.setAdapter(searchRecipeAdapter);

            }
        });

    }
    private void performSearch(String searchType) {
        String inputSearch = searchInput.getText().toString().trim();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query;

        if (selectedSearchType.equals("dogs")) {
            query = db.collection("dogs").whereGreaterThanOrEqualTo("name", inputSearch);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        searchDogList = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Dogs dog1 = documentSnapshot.toObject(Dogs.class);
                            searchDogList.add(dog1);
                        }
                        Log.d("list",searchDogList.get(0).toString());
                        searchDogAdapter.setDogs(searchDogList);
                        searchDogAdapter.notifyDataSetChanged();
                    }
                }
            });

        } else if (selectedSearchType.equals("users")) {

             query = db.collection("user").whereGreaterThanOrEqualTo("name", inputSearch);

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

            query = db.collection("recipes").whereGreaterThanOrEqualTo("title", inputSearch);

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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void initializeItemActionListener() {
        onItemActionListenerDog = new SearchDogAdapter.OnItemActionListener() {
            @Override
            public void onDogsClick(Dogs dogs) {
                Intent intent = new Intent(getActivity(), DogDetailActivity.class);
                intent.putExtra("name", dogs.getName());
                intent.putExtra("image",dogs.getProfileImage());
                startActivity(intent);
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
                    .startAt(input)
                    .endAt(input + "\uf8ff")
                    .limit(10);

        } else if (selectedSearchType.equals("users")) {
            suggestionsQuery = db.collection("user")
                    .orderBy("name")
                    .startAt(input)
                    .endAt(input + "\uf8ff")
                    .limit(10);

        } else if (selectedSearchType.equals("recipes")) {
            suggestionsQuery = db.collection("recipes")
                    .orderBy("title")
                    .startAt(input)
                    .endAt(input + "\uf8ff")
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




}
