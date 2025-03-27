package com.example.todomanager;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private Spinner prioritySpinner;
    private Button updateButton, deleteButton;
    private DatabaseReference taskRef;
    private FirebaseAuth auth;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        titleEditText = findViewById(R.id.detailTaskTitleEditText);
        descriptionEditText = findViewById(R.id.detailTaskDescriptionEditText);
        prioritySpinner = findViewById(R.id.detailPrioritySpinner);
        updateButton = findViewById(R.id.updateTaskButton);
        deleteButton = findViewById(R.id.deleteTaskButton);

        // Set up spinner for priority selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Retrieve passed task data from intent
        taskId = getIntent().getStringExtra("taskId");
        String title = getIntent().getStringExtra("taskTitle");
        String description = getIntent().getStringExtra("taskDescription");
        String priority = getIntent().getStringExtra("taskPriority");

        titleEditText.setText(title);
        descriptionEditText.setText(description);
        // Set spinner selection based on task priority
        if ("High".equals(priority)) {
            prioritySpinner.setSelection(0);
        } else if ("Medium".equals(priority)) {
            prioritySpinner.setSelection(1);
        } else {
            prioritySpinner.setSelection(2);
        }

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(userId).child(taskId);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTask();
            }
        });
    }

    private void updateTask() {
        String updatedTitle = titleEditText.getText().toString().trim();
        String updatedDescription = descriptionEditText.getText().toString().trim();
        String updatedPriority = prioritySpinner.getSelectedItem().toString();

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(TaskDetailActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Task updatedTask = new Task(taskId, updatedTitle, updatedDescription, updatedPriority);
        taskRef.setValue(updatedTask).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(TaskDetailActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                finish(); // Return to HomeActivity
            } else {
                Toast.makeText(TaskDetailActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTask() {
        taskRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(TaskDetailActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                finish(); // Return to HomeActivity
            } else {
                Toast.makeText(TaskDetailActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}