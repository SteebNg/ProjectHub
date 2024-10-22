package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.projecthub.Model.File;
import com.capstone.projecthub.R;

import java.util.ArrayList;

public class FileSharingListsAdapter extends RecyclerView.Adapter<FileSharingListsAdapter.ViewHolder>{
    private Context context;
    private ArrayList<File> files;
    private OnItemClickListener onItemClickListener;

    public FileSharingListsAdapter(Context context, ArrayList<File> files) {
        this.context = context;
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_sharing_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fileName.setText(files.get(position).fileName);
        holder.downloadFile.setOnClickListener(v -> onItemClickListener.onClick(files.get(position)));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView fileImage, downloadFile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.textFileName);
            fileImage = itemView.findViewById(R.id.imageFileSharing);
            downloadFile = itemView.findViewById(R.id.buttonDownloadFile);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(File file);
    }
}
