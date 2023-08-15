package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.BreedDetails;

public class ProfileAllDogBreedsAdapter extends RecyclerView.Adapter<ProfileAllDogBreedsAdapter.ProfileAllDogBreedsViewHolder> {
    private List<BreedDetails> breedDetails;
    private final Context context;
    private static final int VIEW_TYPE_DOG_BREED_DETAILS = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    public ProfileAllDogBreedsAdapter(List<BreedDetails> breedDetails, Context context) {
        this.breedDetails = breedDetails;
        this.context = context;
    }

    public void setFilteredList(List<BreedDetails> filteredList) {
        this.breedDetails = filteredList;
        notifyDataSetChanged();
    }

    public List<BreedDetails> getBreedDetails() {
        return breedDetails;
    }

    @NonNull
    @Override
    public ProfileAllDogBreedsAdapter.ProfileAllDogBreedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_dog_breed_item_row, parent, false);
        return new ProfileAllDogBreedsAdapter.ProfileAllDogBreedsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAllDogBreedsAdapter.ProfileAllDogBreedsViewHolder holder, int position) {
        BreedDetails breedDetail = breedDetails.get(position);


        try {
            holder.bind(breedDetail);

            boolean isExpandable = breedDetail.getExpandable();
            holder.breedDescription.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isAnyItemExpanded(position);
                    breedDetail.setExpandable(!breedDetail.getExpandable());
                    notifyItemChanged(position);
                }
            });

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        setAnimation(holder.itemView, position);
    }

    private void isAnyItemExpanded(int position) {
        int temp = -1;
        for (int i = 0; i < breedDetails.size(); i++) {
            if (breedDetails.get(i).getExpandable()) {
                temp = i;
                break;
            }
        }
        if (temp >= 0 && temp != position) {
            breedDetails.get(temp).setExpandable(false);
            notifyItemChanged(temp);
        }
    }

    @Override
    public int getItemCount() {
        return breedDetails.size();
    }

    @Override
    public int getItemViewType(int position) {
        return breedDetails.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_DOG_BREED_DETAILS;
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView emptyTextView;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyTextView = itemView.findViewById(R.id.emptyTextView);
        }
    }

    public void setBreedDetails(List<BreedDetails> breedDetails) {
        this.breedDetails = breedDetails;

    }

    private void setAnimation(View viewToAnimate, int position) {
        Animation slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        viewToAnimate.startAnimation(slideIn);
    }

    public class ProfileAllDogBreedsViewHolder extends RecyclerView.ViewHolder {
        private final ImageView dogImage;
        private final TextView breedName;
        private final TextView breedDescription;
        private final ConstraintLayout constraintLayout;
        private final MaterialCardView materialCardView;

        public ProfileAllDogBreedsViewHolder(@NonNull View itemView) {
            super(itemView);

            dogImage = itemView.findViewById(R.id.dogImage);
            breedName = itemView.findViewById(R.id.breedName);
            breedDescription = itemView.findViewById(R.id.breedDesc);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            materialCardView = itemView.findViewById(R.id.materialCardView);
        }

        public void bind(BreedDetails breedDetail) throws ParseException {

            breedName.setText(breedDetail.getName());

            StringBuilder imageUrl = null;
            if(!breedDetail.getReference_image_id().equals(""))
            {
                imageUrl = new StringBuilder();
                imageUrl.append("https://cdn2.thedogapi.com/images/");
                imageUrl.append(breedDetail.getReference_image_id());
                imageUrl.append(".jpg");
            }

            //String imageUrl = breedDetail.getImage().getUrl();

            if (!Objects.isNull(imageUrl)) {
                if (!imageUrl.equals("") && !imageUrl.equals("null")) {
                    Glide.with(context)
                            .load(imageUrl.toString())
                            .into(dogImage);
                } else {
                    Glide.with(context)
                            .load(R.drawable.dog)
                            .into(dogImage);
                }
            } else {
                Glide.with(context)
                        .load(R.drawable.dog)
                        .into(dogImage);
            }

            StringBuilder breedDesc = new StringBuilder();

            if (!breedDetail.getHeight().getImperial().isEmpty()) {
                breedDesc.append("Height: ");
                breedDesc.append(breedDetail.getHeight().getImperial());
                breedDesc.append(" inches \n \n");
            }

            if (!breedDetail.getWeight().getImperial().isEmpty()) {
                breedDesc.append("Weight: ");
                breedDesc.append(breedDetail.getWeight().getImperial());
                breedDesc.append(" pounds \n \n");
            }

            if (!breedDetail.getLife_span().isEmpty()) {
                breedDesc.append("Life Expectancy: ");
                breedDesc.append(breedDetail.getLife_span());
                breedDesc.append(" \n \n");
            }

            if (!breedDetail.getBred_for().isEmpty()) {
                breedDesc.append("Breed For: ");
                breedDesc.append(breedDetail.getBred_for());
                breedDesc.append(" \n \n");
            }

            if (!breedDetail.getBreed_group().isEmpty()) {
                breedDesc.append("Breed Group: ");
                breedDesc.append(breedDetail.getBreed_group());
            }

            breedDescription.setText(breedDesc);
        }

        public void collapseExpandedView() {
            breedDescription.setVisibility(View.GONE);
        }
    }
}
