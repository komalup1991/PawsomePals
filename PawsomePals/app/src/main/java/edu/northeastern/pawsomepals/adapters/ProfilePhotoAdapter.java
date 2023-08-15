package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.PhotoVideo;

public class ProfilePhotoAdapter extends RecyclerView.Adapter<ProfilePhotoAdapter.ProfilePhotoViewHolder> {
    private List<PhotoVideo> photos;
    private final Context context;
    private final FirebaseFirestore firebaseFirestore;
    private Boolean isUserProfile;
    private static final int VIEW_TYPE_PHOTO_PROFILE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    public ProfilePhotoAdapter(List<PhotoVideo> photos, Context context) {
        this.photos = photos;
        this.context = context;
        this.isUserProfile = isUserProfile;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public List<PhotoVideo> getPhotos() {
        return photos;
    }

    @NonNull
    @Override
    public ProfilePhotoAdapter.ProfilePhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_photos_items_row, parent, false);
        return new ProfilePhotoAdapter.ProfilePhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePhotoAdapter.ProfilePhotoViewHolder holder, int position) {
        PhotoVideo photo = photos.get(position);

        if (photos.isEmpty()) {
            holder.hideViews();
        } else {
            holder.showViews();
            try {
                holder.bind(photo);

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Need to add in future
                    }
                });

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return photos.isEmpty() ? VIEW_TYPE_EMPTY : VIEW_TYPE_PHOTO_PROFILE;
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView emptyTextView;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            emptyTextView = itemView.findViewById(R.id.emptyTextView);
        }
    }

    public void setPhotos(List<PhotoVideo> photos) {
        this.photos = photos;

    }

    public class ProfilePhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imagePhoto;
        private final TextView nameTextView;

        private final CardView cardView;

        public ProfilePhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.imageRecipe);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(PhotoVideo photo) throws ParseException {

            nameTextView.setText(photo.getCaption());

            String imageUrl = photo.getImg();

            if (!Objects.isNull(imageUrl)) {
                if (!imageUrl.equals("") && !imageUrl.equals("null")) {
                    Glide.with(context)
                            .load(imageUrl)
                            .into(imagePhoto);
                } else {
                    Glide.with(context)
                            .load(R.drawable.dog)
                            .into(imagePhoto);
                }
            } else {
                Glide.with(context)
                        .load(R.drawable.dog)
                        .into(imagePhoto);
            }
        }

        private void hideViews() {
            imagePhoto.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
        }

        private void showViews() {
            // Show views when dog profiles are available
            imagePhoto.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
        }

    }


}