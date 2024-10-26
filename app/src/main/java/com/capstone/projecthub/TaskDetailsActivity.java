package com.capstone.projecthub;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Adapter.TaskDetailsMembersAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.Model.User;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityTaskDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TaskDetailsActivity extends AppCompatActivity {

    private ActivityTaskDetailsBinding binding;
    private FirebaseFirestore db;
    private Tasks task;
    private ArrayList<Tasks> tasks;
    private ArrayList<String> assignedUsersId;
    private ArrayList<User> assignedUsers;
    private TaskDetailsMembersAdapter assignedMembersAdapter;
    private PreferenceManager preferenceManager;
    private Project currentProject;
    private final int KEY_RESULT_FOR_REFRESH_LIST = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        checkForLeader();
        checkForAssignedMembers();
        loadTaskDetails();
        loadAssignedMembers();
    }

    private void checkForLeader() {
        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(currentProject.projectLeaderId)) {
            binding.buttonTaskDetailsEditTaskDetails.setVisibility(View.VISIBLE);
            binding.buttonTaskDetailsSetStatus.setVisibility(View.VISIBLE);
            binding.buttonTaskDetailsEditTaskDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskDetailsActivity.this, EditTaskDetailsActivity.class);
                    intent.putExtra("putTaskToEditTask", task);
                    startActivityForResult(intent, KEY_RESULT_FOR_REFRESH_LIST);
                }
            });
            binding.buttonTaskDetailsSetStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setStatusDialog();
                }
            });
        } else {
            binding.buttonTaskDetailsEditTaskDetails.setVisibility(View.GONE);
            binding.buttonTaskDetailsSetStatus.setVisibility(View.GONE);
        }
    }

    private void setStatusDialog() {
        Dialog dialog = new Dialog(TaskDetailsActivity.this);
        dialog.setContentView(R.layout.change_task_status_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();

        LinearLayout buttonUnfinished = dialog.findViewById(R.id.buttonChooseStatusUnfinished);
        LinearLayout buttonDone = dialog.findViewById(R.id.buttonChooseStatusDone);
        LinearLayout buttonError = dialog.findViewById(R.id.buttonChooseStatusError);

        buttonUnfinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> update = new HashMap<>();
                update.put(Constants.KEY_TASK_STATUS, Constants.KEY_TASK_STATUS_UNDONE);

                updateStatus(update, dialog);
                task.status = "Unfinished";
            }
        });
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> update = new HashMap<>();
                update.put(Constants.KEY_TASK_STATUS, Constants.KEY_TASK_STATUS_DONE);

                updateStatus(update, dialog);
                task.status = "Done";
            }
        });
        buttonError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> update = new HashMap<>();
                update.put(Constants.KEY_TASK_STATUS, Constants.KEY_TASK_STATUS_ERROR);

                updateStatus(update, dialog);
                task.status = "Error Encountered";
            }
        });
    }

    private void updateStatus(Map<String, Object> update, Dialog dialog) {
        db.collection(Constants.KEY_TASKS_LIST).document(task.tasksId).update(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(TaskDetailsActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        loadTaskDetails();
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TaskDetailsActivity.this, "Unable to update status", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private void checkForAssignedMembers() {
        if (assignedUsersId.contains(preferenceManager.getString(Constants.KEY_USER_ID))) {
            binding.buttonTaskDetailsSetStatus.setVisibility(View.VISIBLE);
            binding.buttonTaskDetailsSetStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setStatusDialog();
                }
            });
        } else {
            binding.buttonTaskDetailsSetStatus.setVisibility(View.GONE);
        }
    }

    private void loadAssignedMembers() {
        int[] membersRetrievedFromDatabase = {0};
        for (String usersId : assignedUsersId) {
            db.collection(Constants.KEY_USER_LIST).document(usersId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    assignedUsers = new ArrayList<>();
                                    assignedMembersAdapter = getAssignedMembersAdapter();

                                    User user = new User();
                                    user.email = document.getString(Constants.KEY_EMAIL);
                                    user.userId = document.getId();
                                    user.username = document.getString(Constants.KEY_USERNAME);

                                    membersRetrievedFromDatabase[0]++;
                                    assignedUsers.add(user);
                                }
                                if (membersRetrievedFromDatabase[0] == assignedUsersId.size()) {
                                    //when done retrieving
                                    binding.recyclerTaskDetailsMembersList.setAdapter(assignedMembersAdapter);
                                    isRecyclerLoading(false);
                                    binding.imageErrorTaskDetails.setVisibility(View.GONE);

                                    assignedUsers.sort(Comparator.comparing(obj -> obj.username));
                                    assignedMembersAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(TaskDetailsActivity.this, "Unable to get members", Toast.LENGTH_SHORT).show();
                            isRecyclerLoading(false);

                            binding.imageErrorTaskDetails.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private TaskDetailsMembersAdapter getAssignedMembersAdapter() {
        TaskDetailsMembersAdapter adapter = new TaskDetailsMembersAdapter(assignedUsers, TaskDetailsActivity.this);

        return adapter;
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerTaskDetailsMembersList.setVisibility(View.GONE);
            binding.progressBarTaskDetails.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerTaskDetailsMembersList.setVisibility(View.VISIBLE);
            binding.progressBarTaskDetails.setVisibility(View.GONE);
        }
    }

    private void loadTaskDetails() {
        binding.textTaskDetailsName.setText(task.tasksName);
        String dueDate = "Due Date: " + task.dateStringDueDate();
        binding.textTaskDetailsDueDate.setText(dueDate);
        String assignedDate = "Assigned Date: " + task.dateStringAssignedDate();
        binding.textTaskDetailsAssignedDate.setText(assignedDate);
        binding.textTaskDetailsDesc.setText(task.tasksDesc);

        if (task.status.equals("Done")) {
            binding.backgroundImageTaskDetailsStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(TaskDetailsActivity.this, R.color.mainGreen)
            );
            binding.imageTaskDetailsStatus.setImageDrawable(ContextCompat.getDrawable(
                    TaskDetailsActivity.this, R.drawable.baseline_done_24));
        } else if (task.status.equals("Error Encountered")) {
            binding.backgroundImageTaskDetailsStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(TaskDetailsActivity.this, R.color.red)
            );
            binding.imageTaskDetailsStatus.setImageDrawable(ContextCompat.getDrawable(
                    TaskDetailsActivity.this, R.drawable.danger));
        } else if (task.status.equals("Unfinished")) {
            binding.backgroundImageTaskDetailsStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(TaskDetailsActivity.this, R.color.grey)
            );
            binding.imageTaskDetailsStatus.setImageDrawable(ContextCompat.getDrawable(
                    TaskDetailsActivity.this, R.drawable.baseline_edit_24));
        }
    }

    private void setListeners() {
        binding.layoutButtonBackFromTaskDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void init() {
        isRecyclerLoading(true);

        db = FirebaseFirestore.getInstance();
        task = (Tasks) getIntent().getSerializableExtra("putTaskForTaskDetails");
        currentProject = (Project) getIntent().getSerializableExtra("putProjectForTaskDetails");
        assignedUsersId = task.usersId;
        preferenceManager = new PreferenceManager(TaskDetailsActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_RESULT_FOR_REFRESH_LIST) {
            if (resultCode == RESULT_OK) {
                loadTaskDetails();
            }
        }
    }
}