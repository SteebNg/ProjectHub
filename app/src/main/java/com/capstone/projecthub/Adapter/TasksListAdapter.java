package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.R;

import java.util.ArrayList;
import java.util.Date;

public class TasksListAdapter extends RecyclerView.Adapter<TasksListAdapter.ViewHolder>{
    private ArrayList<Tasks> tasks;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public TasksListAdapter(ArrayList<Tasks> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.project_tasks_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.taskName.setText(tasks.get(position).tasksName);
        holder.taskDesc.setText(tasks.get(position).tasksDesc);
        holder.dueDate.setText(tasks.get(position).dateStringDueDate());
        holder.groupDate.setText(tasks.get(position).dateStringDueDate());

        String status = tasks.get(position).status;

        if (status.equals("Done")) {
            holder.doneOrNot.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_done_24));
            holder.backgroundStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.mainGreen));
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskDesc.setPaintFlags(holder.taskDesc.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else if (status.equals("Unfinished")){
            holder.doneOrNot.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_edit_24));
            holder.backgroundStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskDesc.setPaintFlags(holder.taskDesc.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        } else {
            holder.doneOrNot.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.danger));
            holder.backgroundStatus.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskDesc.setPaintFlags(holder.taskDesc.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        Date groupDate = tasks.get(position).dueDate;
        if (position != 0 && tasks.get(position - 1).dueDate.equals(groupDate)) {
            holder.groupDateView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(tasks.get(position)));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDesc, dueDate, groupDate;
        FrameLayout groupDateView;
        ImageView doneOrNot;
        View backgroundStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.textProjectTasksNameLists);
            taskDesc = itemView.findViewById(R.id.textProjectTasksDescLists);
            dueDate = itemView.findViewById(R.id.textProjectTasksDueDateLists);
            doneOrNot = itemView.findViewById(R.id.imageTasksDoneLists);
            groupDateView = itemView.findViewById(R.id.dateTasksItemListView);
            groupDate = itemView.findViewById(R.id.dateTasksItemList);
            backgroundStatus = itemView.findViewById(R.id.backgroundTasksDoneLists);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Tasks task);
    }
}
