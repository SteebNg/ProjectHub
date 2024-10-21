package com.capstone.projecthub;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Adapter.MembersListAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.databinding.ActivityProjectMembersListBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ProjectMembersListActivity extends AppCompatActivity {

    private ActivityProjectMembersListBinding binding;
    private Project currentProject;

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
    }

    private void loadMembersList() {
        ArrayList<String> membersId = new ArrayList<>(Arrays.asList(currentProject.memberList));

        MembersListAdapter adapter = getMembersListAdapter(membersId);
        binding.recyclerProjectMembersList.setAdapter(adapter);

        isListLoading(false);
        membersId.sort(String::compareToIgnoreCase); //alphabetical order
        adapter.notifyDataSetChanged();
    }

    private MembersListAdapter getMembersListAdapter(ArrayList<String> membersId) {

        return new MembersListAdapter(this, membersId, currentProject.projectId);
    }

    private void init() {
        isListLoading(true);

        currentProject = (Project) getIntent().getSerializableExtra("Project for members list");
    }

    private void isListLoading(boolean isLoading) {
        if (isLoading) {
            binding.buttonLeaveProject.setVisibility(View.GONE);
            binding.recyclerProjectMembersList.setVisibility(View.GONE);
            binding.progressBarMembersList.setVisibility(View.VISIBLE);
        } else {
            binding.buttonLeaveProject.setVisibility(View.VISIBLE);
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
                        dialog.dismiss();

                        Intent intent = new Intent();
                        intent.putExtra("quit", "true");
                        setResult(RESULT_OK, intent);
                        finish();
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
    }
}