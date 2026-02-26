package com.example.employee_gamification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.yourpackage.name.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HRSubmissionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HRSubmissionAdapter adapter;
    private List<HRSubmissionModel> submissionList;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrtasksubmission); // Make sure this layout exists

        recyclerView = findViewById(R.id.recyclerView);  // Should match ID in XML
        progressBar = findViewById(R.id.progressBar);    // Should match ID in XML

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        submissionList = new ArrayList<>();
        adapter = new HRSubmissionAdapter(this, submissionList);
        recyclerView.setAdapter(adapter);

        fetchCompletedTasks(); // Make sure this method exists in this class
    }

    private void fetchCompletedTasks() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alltask").document("01").collection("tasks")
                .whereEqualTo("status", "completed")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getId();
                            String taskName = document.getString("task");
                            String assignedTo = document.getString("assignedTo");




                            fetchSubmissions(taskId, taskName); // Pass taskName to the next function
                        }
                    }
                });
    }

    private void fetchSubmissions(String taskId, String taskName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("submissions").document("02").collection("tasksubmission").document("01")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String submittedBy = documentSnapshot.getString("SubmittedBy");


                        // Get the ID of the user who submitted the task
                        String submittedById = documentSnapshot.getString("submittedBy");

                        if (submittedById != null) {
                            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(submittedById);

                            userRef.get().addOnSuccessListener(userDocument -> {
                                String employeeName = "Unknown";
                                if (userDocument.exists()) {
                                    employeeName = userDocument.getString("name");
                                }

                                // Convert Firestore Timestamp to formatted string
                                Timestamp timestamp = documentSnapshot.getTimestamp("submissionTime");
                                String submissionTime = "N/A";
                                if (timestamp != null) {
                                    Date date = timestamp.toDate();
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                                    submissionTime = sdf.format(date);
                                    Log.d("DEBUG", "Timestamp from Firestore: " + timestamp);

                                }

                           String reply = documentSnapshot.getString("reply");
                            String attachment = documentSnapshot.getString("attachment");

                            // Pass the taskName to HRSubmissionModel
                            HRSubmissionModel submission = new HRSubmissionModel(taskName, employeeName, submissionTime, reply, attachment);
                            submissionList.add(submission);
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);

                        }).addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                        });

                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
    });

}}
