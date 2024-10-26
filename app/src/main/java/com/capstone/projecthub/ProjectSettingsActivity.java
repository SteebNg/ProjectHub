package com.capstone.projecthub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.databinding.ActivityProjectSettingsBinding;
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

public class ProjectSettingsActivity extends AppCompatActivity {

    private ActivityProjectSettingsBinding binding;
    private Project currentProject;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectSettingsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        setProjectDetails();
    }

    private void setProjectDetails() {
        binding.textEditProjectName.setText(currentProject.projectName);
        binding.textEditProjectDesc.setText(currentProject.projectDescription);
        binding.textEditProjectDate.setText(currentProject.dateString());
        binding.imageColor.setBackgroundTintList(projectColor(currentProject.projectColor));
    }

    private ColorStateList projectColor(String projectColor) {
        ColorStateList color = ContextCompat.getColorStateList(ProjectSettingsActivity.this, R.color.lightBlue);

        if (projectColor != null) {
            switch (projectColor) {
                case "0": {
                    color = ContextCompat.getColorStateList(ProjectSettingsActivity.this, R.color.mainGreen);
                    break;
                }
                case "1": {
                    color = ContextCompat.getColorStateList(ProjectSettingsActivity.this, R.color.red);
                    break;
                }
                case "2": {
                    color = ContextCompat.getColorStateList(ProjectSettingsActivity.this, R.color.yellow);
                    break;
                }
                default: {
                    color = ContextCompat.getColorStateList(ProjectSettingsActivity.this, R.color.lightBlue);
                    break;
                }
            }
        }
        return color;
    }

    private void setListeners() {
        binding.buttonEditNameProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProjectSettingsActivity.this);
                dialog.setContentView(R.layout.edit_project_name_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);
                dialog.show();

                Button buttonCancel = dialog.findViewById(R.id.buttonCancelEditProjectName);
                Button buttonConfirm = dialog.findViewById(R.id.buttonConfirmEditProjectName);
                EditText projectNameChange = dialog.findViewById(R.id.editTextEditProjectNameConfirm);

                projectNameChange.setText(currentProject.projectName);

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String projectName = projectNameChange.getText().toString();
                        if (projectName.isEmpty()) {
                            Toast.makeText(ProjectSettingsActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> update = new HashMap<>();
                            update.put(Constants.KEY_PROJECT_NAME, projectName);

                            db.collection(Constants.KEY_PROJECT_LISTS).document(currentProject.projectId).update(update)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.textEditProjectName.setText(projectName);
                                            currentProject.projectName = projectName;
                                            Toast.makeText(ProjectSettingsActivity.this, "Changed successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProjectSettingsActivity.this, "Failed to change project Name", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
        binding.buttonEditDescProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProjectSettingsActivity.this);
                dialog.setContentView(R.layout.edit_project_desc_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);
                dialog.show();

                Button buttonCancel = dialog.findViewById(R.id.buttonCancelEditProjectDesc);
                Button buttonConfirm = dialog.findViewById(R.id.buttonConfirmEditProjectDesc);
                EditText projectDescChange = dialog.findViewById(R.id.editTextEditProjectDescConfirm);

                projectDescChange.setText(currentProject.projectDescription);

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String projectDesc = projectDescChange.getText().toString();

                        if (projectDesc.isEmpty()) {
                            Toast.makeText(ProjectSettingsActivity.this, "Please enter a description", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> update = new HashMap<>();
                            update.put(Constants.KEY_PROJECT_DESC, projectDesc);

                            db.collection(Constants.KEY_PROJECT_LISTS).document(currentProject.projectId).update(update)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.textEditProjectDesc.setText(projectDesc);
                                            currentProject.projectDescription = projectDesc;
                                            Toast.makeText(ProjectSettingsActivity.this, "Changed Successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProjectSettingsActivity.this, "Failed to change", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
        binding.buttonEditDateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProjectSettingsActivity.this);
                dialog.setContentView(R.layout.edit_project_date_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);
                dialog.show();

                Button buttonCancel = dialog.findViewById(R.id.buttonCancelEditProjectDate);
                Button buttonConfirm = dialog.findViewById(R.id.buttonConfirmEditProjectDate);
                EditText projectDateChange = dialog.findViewById(R.id.editTextEditProjectDateConfirm);

                projectDateChange.setInputType(InputType.TYPE_NULL);
                projectDateChange.setText(currentProject.dateString());

                projectDateChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog datePickerDialog;

                        final Calendar calendar = Calendar.getInstance();
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);

                        datePickerDialog = new DatePickerDialog(ProjectSettingsActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String dateChosen = dayOfMonth + "/" + (month + 1) + "/" + year;
                                projectDateChange.setText(dateChosen);
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Date dateSelected = getDate(projectDateChange.getText().toString());

                        if (dateSelected == null) {
                            Toast.makeText(ProjectSettingsActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> update = new HashMap<>();
                            update.put(Constants.KEY_PROJECT_DUE_DATE, dateSelected);

                            db.collection(Constants.KEY_PROJECT_LISTS).document(currentProject.projectId).update(update)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.textEditProjectDate.setText(projectDateChange.getText().toString());
                                            currentProject.dueDate = dateSelected;
                                            Toast.makeText(ProjectSettingsActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProjectSettingsActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
        binding.buttonColorProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProjectSettingsActivity.this);
                dialog.setContentView(R.layout.edit_project_color_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(true);
                dialog.show();

                LinearLayout buttonGreen = dialog.findViewById(R.id.buttonEditProjectGreenChoose);
                LinearLayout buttonRed = dialog.findViewById(R.id.buttonEditProjectRedChoose);
                LinearLayout buttonYellow = dialog.findViewById(R.id.buttonEditProjectYellowChoose);
                LinearLayout buttonBlue = dialog.findViewById(R.id.buttonEditProjectBlueChoose);

                buttonGreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProjectColor("0", dialog);
                    }
                });
                buttonRed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProjectColor("1", dialog);
                    }
                });
                buttonYellow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProjectColor("2", dialog);
                    }
                });
                buttonBlue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProjectColor("3", dialog);
                    }
                });
            }
        });
        binding.buttonBackFromProjectSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void updateProjectColor(String color, Dialog dialog) {
        Map<String, Object> update = getColorUpdate(color);

        db.collection(Constants.KEY_PROJECT_LISTS).document(currentProject.projectId).update(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProjectSettingsActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        binding.imageColor.setBackgroundTintList(projectColor(color));
                        currentProject.projectColor = color;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProjectSettingsActivity.this, "Failed to update project color", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private static @NonNull Map<String, Object> getColorUpdate(String color) {
        String colorChanged;

        switch (color) {
            case "0": {
                colorChanged = "0";
                break;
            }
            case "1": {
                colorChanged = "1";
                break;
            }
            case "2": {
                colorChanged = "2";
                break;
            }
            default: {
                colorChanged = "3";
                break;
            }
        }

        Map<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_PROJECT_COLOR, colorChanged);
        return update;
    }

    private Date getDate(String dateText) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            return dateFormat.parse(dateText);
        } catch (ParseException e) {
            Toast.makeText(this, "Failed to parse date to String", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void init() {
        currentProject = (Project) getIntent().getSerializableExtra("projectForSettingActivity");
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}