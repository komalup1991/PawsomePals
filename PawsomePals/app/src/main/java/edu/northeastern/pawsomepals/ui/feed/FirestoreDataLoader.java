package edu.northeastern.pawsomepals.ui.feed;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreDataLoader {

    private FirestoreDataListener listener;
    private FirebaseFirestore db;
    private List<CollectionReference> collections;
    private String orderByField;

    public FirestoreDataLoader(FirestoreDataListener listener, List<CollectionReference> collections,String orderByField) {
        this.listener = listener;
        this.collections = collections;
        db = FirebaseFirestore.getInstance();
        this.orderByField = orderByField;
    }

    public void loadDataFromCollections() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<QuerySnapshot> querySnapshots = new ArrayList<>();
                try {
                    for (CollectionReference collection : collections) {
                        Query query = collection.orderBy(orderByField);
                        Task<QuerySnapshot> task = query.get();
                        Tasks.await(task);
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                querySnapshots.add(querySnapshot);
                            }
                        } else {
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listener != null) {
                    listener.onDataLoaded(querySnapshots);
                }
            }
        }).start();
    }

    public interface FirestoreDataListener {
        void onDataLoaded(List<QuerySnapshot> querySnapshots);
    }
}
