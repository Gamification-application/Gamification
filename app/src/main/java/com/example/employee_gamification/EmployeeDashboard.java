package com.example.employee_gamification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourpackage.name.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

public class EmployeeDashboard extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton menuButton;
    ActionBarDrawerToggle toggle;

    private RecyclerView projectlistrecyclerView;
    private EMTaskHistoryAdapter taskAdapter;
    private List<EMTaskModel> taskList = new ArrayList<>();

    private RecyclerView rvCalendar, recentActivityRecycler;
    private FirebaseFirestore db;
    private String userId = "04"; // You can set this dynamically based on login
    private String userEmail;



    private ProjectAdapter adapter;
    private List<ProjectModel> projectList;



    private TextView tvHelloName;
    private ImageView ivUserIcon;

    private String userName , email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_dashboard);

        recentActivityRecycler = findViewById(R.id.nearestDeadlineRecycler); // make sure this ID matches your XML
        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new EMTaskHistoryAdapter(taskList, new EMTaskHistoryAdapter.OnAddClickListener() {
            @Override
            public void onAddClicked() {
                //showReplyDialog(); // this opens your dialog
                //
                }            }
            );
        recentActivityRecycler.setAdapter(taskAdapter);
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        tvHelloName = findViewById(R.id.employeeNameText);
        ivUserIcon = findViewById(R.id.userIcon);
        projectlistrecyclerView = findViewById(R.id.projectlistrecyclerView);
        rvCalendar = findViewById(R.id.rvEmployeeCalendar);
        recentActivityRecycler = findViewById(R.id.projectlistrecyclerView);

        // RecyclerView setup for projects
        projectList = new ArrayList<>();
        adapter = new ProjectAdapter(this, projectList);
        projectlistrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectlistrecyclerView.setAdapter(adapter);


        // Get email from LoginActivity
        userEmail = getIntent().getStringExtra("email");

        if (userEmail != null) {
            fetchEmployeeDetails(userEmail);
        } else {
            Toast.makeText(this, "Email not received", Toast.LENGTH_SHORT).show();
        }



    drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.menuButton);


        // Open drawer on 3-dot button click
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
            }


        });


        db.collection("usertaskhistory").document("02")
                .collection("03")
                .document("01")
                .get()
                .addOnSuccessListener(taskDoc -> {
                    String taskTitle = taskDoc.getString("task");
                    Timestamp ts = taskDoc.getTimestamp("deadline");
                    Toast.makeText(this, taskTitle+ts, Toast.LENGTH_LONG).show();


                });

        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle menu item actions here
                int id = item.getItemId();
                if (id == R.id.hr_dashboard) {
                    // handle HR Dashboard click
                } else if (id == R.id.nav_leaderboard) {
                    // handle manage employees click
                }
                else if (id == R.id.nav_notifications) {
                    // handle manage employees click
                }
                else if (id == R.id.nav_settings) {
                    // handle manage employees click
                }
                else if (id == R.id.nav_logout) {
                    finish(); // or go back to login
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });


        // Get email from LoginActivity
        userEmail = getIntent().getStringExtra("email");

        if (userEmail != null) {
            fetchEmployeeDetails(userEmail);
            getUserInfoFromEmail(userEmail);
            email=userEmail;

        } else {
            Toast.makeText(this, "Email not received", Toast.LENGTH_SHORT).show();
        }

        // Setup horizontal calendar
        rvCalendar.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            dateList.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        EmployeeCalendarAdapter adapterCalendar = new EmployeeCalendarAdapter(this, dateList, date -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Log.d("Calendar", "Selected date: " + sdf.format(date));
        });

        rvCalendar.setAdapter(adapterCalendar);

        NavigationView navigationView = findViewById(R.id.navigationView);


        // Open drawer on 3-dot button click
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = new Intent();
                if (id == R.id.nav_dashboard) {
                    //   startActivity(new Intent(EmployeeDashboard.this, EmployeeDashboard.class));
                    return true;
                } else if (id == R.id.nav_leaderboard) {
                    startActivity(new Intent(EmployeeDashboard.this, EMLeaderboardActivity.class));
                    return true;
                } else if (id == R.id.nav_notifications) {
                    startActivity(new Intent(EmployeeDashboard.this, EMNotificationActivity.class));
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent i = new Intent(EmployeeDashboard.this, ProfileActivity.class);
                    i.putExtra("email", userEmail);  // Make sure userEmail is the current user's email
                    startActivity(i);

                    return true;
                } else if (id == R.id.nav_Project) {
                    startActivity(new Intent(EmployeeDashboard.this, EMProjectListActivity.class));
                    intent.putExtra("Email",email);


                    return true;
                } else if (id == R.id.nav_logout) {
                    startActivity(new Intent(EmployeeDashboard.this, LoginActivity.class));
                    finish();
                    return true;
                }

                return false;
            }
        });



        // Load task logic
    }


    private void fetchEmployeeDetails(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            userId = doc.getId();
                            userName = doc.getString("name");

                            tvHelloName.setText(userName);
                            //loadProjects();
                            break;
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    private void loadProjects() {
//        db.collection("projects")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
//                        Toast.makeText(this, "No projects found", Toast.LENGTH_LONG).show();
//                        Log.e("Firestore", "No documents found in 'projects' collection.");
//                        return;
//                    }
//
//                    projectList.clear();
//                    for (DocumentSnapshot document : queryDocumentSnapshots) {
//                        List<String> assignedEmployees = (List<String>) document.get("assignedemployees");
//
//                        if (assignedEmployees != null && assignedEmployees.contains(userId)) {
//                            String projectId = document.getId();
//                            String projectName = document.getString("projectname");
//                            projectList.add(new ProjectModel(projectId, projectName));
//                        }
//                    }
//                    adapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }


    private void getUserInfoFromEmail(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshots.getDocuments().get(0);
                        String userId = userDoc.getId();
                        String name = userDoc.getString("name");
                        Toast.makeText(this,userId+name , Toast.LENGTH_SHORT).show();

                        List<String> assignedProjects = (List<String>) userDoc.get("assigndprojects");

                        if (assignedProjects != null) {
                            getProjectNames(userId, assignedProjects);
                        }
                    }
                });
    }

    private void getProjectNames(String userId, List<String> projectIds) {
        for (String projectId : projectIds) {
            db.collection("projects").document(projectId)
                    .get()
                    .addOnSuccessListener(projectDoc -> {
                        String projectName = projectDoc.getString("projectname");
                        Toast.makeText(this,projectName , Toast.LENGTH_SHORT).show();

                        if (projectName != null) {
                            loadTasks(userId, projectId, projectName);
                        }
                    });
        }
    }

    private void loadTasks(String userId, String projectId, String projectName) {
        db.collection("usertaskhistory").document(userId)
                .collection(projectId)
                .get()
                .addOnSuccessListener(groupDocs -> {
                    for (DocumentSnapshot taskGroupDoc : groupDocs) {
                        String taskDocId = taskGroupDoc.getId();
                        Toast.makeText(this, taskDocId, Toast.LENGTH_SHORT).show();

                        db.collection("usertaskhistory").document(userId)
                                .collection(projectId)
                                .document(taskDocId)
                                .get()
                                .addOnSuccessListener(taskDoc -> {
                                    String taskTitle = taskDoc.getString("task");
                                    String status = taskDoc.getString("status");
                                    Timestamp ts = taskDoc.getTimestamp("deadline");
                                    if (taskTitle != null && ts != null) {
                                        Date deadlineDate = ts.toDate();
                                        String formattedDate = formatDate(deadlineDate); // Converts to "dd/MM/yyyy" format

                                        taskList.add(new EMTaskModel(projectId, taskDocId,taskTitle,formatDate(deadlineDate), status));

                                        // ✅ FIX: notify using adapter instance
                                        taskAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }


}