package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTaskHomeListAdapter extends RecyclerView.Adapter<MyTaskHomeListAdapter.ViewHolder>{
    private ArrayList<Tasks> tasks;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private Project project;
    private FirebaseFirestore db;

    public MyTaskHomeListAdapter(ArrayList<Tasks> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.taskName.setText(tasks.get(position).tasksName);
        String dateSentence = " by " + tasks.get(position).dateStringDueDate();
        holder.taskDueDate.setText(dateSentence);

        setTaskStatus(holder, tasks.get(position));

        findProject(holder, tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void findProject(ViewHolder holder, Tasks currentTask) {
        db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_PROJECT_LISTS).document(currentTask.projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Project project = new Project();
                                project.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                project.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                project.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);
                                project.projectId = document.getId();
                                project.projectImage = document.getString(Constants.KEY_PROJECT_IMAGE);
                                project.projectLeaderId = document.getString(Constants.KEY_PROJECT_LEADER);
                                project.memberList = ((List<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID)).toArray(new String[0]);
                                project.projectColor = document.getString(Constants.KEY_PROJECT_COLOR);

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

    private void setTaskStatus(ViewHolder holder, Tasks task) {
        String status = task.status;
        switch (status) {
            case "Done": {
                holder.bgTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.mainGreen));
                holder.imageTaskStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_done_24));
                holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.taskDueDate.setPaintFlags(holder.taskDueDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                break;
            } case "Unfinished": {
                holder.bgTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
                holder.imageTaskStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_edit_24));
                holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.taskDueDate.setPaintFlags(holder.taskDueDate.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                break;
            } default: {
                holder.bgTaskStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
                holder.imageTaskStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.danger));
                holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.taskDueDate.setPaintFlags(holder.taskDueDate.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDueDate;
        View bgTaskStatus;
        ImageView imageTaskStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.textProjectTaskNameHome);
            taskDueDate = itemView.findViewById(R.id.textProjectTaskDateHome);
            bgTaskStatus = itemView.findViewById(R.id.backgroundTasksHome);
            imageTaskStatus = itemView.findViewById(R.id.imageTaskHome);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
