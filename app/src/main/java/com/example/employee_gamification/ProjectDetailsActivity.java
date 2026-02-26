package com.example.employee_gamification;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yourpackage.name.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ProjectDetailsActivity extends AppCompatActivity {
    FirebaseFirestore db;
    TextView projectName, createdBy, deadline, status;

    private RecyclerView recyclerView;

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_PICK_PDF = 102;
    private static final int REQUEST_CODE_PICK_ZIP = 103;


    private Uri selectedImageUri, selectedPdfUri, selectedZipUri, doneImageUri;
    private String projectId ; // dynamically set as needed
    private String taskId = "03";    // dynamically set as needed




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        db = FirebaseFirestore.getInstance();
        projectName = findViewById(R.id.projectName);
        createdBy = findViewById(R.id.createdBy);
        deadline = findViewById(R.id.deadline);
        status = findViewById(R.id.status);

        projectId = getIntent().getStringExtra("projectId");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        if (projectId != null) {
            Log.d("Firestore", "Received Project ID: " + projectId);
            loadProjectDetails();
            fetchEmployeeTasks("02" ,projectId );

        } else {
            Toast.makeText(this, "Invalid Project ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProjectDetails() {
        db.collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("Firestore", "Project Data: " + documentSnapshot.getData());

                        projectName.setText(documentSnapshot.getString("projectname"));
                        createdBy.setText("Created By: " + documentSnapshot.getString("createdBy"));
//                        deadline.setText("Deadline: " + documentSnapshot.getString("deadline"));
                        Timestamp deadlineTimestamp = documentSnapshot.getTimestamp("deadline");
                        if (deadlineTimestamp != null) {
                            Date deadlineDate = deadlineTimestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                            String formattedDeadline = sdf.format(deadlineDate);
                            deadline.setText("Deadline: " + formattedDeadline);
                        } else {
                            deadline.setText("Deadline: N/A");
                        }


                        status.setText("Current Status: " + documentSnapshot.getString("status"));
                    } else {
                        Toast.makeText(this, "Project Not Found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Failed to load project details", e);
                });

    }




    private void fetchEmployeeTasks(String userId, String projectId) {

//        Toast.makeText(this, userId, Toast.LENGTH_LONG).show();

//        Toast.makeText(this, projectId, Toast.LENGTH_LONG).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<EMTaskModel> taskList = new ArrayList<>();

        Log.d("FirestoreDebug", "Fetching tasks for userId: " + userId + ", projectId: " + projectId);

        db.collection("usertaskhistory").document(userId).collection(projectId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FirestoreDebug", "No tasks found for this user and project.");
                        return; // Stop execution if no tasks
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String taskId = document.getId();
                        String taskName = document.getString("task");
                        String status = document.getString("status");

                        // Convert deadline timestamp
                        Timestamp deadlineTimestamp = document.getTimestamp("deadline");
                        String deadlineFormatted = "N/A";
                        if (deadlineTimestamp != null) {
                            Date deadlineDate = deadlineTimestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                            deadlineFormatted = sdf.format(deadlineDate);
                        }

                        // Create and add to the list
                        EMTaskModel task = new EMTaskModel(userId ,taskId, taskName, deadlineFormatted, status);
                        taskList.add(task);

                        Log.d("FirestoreDebug", "Task added: " + taskName + " - " + status);
                    }

//                     Sort tasks
                    Collections.sort(taskList, (a, b) -> {
                        List<String> order = Arrays.asList("active", "completed", "rejected");
                        return Integer.compare(order.indexOf(a.getStatus()), order.indexOf(b.getStatus()));
                    });

//                     Ensure RecyclerView is initialized
                    if (recyclerView == null) {
                        Log.e("RecyclerViewDebug", "RecyclerView is null!");
                        return;
                    }

                    // Set adapter
                    EMTaskHistoryAdapter adapter = new EMTaskHistoryAdapter(taskList, new EMTaskHistoryAdapter.OnAddClickListener() {
                        @Override
                        public void onAddClicked() {
                            showReplyDialog();// this opens your dialog
                        }
                    });
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ProjectDetailsActivity.this));
                })
                .addOnFailureListener(e -> Log.e("FirestoreDebug", "Error fetching tasks", e));

    }

    private void showReplyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.reply_popup);

        // Get the root layout of the popup
        View popupLayout = dialog.findViewById(R.id.replyPopupLayout);

        // Apply animations to the root layout, not to the dialog
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        popupLayout.startAnimation(slideUp);

        EditText editTextReply = dialog.findViewById(R.id.editTextReply);
        ImageButton btnImage = dialog.findViewById(R.id.btnImage);
        ImageButton btnPdf = dialog.findViewById(R.id.btnPdf);
        ImageButton btnZip = dialog.findViewById(R.id.btnZip);
        ImageButton btnDone = dialog.findViewById(R.id.btnDoneImage);


        btnImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });


        btnPdf.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "PDF Select Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Open file picker for PDF
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), REQUEST_CODE_PICK_PDF);
        });

        btnZip.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "ZIP Select Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Open file picker for ZIP
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select ZIP File"), REQUEST_CODE_PICK_ZIP);
        });

        btnDone.setOnClickListener(v -> {
            Uri doneImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    getResources().getResourcePackageName(R.drawable.ic_done_mark) + '/' +
                    getResources().getResourceTypeName(R.drawable.ic_done_mark) + '/' +
                    getResources().getResourceEntryName(R.drawable.ic_done_mark));

            Toast.makeText(getApplicationContext(), "Predefined Done Image Set", Toast.LENGTH_SHORT).show();
        });



        Button btnSubmit = dialog.findViewById(R.id.btnSubmitReply);
        btnSubmit.setOnClickListener(v -> {
            String replyText = editTextReply.getText().toString().trim();

            if (replyText.isEmpty()) {
                Toast.makeText(this, "Reply cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri fileUri = selectedImageUri != null ? selectedImageUri :
                    selectedPdfUri != null ? selectedPdfUri :
                            selectedZipUri != null ? selectedZipUri :
                                    doneImageUri;

            if (fileUri != null) {
                uploadFileToStorage(fileUri, replyText, dialog);
            } else {
                saveReplyToFirestore(null, replyText, dialog);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

//         Optional: if you want to animate on dismiss or hide later
//        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
//        popupLayout.startAnimation(slideDown);
//        popupLayout.setVisibility(View.GONE); // Only if you're hiding it manually
    }

    private void uploadFileToStorage(Uri fileUri, String replyText, Dialog dialog) {
        String fileName = "submissions/" + UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveReplyToFirestore(downloadUrl, replyText, dialog);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveReplyToFirestore(String attachmentUrl, String replyText, Dialog dialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String submittedBy = "02";

        // First, prepare submission map
        Map<String, Object> submission = new HashMap<>();
        submission.put("reply", replyText);
        submission.put("submissionTime", FieldValue.serverTimestamp());
        submission.put("submittedBy", submittedBy);
        submission.put("attachment", attachmentUrl);

        // Submit reply
        db.collection("submissions")
                .document(projectId)
                .collection("tasksubmission")
                .document(taskId)
                .set(submission)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reply submitted successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // ✅ Fetch the resolved timestamp from Firestore for completedAt
                    db.collection("submissions")
                            .document(projectId)
                            .collection("tasksubmission")
                            .document(taskId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Timestamp completedAt = documentSnapshot.getTimestamp("submissionTime");

                                if (completedAt != null) {
                                    // ✅ Update alltask with status and completedAt
                                    Map<String, Object> updateAllTask = new HashMap<>();
                                    updateAllTask.put("status", "completed");
                                    updateAllTask.put("completedAt", completedAt);

                                    db.collection("alltask")
                                            .document(projectId)
                                            .collection("tasks")
                                            .document(taskId)
                                            .update(updateAllTask)
                                            .addOnSuccessListener(unused -> Log.d("Firestore", "Task status updated in alltask"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update status in alltask", e));

                                    // ✅ Update usertaskhistory with status and completedAt
                                    Map<String, Object> updateHistory = new HashMap<>();
                                    updateHistory.put("status", "completed");
                                    updateHistory.put("completedAt", completedAt);

                                    db.collection("usertaskhistory")
                                            .document(submittedBy)
                                            .collection(projectId)
                                            .document(taskId)
                                            .update(updateHistory)
                                            .addOnSuccessListener(unused -> Log.d("Firestore", "Task status updated in usertaskhistory"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update status in usertaskhistory", e));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error submitting reply", Toast.LENGTH_SHORT).show();
                });
    }




}
