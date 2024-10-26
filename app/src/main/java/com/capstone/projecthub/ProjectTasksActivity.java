package com.capstone.projecthub;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Adapter.TasksListAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityProjectTasksBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class ProjectTasksActivity extends AppCompatActivity {

    private ActivityProjectTasksBinding binding;
    private FirebaseFirestore db;
    private Project currentProject;
    private final int KEY_RESULT_FOR_REFRESH_LIST = 8;
    private Tasks individualTaskInThisProject;
    private ArrayList<Tasks> tasksAll;
    private TasksListAdapter adapter;
    private FrameLayout selectedQueryBackground;
    private TextView selectedQueryTitle;
    private FrameLayout selectedQueryBackgroundNumber;
    private ArrayList<Tasks> tasksDone;
    private ArrayList<Tasks> tasksUndone;
    private ArrayList<Tasks> tasksError;
    private final int KEY_RESULT_FOR_REFRESH_FROM_TASK = 10;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectTasksBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        loadTasksDetails();
        checkLeader();
    }

    private void checkLeader() {
        if (currentProject.projectLeaderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
            binding.buttonProjectAssignTask.setVisibility(View.VISIBLE);
        }
    }

    private void loadTasksDetails() {
        Date date = new Date();
        String dateFormat = "dd/MM/yyyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(dateFormat);
        binding.textProjectTasksListsTodayDate.setText(df.format(date));

        db.collection(Constants.KEY_TASKS_LIST)
                .whereEqualTo(Constants.KEY_PROJECT_ID, currentProject.projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                tasksAll = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    individualTaskInThisProject = new Tasks();

                                    adapter = getTasksListAdapter();

                                    individualTaskInThisProject.projectId = currentProject.projectId;
                                    individualTaskInThisProject.tasksName = document.getString(Constants.KEY_TASK_NAME);
                                    individualTaskInThisProject.tasksDesc = document.getString(Constants.KEY_TASK_DESC);
                                    individualTaskInThisProject.status = getStatusCode(document.getString(Constants.KEY_TASK_STATUS));
                                    individualTaskInThisProject.usersId = (ArrayList<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID);
                                    individualTaskInThisProject.dueDate = document.getDate(Constants.KEY_TASK_DUE_DATE);
                                    individualTaskInThisProject.assignedDate = document.getDate(Constants.KEY_TASK_ASSIGNED_DATE);
                                    individualTaskInThisProject.tasksId = document.getId();

                                    tasksAll.add(individualTaskInThisProject);
                                }
                                loadDoneTasksDetail();
                                loadUndoneTasksDetail();
                                loadErrorTasksDetail();

                                String numberOfTasks = String.valueOf(tasksAll.size());
                                binding.textTasksListAllNumber.setText(numberOfTasks);

                                binding.recyclerProjectTasksList.setAdapter(adapter);

                                isRecyclerLoading(false);
                                binding.imageNoTasksList.setVisibility(View.GONE);
                                tasksAll.sort(Comparator.comparing(obj -> obj.dueDate));
                                adapter.notifyDataSetChanged();
                            } else {
                                binding.recyclerProjectTasksList.setVisibility(View.GONE);
                                binding.progressBarTasksProject.setVisibility(View.GONE);
                                binding.imageNoTasksList.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    private void loadErrorTasksDetail() {
        tasksError = new ArrayList<>();
        if (!tasksAll.isEmpty()) {
            for (Tasks taskError : tasksAll) {
                if (taskError.status.equals("Error Encountered")) {
                    tasksError.add(taskError);
                }
            }
            tasksError.sort(Comparator.comparing(obj -> obj.dueDate));

            String numberOfErrorTasks = String.valueOf(tasksError.size());
            binding.textTasksListOtherNumber.setText(numberOfErrorTasks);
        }
    }

    private void loadUndoneTasksDetail() {
        tasksUndone = new ArrayList<>();
        if (!tasksAll.isEmpty()) {
            for (Tasks taskUndone : tasksAll) {
                if (taskUndone.status.equals("Unfinished")) {
                    tasksUndone.add(taskUndone);
                }
            }
            tasksUndone.sort(Comparator.comparing(obj -> obj.dueDate));

            String numberOfUndoneTasks = String.valueOf(tasksUndone.size());
            binding.textTasksListNotDoneNumber.setText(numberOfUndoneTasks);
        }
    }

    private void loadDoneTasksDetail() {
        tasksDone = new ArrayList<>();
        if (!tasksAll.isEmpty()) {
            for (Tasks taskDone : tasksAll) {
                if (taskDone.status.equals("Done")) {
                    tasksDone.add(taskDone);
                }
            }
            tasksDone.sort(Comparator.comparing(obj -> obj.dueDate));

            String numberOfDoneTasks = String.valueOf(tasksDone.size());
            binding.textTasksListDoneNumber.setText(numberOfDoneTasks);
        }
    }

    private TasksListAdapter getTasksListAdapter() {
        TasksListAdapter adapter = new TasksListAdapter(tasksAll, ProjectTasksActivity.this);

        adapter.setOnItemClickListener(new TasksListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Tasks task) {
                Intent intent = new Intent(ProjectTasksActivity.this, TaskDetailsActivity.class);
                intent.putExtra("putTaskForTaskDetails", task);
                intent.putExtra("putProjectForTaskDetails", currentProject);
                startActivityForResult(intent, KEY_RESULT_FOR_REFRESH_FROM_TASK);
            }
        });

        return adapter;
    }



    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerProjectTasksList.setVisibility(View.GONE);
            binding.progressBarTasksProject.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerProjectTasksList.setVisibility(View.VISIBLE);
            binding.progressBarTasksProject.setVisibility(View.GONE);
        }
    }

    private String getStatusCode(String statusCode) {
        if (statusCode != null) {
            switch (statusCode) {
                case Constants.KEY_TASK_STATUS_DONE: {
                    return "Done";
                }
                case Constants.KEY_TASK_STATUS_ERROR: {
                    return "Error Encountered";
                }
                default: {
                    return "Unfinished";
                }
            }
        } else {
            return "Unfinished";
        }
    }

    private void setListeners() {
        binding.buttonProjectAssignTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectTasksActivity.this, AssignMembersToTaskActivity.class);
                intent.putExtra("projectToAssignMembers", currentProject);
                startActivityForResult(intent, KEY_RESULT_FOR_REFRESH_LIST);
            }
        });
        binding.buttonBackFromTasksList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.buttonProjectTasksAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySelected(binding.buttonProjectTasksAll);
                loadTasksDetails();
                String message = "No Tasks For Now";
                binding.textNoTasks.setText(message);
            }
        });
        binding.buttonProjectTasksDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySelected(binding.buttonProjectTasksDone);
                TasksListAdapter doneAdapter = new TasksListAdapter(tasksDone, ProjectTasksActivity.this);
                binding.recyclerProjectTasksList.setAdapter(doneAdapter);

                doneAdapter.setOnItemClickListener(new TasksListAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Tasks task) {
                        Intent intent = new Intent(ProjectTasksActivity.this, TaskDetailsActivity.class);
                        intent.putExtra("putTaskForTaskDetails", task);
                        intent.putExtra("putProjectForTaskDetails", currentProject);
                        startActivityForResult(intent, KEY_RESULT_FOR_REFRESH_FROM_TASK);
                    }
                });

                isRecyclerLoading(false);
                if (tasksDone.isEmpty()) {
                    String message = "No Tasks Done Yet";
                    binding.textNoTasks.setText(message);
                    binding.imageNoTasksList.setVisibility(View.VISIBLE);
                } else {
                    doneAdapter.notifyDataSetChanged();
                    binding.imageNoTasksList.setVisibility(View.GONE);
                }
            }
        });
        binding.buttonProjectTasksNotDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySelected(binding.buttonProjectTasksNotDone);
                TasksListAdapter undoneAdapter = new TasksListAdapter(tasksUndone, ProjectTasksActivity.this);
                binding.recyclerProjectTasksList.setAdapter(undoneAdapter);

                undoneAdapter.setOnItemClickListener(new TasksListAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Tasks task) {
                        Intent intent = new Intent(ProjectTasksActivity.this, TaskDetailsActivity.class);
                        intent.putExtra("putTaskForTaskDetails", task);
                        intent.putExtra("putProjectForTaskDetails", currentProject);
                        startActivityForResult(intent, KEY_RESULT_FOR_REFRESH_FROM_TASK);
                    }
                });

                isRecyclerLoading(false);
                if (tasksUndone.isEmpty()) {
                    String message = "No Tasks Left Unfinished";
                    binding.textNoTasks.setText(message);
                    binding.imageNoTasksList.setVisibility(View.VISIBLE);
                } else {
                    undoneAdapter.notifyDataSetChanged();
                    binding.imageNoTasksList.setVisibility(View.GONE);
                }
            }
        });
        binding.buttonProjectTasksOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySelected(binding.buttonProjectTasksOther);
                TasksListAdapter errorAdapter = new TasksListAdapter(tasksError, ProjectTasksActivity.this);
                binding.recyclerProjectTasksList.setAdapter(errorAdapter);

                errorAdapter.setOnItemClickListener(new TasksListAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Tasks task) {
                        Intent intent = new Intent(ProjectTasksActivity.this, TaskDetailsActivity.class);
                        intent.putExtra("putTaskForTaskDetails", task);
                        intent.putExtra("putProjectForTaskDetails", currentProject);
                        startActivityForResult(intent, KEY_RESULT_FOR_REFRESH_FROM_TASK);
                    }
                });

                isRecyclerLoading(false);
                if (tasksError.isEmpty()) {
                    String message = "No Error Encountered";
                    binding.textNoTasks.setText(message);
                    binding.imageNoTasksList.setVisibility(View.VISIBLE);
                } else {
                    errorAdapter.notifyDataSetChanged();
                    binding.imageNoTasksList.setVisibility(View.GONE);
                }
            }
        });
    }

    private void querySelected(FrameLayout queryToBeSelected) {

        Context context = ProjectTasksActivity.this;

        ColorStateList selectedWholeBackgroundColor = ContextCompat.getColorStateList(context, R.color.mainGreen);
        ColorStateList selectedTitleColor = ContextCompat.getColorStateList(context, R.color.mainDark);
        ColorStateList selectedNumberBackgroundColor = ContextCompat.getColorStateList(context, R.color.mainDark);

        ColorStateList unselectedWholeBackgroundColor = ContextCompat.getColorStateList(context, R.color.unSelectedButtonBackground);
        ColorStateList unselectedTitleColor = ContextCompat.getColorStateList(context, R.color.whiteText);
        ColorStateList unselectedNumberBackgroundColor = ContextCompat.getColorStateList(context, R.color.grey);

        selectedQueryBackground.setBackgroundTintList(unselectedWholeBackgroundColor);
        selectedQueryTitle.setTextColor(unselectedTitleColor);
        selectedQueryBackgroundNumber.setBackgroundTintList(unselectedNumberBackgroundColor);

        if (queryToBeSelected == binding.buttonProjectTasksAll) {
            binding.buttonProjectTasksAll.setBackgroundTintList(selectedWholeBackgroundColor);
            binding.textTasksListAllTitle.setTextColor(selectedTitleColor);
            binding.backgroundTasksListAllNumber.setBackgroundTintList(selectedNumberBackgroundColor);

            selectedQueryBackground = binding.buttonProjectTasksAll;
            selectedQueryTitle = binding.textTasksListAllTitle;
            selectedQueryBackgroundNumber = binding.backgroundTasksListAllNumber;
        } else if (queryToBeSelected == binding.buttonProjectTasksDone) {
            binding.buttonProjectTasksDone.setBackgroundTintList(selectedWholeBackgroundColor);
            binding.textTasksListDoneTitle.setTextColor(selectedTitleColor);
            binding.backgroundTasksListDoneNumber.setBackgroundTintList(selectedNumberBackgroundColor);

            selectedQueryBackground = binding.buttonProjectTasksDone;
            selectedQueryTitle = binding.textTasksListDoneTitle;
            selectedQueryBackgroundNumber = binding.backgroundTasksListDoneNumber;
        } else if (queryToBeSelected == binding.buttonProjectTasksNotDone) {
            binding.buttonProjectTasksNotDone.setBackgroundTintList(selectedWholeBackgroundColor);
            binding.textTasksListNotDoneTitle.setTextColor(selectedTitleColor);
            binding.backgroundTasksListNotDoneNumber.setBackgroundTintList(selectedNumberBackgroundColor);

            selectedQueryBackground = binding.buttonProjectTasksNotDone;
            selectedQueryTitle = binding.textTasksListNotDoneTitle;
            selectedQueryBackgroundNumber = binding.backgroundTasksListNotDoneNumber;
        } else {
            binding.buttonProjectTasksOther.setBackgroundTintList(selectedWholeBackgroundColor);
            binding.textTasksListOtherTitle.setTextColor(selectedTitleColor);
            binding.backgroundTasksListOtherNumber.setBackgroundTintList(selectedNumberBackgroundColor);

            selectedQueryBackground = binding.buttonProjectTasksOther;
            selectedQueryTitle = binding.textTasksListOtherTitle;
            selectedQueryBackgroundNumber = binding.backgroundTasksListOtherNumber;
        }
    }

    private void init() {
        isRecyclerLoading(true);

        selectedQueryBackground = binding.buttonProjectTasksAll;
        selectedQueryTitle = binding.textTasksListAllTitle;
        selectedQueryBackgroundNumber = binding.backgroundTasksListAllNumber;
        db = FirebaseFirestore.getInstance();
        currentProject = (Project) getIntent().getSerializableExtra("projectForTasks");
        preferenceManager = new PreferenceManager(ProjectTasksActivity.this);
        tasksAll = new ArrayList<>();
        tasksDone = new ArrayList<>();
        tasksUndone = new ArrayList<>();
        tasksError = new ArrayList<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_RESULT_FOR_REFRESH_LIST) {
            if (resultCode == RESULT_OK) {
                loadTasksDetails();
            }
        } else if (requestCode == KEY_RESULT_FOR_REFRESH_FROM_TASK) {
            if (resultCode == RESULT_OK) {
                loadTasksDetails();
            }
        }
    }
}