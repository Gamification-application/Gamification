package com.example.employee_gamification;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourpackage.name.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EMNotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final List<NotificationModel> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private final String userId = "03";  // Modify as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Ensure activity_emnotification.xml exists in res/layout
        setContentView(R.layout.activity_emnotification);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchNotifications();
    }

    private void fetchNotifications() {
        db.collection("notifications")
                .whereEqualTo("seen", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) {
                        return;
                    }

                    notificationList.clear();

                    for (QueryDocumentSnapshot document : value) {
                        NotificationModel notification = new NotificationModel();
                        notification.setId(document.getId());
                        notification.setMessage(document.getString("message"));

                        Timestamp createdAtTimestamp = document.getTimestamp("createdAt");
                        if (createdAtTimestamp != null) {
                            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(createdAtTimestamp.toDate());
                            notification.setCreatedAt(formattedDate);
                        }

                        Timestamp deadlineTimestamp = document.getTimestamp("deadline");
                        if (deadlineTimestamp != null) {
                            String formattedDeadline = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(deadlineTimestamp.toDate());
                            notification.setDeadline(formattedDeadline);
                        }

                        Boolean seenValue = document.getBoolean("seen");
                        notification.setSeen(seenValue != null && seenValue);

                        notificationList.add(notification);
                    }

                  adapter.notifyDataSetChanged();
                });
    }
}
