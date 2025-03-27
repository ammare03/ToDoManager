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

public class AddTaskActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private Spinner prioritySpinner;
    private Button addTaskButton;
    private DatabaseReference tasksRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks").child(userId);

        titleEditText = findViewById(R.id.taskTitleEditText);
        descriptionEditText = findViewById(R.id.taskDescriptionEditText);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        addTaskButton = findViewById(R.id.addTaskButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });
    }

    private void addTask() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priority = prioritySpinner.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(AddTaskActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = tasksRef.push().getKey();
        Task task = new Task(taskId, title, description, priority);

        assert taskId != null;
        tasksRef.child(taskId).setValue(task).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(AddTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddTaskActivity.this, "Failed to add task", Toast.LENGTH_SHORT).show();
            }
        });
    }
}