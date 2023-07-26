package edu.northeastern.pawsomepals.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecipeAdapter;
import edu.northeastern.pawsomepals.adapters.SearchRecyclerAdapter;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;

public class SearchFragment extends Fragment {

    ImageButton searchButton;

    EditText searchInput;

    RecyclerView searchRecyclerView;
    List<Recipe> searchRecipeList;

    SearchRecyclerAdapter searchAdapter;

    private SearchRecyclerAdapter.OnItemActionListener onItemActionListener;


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

        searchAdapter = new SearchRecyclerAdapter(new ArrayList<Recipe>(),new ArrayList<Users>(),onItemActionListener);
        searchRecyclerView.setAdapter(searchAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        searchRecyclerView.setLayoutManager(layoutManager);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String inputSearch = searchInput.getText().toString();
                Log.i("inputSearch", inputSearch);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Query query = db.collection("recipes").whereGreaterThanOrEqualTo("title", inputSearch);
                Log.i("q",query.get().toString());

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            searchRecipeList = new ArrayList<>();
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                Recipe recipe1 = documentSnapshot.toObject(Recipe.class);
                                searchRecipeList.add(recipe1);

                            }

                            searchAdapter.setRecipes(searchRecipeList);
                            searchAdapter.notifyDataSetChanged();

                        }else{
                            //
                        }
                    }
                });
                FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                        .setQuery(query, Recipe.class).build();

            }

        });
    }


}
