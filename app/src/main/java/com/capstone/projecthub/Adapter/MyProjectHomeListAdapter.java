package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyProjectHomeListAdapter extends RecyclerView.Adapter<MyProjectHomeListAdapter.ViewHolder>{
    private ArrayList<Project> projects;
    private Context context;
    private FirebaseFirestore db;
    private OnItemClickListener onItemClickListener;

    public MyProjectHomeListAdapter(ArrayList<Project> projects, Context context) {
        this.projects = projects;
        this.context = context;
    }

    private void initDefine() {
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_project_home_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        holder.projectName.setText(projects.get(position).projectName);
        holder.date.setText(projects.get(position).dateString());

        setProgress(holder, projects.get(position));

        holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(projects.get(position)));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    private void setProgress(ViewHolder holder, Project project) {
        db.collection(Constants.KEY_TASKS_LIST).whereEqualTo(Constants.KEY_PROJECT_ID, project.projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            int numberOfDoneTasks = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getString(Constants.KEY_TASK_STATUS).equals(Constants.KEY_TASK_STATUS_DONE)) {
                                    numberOfDoneTasks++;
                                }
                            }
                            int progress = (int) (((double) numberOfDoneTasks / (double) task.getResult().size()) * 100.0);
                            String progressPercentString = progress + "%";
                            holder.progressPercent.setText(progressPercentString);
                            holder.progressBar.setProgress(progress);
                        }
                    }
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, projectName, progressPercent;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.textDateProjectHomeList);
            projectName = itemView.findViewById(R.id.textProjectNameHomeList);
            progressPercent = itemView.findViewById(R.id.textProjectProgressNumberHomeList);
            progressBar = itemView.findViewById(R.id.progressBarProjectHomeList);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
