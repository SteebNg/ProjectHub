package com.capstone.projecthub.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Listeners.ProjectListListener;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.databinding.ProjectListItemBinding;

import java.util.ArrayList;

public class ProjectListsAdapter extends RecyclerView.Adapter<ProjectListsAdapter.ProjectListsViewHolder> {

    private ArrayList<Project> projectArrayList;
    private ProjectListListener projectListListener;

    public ProjectListsAdapter(ArrayList<Project> projects, ProjectListListener projectListListener) {
        projectArrayList = projects;
        this.projectListListener = projectListListener;
    }

    @NonNull
    @Override
    public ProjectListsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProjectListsViewHolder(
                ProjectListItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectListsViewHolder holder, int position) {
        holder.setData(projectArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return projectArrayList.size();
    }

    public class ProjectListsViewHolder extends RecyclerView.ViewHolder {
        ProjectListItemBinding binding;

        ProjectListsViewHolder(ProjectListItemBinding projectListItemBinding) {
            super(projectListItemBinding.getRoot());
            binding = projectListItemBinding;
        }

        void setData(Project project) {
            binding.textProjectListsName.setText(project.projectName);
            binding.textProjectDescLists.setText(project.projectDescription);
            binding.textDueDateLists.setText(project.dateString());

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    projectListListener.onProjectClicked(project);
                }
            });
        }
    }
}
