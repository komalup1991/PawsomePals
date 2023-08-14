package edu.northeastern.pawsomepals.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.search.SearchViewHolder;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchViewHolder>{

    private final SearchUserAdapter.OnItemActionListener onItemActionListener;

    public interface OnItemActionListener {
        void onUserClick(Users user);
    }
    private List<Users> users;

    public SearchUserAdapter(List<Users> users, SearchUserAdapter.OnItemActionListener onItemActionListener) {
        this.users = users;
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

        Users user = users.get(position);
        holder.title.setText(user.getName());

        Glide.with(holder.itemView.getContext()).
                load(user.getProfileImage()).
                into(holder.searchImage);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(users.get(position));
            }
        });


    }

    public void setUsers(List<Users> userList) {
        users = userList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public void clearData() {
        users.clear();
        notifyDataSetChanged();
    }
}
