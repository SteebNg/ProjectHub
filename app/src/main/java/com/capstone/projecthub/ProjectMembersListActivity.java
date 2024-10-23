package com.capstone.projecthub;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Adapter.MembersListAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityProjectMembersListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProjectMembersListActivity extends AppCompatActivity {

    private ActivityProjectMembersListBinding binding;
    private Project currentProject;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectMembersListBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        loadMembersList();
        updateViewForLeader();
    }

    private void updateViewForLeader() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        ArrayList<String> membersList = new ArrayList<>(Arrays.asList(currentProject.memberList));

        if (!currentProject.projectLeaderId.equals(userId)) {
            binding.buttonAddMembers.setVisibility(View.GONE);
        }
    }

    private void loadMembersList() {
        ArrayList<String> membersId = new ArrayList<>(Arrays.asList(currentProject.memberList));

        MembersListAdapter adapter = getMembersListAdapter(membersId);
        binding.recyclerProjectMembersList.setAdapter(adapter);

        membersId.sort(String::compareToIgnoreCase); //alphabetical order
        adapter.notifyDataSetChanged();
        isListLoading(false);
    }

    private MembersListAdapter getMembersListAdapter(ArrayList<String> membersId) {

        return new MembersListAdapter(this, membersId, currentProject.projectId);
    }

    private void init() {
        isListLoading(true);

        currentProject = (Project) getIntent().getSerializableExtra("Project for members list");
        preferenceManager = new PreferenceManager(ProjectMembersListActivity.this);
        db = FirebaseFirestore.getInstance();
    }

    private void isListLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerProjectMembersList.setVisibility(View.GONE);
            binding.progressBarMembersList.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerProjectMembersList.setVisibility(View.VISIBLE);
            binding.progressBarMembersList.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        binding.buttonLeaveProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prompt a dialog box confirm
                Dialog dialog = new Dialog(ProjectMembersListActivity.this);
                dialog.setContentView(R.layout.confirm_leave_project_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);
                dialog.show();

                Button cancelButton = dialog.findViewById(R.id.buttonCancelLeave);
                Button confirmButton = dialog.findViewById(R.id.buttonConfirmLeaveProject);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //(TODO) Remove user from the project
                        removeUserFromDatabase();

                        dialog.dismiss();
                    }
                });
            }
        });
        binding.buttonBackFromMembersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        binding.buttonRefreshMembersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isListLoading(true);
                loadMembersList();
            }
        });
        binding.buttonAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectMembersListActivity.this, AddMembersActivity.class);
                intent.putExtra("project", currentProject);
                startActivity(intent);
            }
        });
    }

    private void removeUserFromDatabase() {
        Map<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_PROJECT_MEMBERS_ID, FieldValue.arrayRemove(
                preferenceManager.getString(Constants.KEY_USER_ID)
        ));

        db.collection(Constants.KEY_PROJECT_LISTS)
                .document(currentProject.projectId)
                .update(update)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProjectMembersListActivity.this
                                    , "Leave Successful"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProjectMembersListActivity.this
                                , "An error occurred"
                                , Toast.LENGTH_SHORT).show();
                    }
                });

        Intent intent = new Intent();
        intent.putExtra("quit", "true");
        setResult(RESULT_OK, intent);
        finish();
    }
}