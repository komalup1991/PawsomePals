package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.ui.profile.EditDogUserActivity;

public class ProfileDogAdapter extends RecyclerView.Adapter<ProfileDogAdapter.DogProfileViewHolder> {
    private List<Dogs> dogProfiles;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private Boolean isUserProfile;
    private static final int VIEW_TYPE_DOG_PROFILE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;
    public ProfileDogAdapter(List<Dogs> dogProfiles, Context context, Boolean isUserProfile) {
        this.dogProfiles = dogProfiles;
        this.context = context;
        this.isUserProfile = isUserProfile;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public List<Dogs> getDogProfiles() {
        return dogProfiles;
    }

    @NonNull
    @Override
    public DogProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_dogs_items_row, parent, false);
        return new DogProfileViewHolder(view);
    }
    private void deleteDogProfileFromFirebase(int position) {
        // Get the Dog object to be deleted
        Dogs dogProfileToDelete = dogProfiles.get(position);
        String dogProfileIdToDelete = dogProfileToDelete.getDogId();

        firebaseFirestore.collection("dogs").document(dogProfileIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Dog profile deleted successfully
                    Toast.makeText(context, "Dog profile deleted successfully.", Toast.LENGTH_SHORT).show();
                    // You can also remove the dog profile from the local list to update the RecyclerView

                })
                .addOnFailureListener(e -> {
                    // Failed to delete dog profile
                    Toast.makeText(context, "Failed to delete dog profile.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBindViewHolder(@NonNull DogProfileViewHolder holder, int position) {
        Dogs dogProfile = dogProfiles.get(position);

        if (dogProfiles.isEmpty()) {
            holder.hideViews();
        } else {
            holder.showViews();
            try {
                holder.bind(dogProfile);

                if (isUserProfile) {
                    holder.editButton.setOnClickListener(v -> {
                        Intent intent = new Intent(context, EditDogUserActivity.class);
                        intent.putExtra("dogId", dogProfile.getDogId());
                        context.startActivity(intent);

                    });

                    holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            deleteDogProfileFromFirebase(position);
                            dogProfiles.remove(position);
                            notifyItemRemoved(position);
                        }
                    });
                }


                holder.layoutDogInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(holder.expandableLayout.getVisibility() == View.GONE)
                        {
                            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                            holder.expandableLayout.setVisibility(View.VISIBLE);
                        }
                        else {
                            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                            holder.expandableLayout.setVisibility(View.GONE);
                        }
                    }
                });

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dogProfiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return dogProfiles.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_DOG_PROFILE;
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView emptyTextView;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyTextView = itemView.findViewById(R.id.emptyTextView);
        }
    }

    public void setDogProfiles(List<Dogs> dogProfiles) {
        this.dogProfiles = dogProfiles;

    }

    public class DogProfileViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageDog;
        private TextView nameTextView;
        private TextView breedTextView;
        private ImageButton editButton;
        private ImageButton deleteButton;
        private LinearLayout layoutDogInfo;
        private ConstraintLayout expandableLayout;
        private TextView textDogDobValue;
        private TextView textDogGenderValue;
        private TextView textDogSizeValue;
        private CardView cardView;
        public DogProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            imageDog = itemView.findViewById(R.id.imageDog);
            nameTextView = itemView.findViewById(R.id.textDogName);
            breedTextView = itemView.findViewById(R.id.textDogBreed);

            editButton = itemView.findViewById(R.id.btnEdit);
            deleteButton = itemView.findViewById(R.id.btnDelete);
            layoutDogInfo = itemView.findViewById(R.id.layoutDogInfo);

            textDogDobValue = itemView.findViewById(R.id.textDogDobValue);
            textDogGenderValue = itemView.findViewById(R.id.textDogGenderValue);
            textDogSizeValue = itemView.findViewById(R.id.textDogSizeValue);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Dogs dogProfile) throws ParseException {

            nameTextView.setText(dogProfile.getName());

            String breed = "";
            if (dogProfile.getIsMixedBreed()) {
                breed = (new StringBuilder()).append(dogProfile.getBreed()).append(" + ").append(dogProfile.getMixedBreed()).toString();
            } else {
                breed = dogProfile.getBreed();
            }
            breedTextView.setText(breed);

            String imageUrl = dogProfile.getProfileImage();

            if (!imageUrl.equals("") && !imageUrl.equals("null")) {
                Glide.with(context)
                        .load(imageUrl)
                        .into(imageDog);
            } else {
                Glide.with(context)
                        .load(R.drawable.dog)
                        .into(imageDog);
            }

            if (isUserProfile) {
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
            textDogDobValue.setText(dogProfile.getDob());
            textDogGenderValue.setText(dogProfile.getGender());
            textDogSizeValue.setText(dogProfile.getSize());

        }

        private void hideViews() {
            // Hide views when there are no dog profiles
            imageDog.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
            breedTextView.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            expandableLayout.setVisibility(View.GONE);
        }

        private void showViews() {
            // Show views when dog profiles are available
            imageDog.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
            breedTextView.setVisibility(View.VISIBLE);

            if (isUserProfile) {
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
        }

    }


}
