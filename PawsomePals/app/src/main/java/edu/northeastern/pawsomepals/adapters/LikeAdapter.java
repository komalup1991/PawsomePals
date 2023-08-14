package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Like;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.feed.viewHolder.LikeViewHolder;
import edu.northeastern.pawsomepals.utils.BaseDataCallback;
import edu.northeastern.pawsomepals.utils.FirebaseUtil;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class LikeAdapter extends RecyclerView.Adapter<LikeViewHolder> {
    private final Context context;
    private final List<Like> likeList;
    private final String postId;
    private FirebaseUser firebaseUser;
    private final OnItemActionListener onItemActionListener;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    private Set<String> followListSet = new HashSet<>();

    public LikeAdapter(Context context, List<Like> likeList, String postId, OnItemActionListener onItemActionListener) {
        this.context = context;
        this.likeList = likeList;
        this.postId = postId;
        this.onItemActionListener = onItemActionListener;
    }

    @NonNull
    @Override
    public LikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.like_item, parent, false);
        return new LikeViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull LikeViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Like like = likeList.get(position);
        setupFollowButton(like.getCreatedBy(), holder);
        FirebaseUtil.fetchUserInfoFromFirestore(like.getCreatedBy(), new BaseDataCallback() {
            @Override
            public void onUserReceived(Users user) {
                Glide.with(context).load(user.getProfileImage()).into(holder.image_profile);
                holder.username.setText(user.getName());
                holder.createdAtTextView.setText(like.getCreatedAt());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemActionListener != null) {
                    onItemActionListener.onUserClick(like.getCreatedBy());
                }
            }
        });

        holder.followFollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonText = holder.followFollowingButton.getText().toString();
                if (buttonText.equals("Follow")) {
                    holder.followFollowingButton.setText("Following");
                    followProfile(like.getCreatedBy(),holder);
                } else if (buttonText.equals("Following")) {
                    unfollowProfile(like.getCreatedBy(),holder);
                }
            }
        });
    }

    private void setupFollowButton(String profileId, LikeViewHolder holder) {
        holder.followFollowingButton.setVisibility(View.VISIBLE);
        if (followListSet != null && followListSet.contains(profileId)) {
            holder.followFollowingButton.setText("Following");
        } else {
            if (Objects.equals(profileId, FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.followFollowingButton.setVisibility(View.GONE);
            } else {
                holder.followFollowingButton.setText("Follow");
            }
        }
    }


    private void followProfile(String profileId, LikeViewHolder holder) {
        firebaseFirestore=FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Check if the document for the current user exists in the "follow" collection
        firebaseFirestore.collection("follow").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Document exists, update the "following" field
                firebaseFirestore.collection("follow").document(userId).update("following", FieldValue.arrayUnion(profileId)).addOnSuccessListener(aVoid -> {
                    // Check if the document for the profileId exists in the "follow" collection
                    firebaseFirestore.collection("follow").document(profileId).get().addOnSuccessListener(profileDocumentSnapshot -> {
                        if (profileDocumentSnapshot.exists()) {
                            // Document exists, update the "followers" field
                            List<String> followersList = (List<String>) profileDocumentSnapshot.get("followers");
                            if (followersList != null && !followersList.contains(userId)) {
                                followersList.add(userId);
                                firebaseFirestore.collection("follow").document(profileId).update("followers", followersList).addOnSuccessListener(aVoid1 -> {
                                    // Update the UI and change the button text to "Following"
                                    holder.followFollowingButton.setText("Following");
                                }).addOnFailureListener(e -> {
                                    Log.w("Follow Profile", "Error updating followers", e);
                                });
                            } else {
                                // The current user is already in the followers list
                            }
                        } else {
                            // Document does not exist, create it and set the "followers" field
                            Map<String, Object> profileData = new HashMap<>();
                            profileData.put("followers", Collections.singletonList(userId));
                            firebaseFirestore.collection("follow").document(profileId).set(profileData).addOnSuccessListener(aVoid1 -> {
                                // Update the UI and change the button text to "Following"
                                holder.followFollowingButton.setText("Following");
                            }).addOnFailureListener(e -> {
                                Log.w("Follow Profile", "Error creating profile document", e);
                            });
                        }
                    }).addOnFailureListener(e -> {
                        Log.w("Follow Profile", "Error getting profile document", e);
                    });
                }).addOnFailureListener(e -> {
                    Log.w("Follow Profile", "Error updating following", e);
                });
            } else {
                // Document does not exist, create it and set the "following" field
                Map<String, Object> data = new HashMap<>();
                data.put("following", Collections.singletonList(profileId));
                firebaseFirestore.collection("follow").document(userId).set(data).addOnSuccessListener(aVoid -> {
                    // Check if the document for the profileId exists in the "follow" collection
                    firebaseFirestore.collection("follow").document(profileId).get().addOnSuccessListener(profileDocumentSnapshot -> {
                        if (profileDocumentSnapshot.exists()) {
                            // Document exists, update the "followers" field
                            List<String> followersList = (List<String>) profileDocumentSnapshot.get("followers");
                            if (followersList != null && !followersList.contains(userId)) {
                                followersList.add(userId);
                                firebaseFirestore.collection("follow").document(profileId).update("followers", followersList).addOnSuccessListener(aVoid1 -> {
                                    // Update the UI and change the button text to "Following"
                                    holder.followFollowingButton.setText("Following");
                                }).addOnFailureListener(e -> {
                                    Log.w("Follow Profile", "Error updating followers", e);
                                });
                            } else {
                                // The current user is already in the followers list
                            }
                        } else {
                            // Document does not exist, create it and set the "followers" field
                            Map<String, Object> profileData = new HashMap<>();
                            profileData.put("followers", Collections.singletonList(userId));
                            firebaseFirestore.collection("follow").document(profileId).set(profileData).addOnSuccessListener(aVoid1 -> {
                                // Update the UI and change the button text to "Following"
                                holder.followFollowingButton.setText("Following");
                            }).addOnFailureListener(e -> {
                                Log.w("Follow Profile", "Error creating profile document", e);
                            });
                        }
                    }).addOnFailureListener(e -> {
                        Log.w("Follow Profile", "Error getting profile document", e);
                    });
                }).addOnFailureListener(e -> {
                    Log.w("Follow Profile", "Error updating following", e);
                });
            }
        }).addOnFailureListener(e -> {
            Log.w("Follow Profile", "Error checking if document exists", e);
        });
    }


    private void unfollowProfile(String profileId, LikeViewHolder holder) {
        firebaseFirestore=FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Check if the document for the current user exists in the "follow" collection
        firebaseFirestore.collection("follow").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Document exists, update the "following" field
                firebaseFirestore.collection("follow").document(userId).update("following", FieldValue.arrayRemove(profileId)).addOnSuccessListener(aVoid -> {
                    // Check if the document for the profileId exists in the "follow" collection
                    firebaseFirestore.collection("follow").document(profileId).get().addOnSuccessListener(profileDocumentSnapshot -> {
                        if (profileDocumentSnapshot.exists()) {
                            // Document exists, update the "followers" field
                            firebaseFirestore.collection("follow").document(profileId).update("followers", FieldValue.arrayRemove(userId)).addOnSuccessListener(aVoid1 -> {
                                // Update the UI and change the button text to "Follow"
                                holder.followFollowingButton.setText("Follow");
                            }).addOnFailureListener(e -> {
                                Log.w("Unfollow Profile", "Error updating followers", e);
                            });
                        } else {
                            // Document does not exist, log an error (should not happen)
                            Log.e("Unfollow Profile", "Profile document does not exist");
                        }
                    }).addOnFailureListener(e -> {
                        Log.w("Unfollow Profile", "Error getting profile document", e);
                    });
                }).addOnFailureListener(e -> {
                    Log.w("Unfollow Profile", "Error updating following", e);
                });
            } else {
                // Document does not exist, log an error (should not happen)
                Log.e("Unfollow Profile", "User document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.w("Unfollow Profile", "Error checking if document exists", e);
        });
    }


    @Override
    public int getItemCount() {
        return likeList.size();
    }


    public void setFollowListSet(Set<String> followListSet) {
        this.followListSet = followListSet;
    }
}
