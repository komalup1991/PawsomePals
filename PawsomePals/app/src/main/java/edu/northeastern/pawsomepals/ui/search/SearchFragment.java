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
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Query;
import com.google.firebase.ktx.Firebase;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil;

public class SearchFragment extends Fragment {

    ImageButton searchButton;

    EditText searchInput;

    RecyclerView searchRecyclerView;


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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String inputSearch = searchInput.getText().toString();
                Log.i("inputSearch",inputSearch);
                Query query = ChatFirebaseUtil.allRecipeCollectionReference()
                        .whereEqualTo("title",inputSearch);

                FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                        .setQuery(query, Recipe.class).build();

                Log.i("op", options.getSnapshots().toString());
            }
        });
    }


}
