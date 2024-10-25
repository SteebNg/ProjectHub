package com.capstone.projecthub.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.projecthub.Constants;
import com.capstone.projecthub.Model.User;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class TaskDetailsMembersAdapter extends RecyclerView.Adapter<TaskDetailsMembersAdapter.ViewHolder>{
    private ArrayList<User> assignedMembers;
    private Context context;
    private FirebaseStorage storage;
    private StorageReference profileImageReference;

    public TaskDetailsMembersAdapter(ArrayList<User> assignedMembers, Context context) {
        this.assignedMembers = assignedMembers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.members_list_item,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        holder.username.setText(assignedMembers.get(position).username);
        holder.userEmail.setText(assignedMembers.get(position).email);

        profileImageReference.child(assignedMembers.get(position).userId + "/" + Constants.KEY_PROFILE_IMAGE)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.userProfileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //(TODO) Set a default pfp
                    }
                });

        holder.memberOption.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return assignedMembers.size();
    }

    private void initDefine() {
        storage = FirebaseStorage.getInstance();
        profileImageReference = storage.getReference().child(Constants.KEY_USER_LIST + "/");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, userEmail;
        ImageView userProfileImage, memberOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textProfileNameMembersList);
            userEmail = itemView.findViewById(R.id.textProfileEmailMembersList);
            userProfileImage = itemView.findViewById(R.id.imageProfileImageMembersList);
            memberOption = itemView.findViewById(R.id.buttonMemberOption);
        }
    }
}
