package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ProjectListsAdapter extends RecyclerView.Adapter<ProjectListsAdapter.ViewHolder>{

    private ArrayList<Project> projects;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private FirebaseFirestore db;

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

        ColorStateList color;
        switch (projects.get(position).projectColor) {
            case "0": {
                color = ContextCompat.getColorStateList(context, R.color.mainGreen);
                break;
            }
            case "1": {
                color = ContextCompat.getColorStateList(context, R.color.red);
                break;
            }
            case "2": {
                color = ContextCompat.getColorStateList(context, R.color.yellow);
                break;
            }
            default: {
                color = ContextCompat.getColorStateList(context, R.color.lightBlue);
                break;
            }
        }
        holder.bulletProjects.setBackgroundTintList(color);

        setProgress(holder, projects.get(position));

        holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(projects.get(position)));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    private void setProgress(ViewHolder holder, Project project) {
        db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_TASKS_LIST)
                .whereEqualTo(Constants.KEY_PROJECT_ID, project.projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            int tasksDone = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getString(Constants.KEY_TASK_STATUS).equals(Constants.KEY_TASK_STATUS_DONE)) {
                                    tasksDone++;
                                }
                            }
                            int progress = (int) (((double) tasksDone / (double) task.getResult().size()) * 100.0);
                            String progressPercentString = progress + "%";
                            holder.progressPercent.setText(progressPercentString);
                            holder.progressBar.setProgress(progress);
                        }
                    }
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, projectDesc, projectDate, progressPercent;
        View bulletProjects;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.textProjectListsName);
            projectDesc = itemView.findViewById(R.id.textProjectDescLists);
            projectDate = itemView.findViewById(R.id.textDueDateLists);
            bulletProjects = itemView.findViewById(R.id.viewBulletProjectList);
            progressBar = itemView.findViewById(R.id.progressBarProjectListsItem);
            progressPercent = itemView.findViewById(R.id.textProgressPercentProjectList);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
