package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TaskHomeListAdapter extends RecyclerView.Adapter<TaskHomeListAdapter.ViewHolder>{
    private ArrayList<Tasks> tasks;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private FirebaseFirestore db;

    public TaskHomeListAdapter(ArrayList<Tasks> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_home_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        setTaskStatus(holder, position);

        holder.taskName.setText(tasks.get(position).tasksName);
        holder.taskDesc.setText(tasks.get(position).tasksDesc);
        holder.taskDueDate.setText(tasks.get(position).dateStringDueDate());

        db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_PROJECT_LISTS).document(tasks.get(position).projectId)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Project project = new Project();
                                project.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                project.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                project.projectId = document.getId();
                                project.projectLeaderId = document.getString(Constants.KEY_PROJECT_LEADER);
                                project.projectImage = document.getString(Constants.KEY_PROJECT_IMAGE);
                                project.projectColor = document.getString(Constants.KEY_PROJECT_COLOR);
                                project.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);
                                project.memberList = ((List<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID)).toArray(new String[0]);

                                holder.projectName.setText(project.projectName);
                                setProjectColor(holder, project);

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onItemClickListener.onClick(project);
                                    }
                                });
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void setProjectColor(ViewHolder holder, Project project) {
        String projectColor = project.projectColor;
        ColorStateList color = ContextCompat.getColorStateList(context, R.color.lightBlue);
        switch (projectColor) {
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
        holder.projectTheme.setBackgroundTintList(color);
    }

    private void setTaskStatus(ViewHolder holder, int position) {
        String taskStatus = tasks.get(position).status;
        if (taskStatus.equals(Constants.KEY_TASK_STATUS_DONE)) {
            holder.bgTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.mainGreen));
            holder.taskStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_done_24));
        } else if (taskStatus.equals(Constants.KEY_TASK_STATUS_UNDONE)){
            holder.bgTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
            holder.taskStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_edit_24));
        } else {
            holder.bgTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
            holder.taskStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.danger));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, taskName, taskDesc, taskDueDate;
        View projectTheme, bgTaskStatus;
        ImageView taskStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.projectNameTasksItemListHome);
            taskName = itemView.findViewById(R.id.textTasksNameListsHome);
            taskDesc = itemView.findViewById(R.id.textTasksDescListsHome);
            taskDueDate = itemView.findViewById(R.id.textTasksDueDateListsHome);
            projectTheme = itemView.findViewById(R.id.viewBulletAnnouncement);
            bgTaskStatus = itemView.findViewById(R.id.backgroundTasksDoneListsHome);
            taskStatus = itemView.findViewById(R.id.imageTasksDoneListsHome);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
