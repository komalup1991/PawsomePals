package edu.northeastern.pawsomepals.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.databinding.FragmentFeedCreateListDialogListDialogBinding;
import edu.northeastern.pawsomepals.databinding.FragmentFeedCreateListDialogListDialogItemBinding;

public class FeedCreateListDialogFragment extends BottomSheetDialogFragment {

    private FragmentFeedCreateListDialogListDialogBinding binding;
    private Toolbar toolbar;

    public static FeedCreateListDialogFragment newInstance() {
        return new FeedCreateListDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed_create_list_dialog_list_dialog, container, false);
        binding = FragmentFeedCreateListDialogListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter itemAdapter = new ItemAdapter(getActivity());
        recyclerView.setAdapter(itemAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ImageView icon;

        ViewHolder(FragmentFeedCreateListDialogListDialogItemBinding binding, ImageView icon) {
            super(binding.getRoot());
            text = binding.text;
            this.icon = icon;
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {
        private  Activity activity;
        private final String[] actions = {"Recipe", "Post", "Photo/Video", "Services", "Events"};
        private final int[] icons = {R.drawable.dogbowl, R.drawable.socialmedia,
                R.drawable.media, R.drawable.groomingcolor, R.drawable.planner};
        ItemAdapter(Activity activity) {
            this.activity = activity;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            FragmentFeedCreateListDialogListDialogItemBinding binding = FragmentFeedCreateListDialogListDialogItemBinding.inflate(inflater, parent, false);
            View view = binding.getRoot();
            ImageView icon = view.findViewById(R.id.icon);
            return new ViewHolder(binding,icon);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.text.setText(actions[position]);
            holder.icon.setImageResource(icons[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                String action = actions[holder.getBindingAdapterPosition()];
                Intent intent;
                    switch (action) {
                        case "Recipe":
                            intent = new Intent(activity, CreateRecipeActivity.class);
                            break;
                        case "Post":
                            intent = new Intent(activity, CreatePostActivity.class);
                            break;
                        case "Photo/Video":
                            intent = new Intent(activity, CreatePhotoVideoActivity.class);
                            break;
                        case "Services":
                            intent = new Intent(activity, CreateServicesActivity.class);
                            break;
                        case "Events":
                            intent = new Intent(activity, CreateEventsActivity.class);
                            break;
                        default:
                            return;
                    }
                activity.startActivity(intent);
                FeedCreateListDialogFragment.this.dismiss();

            }
        });
        }

        @Override
        public int getItemCount() {
            return actions.length;
        }

    }
}
