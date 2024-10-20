package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.R;

import java.util.ArrayList;

public class ProjectListsAdapter extends RecyclerView.Adapter<ProjectListsAdapter.ViewHolder>{

    private ArrayList<Project> projects;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ProjectListsAdapter(Context context, ArrayList<Project> projects) {
        this.context = context;
        this.projects = projects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.project_list_item,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.projectName.setText(projects.get(position).projectName);
        holder.projectDesc.setText(projects.get(position).projectDescription);
        String dateDisplay = "Due: " + projects.get(position).dateString();
        holder.projectDate.setText(dateDisplay);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(projects.get(position)));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, projectDesc, projectDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.textProjectListsName);
            projectDesc = itemView.findViewById(R.id.textProjectDescLists);
            projectDate = itemView.findViewById(R.id.textDueDateLists);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
