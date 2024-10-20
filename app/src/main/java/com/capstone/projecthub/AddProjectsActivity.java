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

import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityAddProjectsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddProjectsActivity extends AppCompatActivity {

    private ActivityAddProjectsBinding binding;
    private DatePickerDialog datePickerDialog;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProjectsBinding.inflate(getLayoutInflater());
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

    private void init() {
        binding.editTextAddProjectDueDate.setInputType(InputType.TYPE_NULL);
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(AddProjectsActivity.this);
    }

    private Date getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date date = dateFormat.parse(binding.editTextAddProjectDueDate.getText().toString());

            //Success
            return date;
        } catch (ParseException e) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void setListeners() {
        binding.buttonCancelAddProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.buttonCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date dateSelected = getDate();

                if (dateSelected != null) {
                    Map<String, Object> project = new HashMap<>();
                    project.put(Constants.KEY_PROJECT_NAME, binding.editTextAddProjectName.getText().toString());
                    project.put(Constants.KEY_PROJECT_DESC, binding.editTextAddProjectDesc.getText().toString());
                    project.put(Constants.KEY_PROJECT_DUE_DATE, dateSelected);
                    //add user to the members list
                    project.put(Constants.KEY_PROJECT_MEMBERS_ID, Arrays.asList(preferenceManager.getString(Constants.KEY_USER_ID)));

                    db.collection(Constants.KEY_PROJECT_LISTS)
                            .add(project)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    //(TODO) Direct to activity
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("result", documentReference.getId());
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddProjectsActivity.this, "Failed to update db", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        binding.editTextAddProjectDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(AddProjectsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dateChosen = dayOfMonth + "/" + (month + 1) + "/" + year;
                        binding.editTextAddProjectDueDate.setText(dateChosen);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
    }
}