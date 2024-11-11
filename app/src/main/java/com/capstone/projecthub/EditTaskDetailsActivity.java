package com.capstone.projecthub;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.databinding.ActivityEditTaskDetailsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTaskDetailsActivity extends AppCompatActivity {

    private ActivityEditTaskDetailsBinding binding;
    private Tasks task;
    private DatePickerDialog datePickerDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTaskDetailsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        loadTaskDetails();
    }

    private void loadTaskDetails() {
        binding.editTextEditTaskTaskName.setText(task.tasksName);
        binding.editTextEditTaskTaskDesc.setText(task.tasksDesc);
        binding.editTextEditTaskTaskDueDate.setText(task.dateStringDueDate());
    }

    private void setListeners() {
        binding.buttonBackFromEditTaskDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        binding.buttonEditTaskConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.editTextEditTaskTaskDesc.getText().toString().isEmpty()) {
                    Toast.makeText(EditTaskDetailsActivity.this, "Please enter the description", Toast.LENGTH_SHORT).show();
                } else if (binding.editTextEditTaskTaskName.getText().toString().isEmpty()) {
                    Toast.makeText(EditTaskDetailsActivity.this, "Please enter the name of the task", Toast.LENGTH_SHORT).show();
                } else if (binding.editTextEditTaskTaskDueDate.getText().toString().isEmpty()) {
                    Toast.makeText(EditTaskDetailsActivity.this, "Please enter a date", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> update = new HashMap<>();
                    update.put(Constants.KEY_TASK_NAME, binding.editTextEditTaskTaskName.getText().toString());
                    update.put(Constants.KEY_TASK_DESC, binding.editTextEditTaskTaskDesc.getText().toString());
                    update.put(Constants.KEY_TASK_DUE_DATE, getDate());

                    db.collection(Constants.KEY_TASKS_LIST).document(task.tasksId).update(update)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(EditTaskDetailsActivity.this, "Edit Task Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditTaskDetailsActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        binding.editTextEditTaskTaskDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(EditTaskDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dateChosen = dayOfMonth + "/" + (month + 1) + "/" + year;
                        binding.editTextEditTaskTaskDueDate.setText(dateChosen);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    private Date getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            //Success
            return dateFormat.parse(binding.editTextEditTaskTaskDueDate.getText().toString());
        } catch (ParseException e) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void init() {
        task = (Tasks) getIntent().getSerializableExtra("putTaskToEditTask");

        binding.editTextEditTaskTaskDueDate.setInputType(InputType.TYPE_NULL);
        db = FirebaseFirestore.getInstance();
    }
}