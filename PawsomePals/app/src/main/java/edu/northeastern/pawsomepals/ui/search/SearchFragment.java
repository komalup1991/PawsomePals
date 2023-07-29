package edu.northeastern.pawsomepals.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

import edu.northeastern.pawsomepals.adapters.SearchRecipeAdapter;
import edu.northeastern.pawsomepals.adapters.SearchUserAdapter;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;

public class SearchFragment extends Fragment {

    ImageButton searchButton;

    EditText searchInput;

    RecyclerView searchRecyclerView;
    List<Recipe> searchRecipeList;

    List<Users> searchUserList;

    SearchRecipeAdapter searchAdapter;

    SearchUserAdapter searchUserAdapter;

    private SearchRecipeAdapter.OnItemActionListener onItemActionListener;

    private SearchUserAdapter.OnItemActionListener OnItemActionListener;


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

        searchAdapter = new SearchRecipeAdapter(new ArrayList<Recipe>(),onItemActionListener);
        searchUserAdapter = new SearchUserAdapter(new ArrayList<Users>(), OnItemActionListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        searchRecyclerView.setLayoutManager(layoutManager);

        Button dogBtn = view.findViewById(R.id.dog_btn);
        Button userBtn = view.findViewById(R.id.user_btn);
        Button recipeBtn = view.findViewById(R.id.recipe_btn);

        dogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                performSearch("dogs");
            }
        });

        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRecyclerView.setAdapter(searchUserAdapter);
                performSearch("users");
            }
        });

        recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRecyclerView.setAdapter(searchAdapter);
                performSearch("recipes");
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String inputSearch = searchInput.getText().toString();
                 FirebaseFirestore db = FirebaseFirestore.getInstance();

                //FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                //        .setQuery(query, Recipe.class).build();

            }

        });
    }
    private void performSearch(String searchType) {
        String inputSearch = searchInput.getText().toString().trim();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query;

        if (searchType.equals("dogs")) {

        } else if (searchType.equals("users")) {

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
        } else if (searchType.equals("recipes")) {

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

                        searchAdapter.setRecipes(searchRecipeList);
                        searchAdapter.notifyDataSetChanged();
                    } else {
                        // Handle the error if the search fails
                    }
                }
            });
        }
    }



}
