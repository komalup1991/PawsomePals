package edu.northeastern.pawsomepals.ui.chat;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.RecentChatRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;

public class ChatFragment extends Fragment {

    private Users currentUser;
    private RecyclerView chatRecyclerview;
    private RecentChatRecyclerAdapter adapter;

    public interface ProfilePicClickListener {
        void onItemClicked(String userId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView searchInput = view.findViewById(R.id.chat_search_chat);
        chatRecyclerview = view.findViewById(R.id.chat_search_user_recyclerView);
        Button createNewGroupButton = view.findViewById(R.id.new_group_chat_btn);

        ChatFirebaseUtil.currentUserDetails().get().addOnSuccessListener(snapshot -> currentUser = snapshot.toObject(Users.class));
        searchInput.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), ChatSearchChatroomActivity.class);
            startActivity(intent);
        });
        createNewGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateNewGroupChat.class);
            startActivity(intent);
        });

        getFCMToken();
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                ChatFirebaseUtil.currentUserDetails().update("fcmToken", token);
                setupRecyclerView();
            }
        });
    }

    private void setupRecyclerView() {
        Query query = ChatFirebaseUtil.allChatRoomCollectionReference()
                .whereArrayContains("userIds", ChatFirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<ChatRoomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                        .setQuery(query, ChatRoomModel.class)
                        .build();
        adapter = new RecentChatRecyclerAdapter(options, getContext(), userId -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ProfileId", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("profileId", userId);
            editor.apply();

            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_view, profileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }, () -> {
        });
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
