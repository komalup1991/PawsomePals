package edu.northeastern.pawsomepals.ui.feed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.databinding.FragmentFeedCreateListDialogListDialogBinding;
import edu.northeastern.pawsomepals.databinding.FragmentFeedCreateListDialogListDialogItemBinding;

public class FeedCreateListDialogFragment extends BottomSheetDialogFragment {

    private static final int FEED_CREATION_REQUEST = 1;
    private FragmentFeedCreateListDialogListDialogBinding binding;
    private ItemAdapter itemAdapter;

    public static FeedCreateListDialogFragment newInstance() {
        return new FeedCreateListDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedCreateListDialogListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView =  (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemAdapter = new ItemAdapter(getActivity());
        recyclerView.setAdapter(itemAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setOnItemClickListener(ItemAdapter.OnItemClickListener listener) {
        if (itemAdapter != null) {
            itemAdapter.setOnItemClickListener(listener);
        }
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

    class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {
        private Activity activity;
        private final String[] actions = {"Recipe", "Post", "Photo", "Services", "Events"};
        private final int[] icons = {R.drawable.dogbowl, R.drawable.socialmedia,
                R.drawable.media, R.drawable.groomingcolor, R.drawable.planner};
        private OnItemClickListener listener;

        ItemAdapter(Activity activity) {
            this.activity = activity;
        }

        interface OnItemClickListener {
            void onItemClick(int position);
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            FragmentFeedCreateListDialogListDialogItemBinding binding = FragmentFeedCreateListDialogListDialogItemBinding.inflate(inflater, parent, false);
            View view = binding.getRoot();
            ImageView icon = view.findViewById(R.id.icon);
            return new ViewHolder(binding, icon);
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
                        case "Photo":
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
                    activity.startActivityForResult(intent, FEED_CREATION_REQUEST);
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
