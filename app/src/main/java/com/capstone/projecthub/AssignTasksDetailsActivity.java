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

import com.capstone.projecthub.Model.DataWrapper;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.User;
import com.capstone.projecthub.databinding.ActivityAssignTasksDetailsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AssignTasksDetailsActivity extends AppCompatActivity {

    private ActivityAssignTasksDetailsBinding binding;
    private Project project;
    private ArrayList<User> assignedMembers;
    private DatePickerDialog datePickerDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssignTasksDetailsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
    }

    private boolean detailsValid() {
        if (binding.editTextTasksNameAssigning.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a name for your task", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.editTextTasksDescAssigning.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void setListeners() {
        binding.buttonCalenderForTaskDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
            }
        });
        binding.buttonDateTasksDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
            }
        });
        binding.buttonTasksNextAssignTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailsValid()) {
                    Date dateSelected = getDate();
                    ArrayList<String> assignedMembersId= new ArrayList<>();
                    for (User user : assignedMembers) {
                        assignedMembersId.add(user.userId);
                    }

                    if (dateSelected != null) {
                        Map<String, Object> task = new HashMap<>();
                        task.put(Constants.KEY_TASK_NAME, binding.editTextTasksNameAssigning.getText().toString());
                        task.put(Constants.KEY_TASK_DESC, binding.editTextTasksDescAssigning.getText().toString());
                        task.put(Constants.KEY_TASK_STATUS, Constants.KEY_TASK_STATUS_UNDONE);
                        task.put(Constants.KEY_TASK_ASSIGNED_DATE, new Date()); //todays date
                        task.put(Constants.KEY_TASK_DUE_DATE, dateSelected);
                        task.put(Constants.KEY_PROJECT_MEMBERS_ID, assignedMembersId);
                        task.put(Constants.KEY_PROJECT_ID, project.projectId);

                        db.collection(Constants.KEY_TASKS_LIST).add(task)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(AssignTasksDetailsActivity.this,
                                                "Added Successfully",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent();
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AssignTasksDetailsActivity.this,
                                                "Error Adding Tasks",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
        });
        binding.buttonBackFromTasksDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    private void showCalender() {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        datePickerDialog = new DatePickerDialog(AssignTasksDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String dateChosen = dayOfMonth + "/" + (month + 1) + "/" + year;
                binding.buttonDateTasksDetails.setText(dateChosen);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private Date getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            //Success
            return dateFormat.parse(binding.buttonDateTasksDetails.getText().toString());
        } catch (ParseException e) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void init() {
        project = (Project) getIntent().getSerializableExtra("putProjectForTasksDetails");
        DataWrapper dw = (DataWrapper) getIntent().getSerializableExtra("putAssignedMemberForTasksDetails");
        assignedMembers = dw.getUsers();
        db = FirebaseFirestore.getInstance();
        binding.buttonDateTasksDetails.setInputType(InputType.TYPE_NULL);
    }
}