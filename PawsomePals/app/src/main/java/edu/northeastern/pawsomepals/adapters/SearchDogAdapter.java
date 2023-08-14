package edu.northeastern.pawsomepals.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Dogs;

import edu.northeastern.pawsomepals.ui.search.SearchViewHolder;

public class SearchDogAdapter extends RecyclerView.Adapter<SearchViewHolder>{

    private final OnItemActionListener onItemActionListener;
    private List<Dogs> dogs;

    public interface OnItemActionListener {
        void onDogsClick(Dogs dog);
    }

    public SearchDogAdapter(ArrayList<Dogs> dogs, SearchDogAdapter.OnItemActionListener onItemActionListener) {
        this.dogs = dogs;
        this.onItemActionListener = onItemActionListener;
    }
    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_recipe, parent, false);
        return new SearchViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Dogs dog = dogs.get(position);
        holder.title.setText(dog.getName());

        Glide.with(holder.itemView.getContext()).
                load(dog.getProfileImage()).
                into(holder.searchImage);


        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onItemActionListener.onDogsClick(dogs.get(position));
            }
        });

        holder.searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onDogsClick(dogs.get(position));
            }
        });

    }

    public void setDogs(List<Dogs> dogList) {
        dogs = dogList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dogs.size();
    }

    public void clearData() {
        dogs.clear();
        notifyDataSetChanged();
    }

}
