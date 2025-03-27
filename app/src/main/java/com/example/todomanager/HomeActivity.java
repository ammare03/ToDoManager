package com.example.todomanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private FloatingActionButton fab;
    private DatabaseReference tasksRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up Firebase
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks").child(userId);

        // Set up Toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // RecyclerView setup
        recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(this, taskList, this);
        recyclerView.setAdapter(adapter);

        // Floating Action Button to add tasks
        fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddTaskActivity.class));
        });

        // Load tasks from Firebase
        loadTasks();

        // Swipe to delete functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // Not used
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task taskToDelete = taskList.get(position);
                // Remove from Firebase
                tasksRef.child(taskToDelete.getId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(HomeActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                    }
                });
                // Remove from local list
                taskList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadTasks() {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                // Sort tasks based on priority: High -> Medium -> Low
                taskList.sort(new Comparator<Task>() {
                    @Override
                    public int compare(Task t1, Task t2) {
                        return getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority());
                    }

                    private int getPriorityValue(String priority) {
                        switch (priority) {
                            case "High":   return 1;
                            case "Medium": return 2;
                            case "Low":    return 3;
                            default:       return 4;
                        }
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInfoClick(Task task) {
        // Open TaskDetailActivity to view/update the task
        Intent intent = new Intent(HomeActivity.this, TaskDetailActivity.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("taskTitle", task.getTitle());
        intent.putExtra("taskDescription", task.getDescription());
        intent.putExtra("taskPriority", task.getPriority());
        startActivity(intent);
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Perform logout
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}