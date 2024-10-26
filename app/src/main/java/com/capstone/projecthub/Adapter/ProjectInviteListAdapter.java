package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ProjectInviteListAdapter extends RecyclerView.Adapter<ProjectInviteListAdapter.ViewHolder>{
    private ArrayList<Project> projectsInvited;
    private Context context;
    private FirebaseFirestore db;
    private StorageReference leaderProfileImageReference;
    private OnItemClickListener onRejectClickListener;
    private OnItemClickListener onAcceptClickListener;

    public ProjectInviteListAdapter(ArrayList<Project> projectsInvited, Context context) {
        this.projectsInvited = projectsInvited;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.project_invitation_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        holder.projectName.setText(projectsInvited.get(position).projectName);
        holder.projectDesc.setText(projectsInvited.get(position).projectDescription);

        String dueDateSentence = "Due Date: " + projectsInvited.get(position).dateString();
        holder.projectDueDate.setText(dueDateSentence);

        setLeaderNameSentence(holder, projectsInvited.get(position).projectLeaderId);
        setLeaderProfileImage(holder, projectsInvited.get(position).projectLeaderId);

        holder.buttonAccept.setOnClickListener(v -> onAcceptClickListener.onClick(projectsInvited.get(position)));
        holder.buttonReject.setOnClickListener(v -> onRejectClickListener.onClick(projectsInvited.get(position)));
    }

    @Override
    public int getItemCount() {
        return projectsInvited.size();
    }

    private void setLeaderProfileImage(ViewHolder holder, String leaderId) {
        leaderProfileImageReference.child(leaderId + "/" + Constants.KEY_PROFILE_IMAGE)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.leaderImage);
                    }
                });
    }

    private void setLeaderNameSentence(ViewHolder holder, String leaderId) {
        db.collection(Constants.KEY_USER_LIST).document(leaderId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String invitationSentence, leaderName;
                                leaderName = document.getString(Constants.KEY_USERNAME);
                                invitationSentence = leaderName + " invites you to:";
                                holder.leaderInviteSentence.setText(invitationSentence);
                            }
                        }
                    }
                });
    }

    private void initDefine() {
        db = FirebaseFirestore.getInstance();
        leaderProfileImageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_USER_LIST + "/");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView leaderInviteSentence, projectName, projectDesc, projectDueDate;
        ImageView leaderImage;
        FrameLayout buttonAccept, buttonReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leaderInviteSentence = itemView.findViewById(R.id.textProjectInvitationLeaderSentence);
            projectName = itemView.findViewById(R.id.textProjectInvitationName);
            projectDesc = itemView.findViewById(R.id.textProjectInvitationBody);
            projectDueDate = itemView.findViewById(R.id.textProjectInvitationDueDate);
            leaderImage = itemView.findViewById(R.id.imageProjectInviteLeaderImage);
            buttonAccept = itemView.findViewById(R.id.buttonAcceptInvitation);
            buttonReject = itemView.findViewById(R.id.buttonRejectInvitation);
        }
    }

    public void setOnRejectClickListener(OnItemClickListener onRejectClickListener) {
        this.onRejectClickListener = onRejectClickListener;
    }

    public void setOnAcceptClickListener(OnItemClickListener onAcceptClickListener) {
        this.onAcceptClickListener = onAcceptClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Project project);
    }
}
