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

import com.bumptech.glide.Glide;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.Model.Announcement;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementHomeListAdapter extends RecyclerView.Adapter<AnnouncementHomeListAdapter.ViewHolder>{
    private ArrayList<Announcement> announcements;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private FirebaseStorage storage;
    private StorageReference profileImageReference;
    private FirebaseFirestore db;

    public AnnouncementHomeListAdapter(ArrayList<Announcement> announcements, Context context) {
        this.announcements = announcements;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.announcement_home_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        if (position != 0 && announcements.get(position).projectId.equals(
                announcements.get(position - 1).projectId)){
            holder.viewDivider.setVisibility(View.GONE);
            holder.projectName.setVisibility(View.GONE);
        } else {
            holder.viewDivider.setVisibility(View.VISIBLE);
            holder.projectName.setVisibility(View.VISIBLE);
        }

        holder.announcerName.setText(announcements.get(position).announcerName);
        holder.title.setText(announcements.get(position).title);
        holder.body.setText(announcements.get(position).body);
        holder.date.setText(announcements.get(position).dateString());

        setAnnouncerImage(holder, announcements.get(position).announcerId);
        setProjectName(holder, announcements.get(position).projectId);
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    private void initDefine() {
        storage = FirebaseStorage.getInstance();
        profileImageReference = storage.getReference().child(Constants.KEY_USER_LIST + "/");
        db = FirebaseFirestore.getInstance();
    }

    private void setProjectName(ViewHolder holder, String projectId) {
        db.collection(Constants.KEY_PROJECT_LISTS).document(projectId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                                holder.projectName.setText(project.projectName);

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

    private void setAnnouncerImage(ViewHolder holder, String userId) {
        profileImageReference.child(userId + "/" + Constants.KEY_PROFILE_IMAGE).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.profileImage);
                    }
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, announcerName, title, body, date;
        ImageView profileImage;
        View viewDivider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            projectName = itemView.findViewById(R.id.textAnnouncementProjectNameHome);
            announcerName = itemView.findViewById(R.id.textAnnouncementAnnouncerNameHome);
            title = itemView.findViewById(R.id.textAnnouncementTitleHome);
            body = itemView.findViewById(R.id.textAnnouncementDescHome);
            date = itemView.findViewById(R.id.textAnnouncementDateHome);
            profileImage = itemView.findViewById(R.id.imageProfileAnnouncementHome);
            viewDivider = itemView.findViewById(R.id.viewDividerInAnnouncementHome);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
