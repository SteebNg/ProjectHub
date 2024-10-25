package com.capstone.projecthub.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.projecthub.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersListAdapter extends RecyclerView.Adapter<MembersListAdapter.ViewHolder>{
    private ArrayList<String> membersId;
    private Context context;
    private String projectId;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference pathForProfileImage;
    private OnItemClickListener onItemClickListener;
    private PreferenceManager preferenceManager;

    public MembersListAdapter(Context context, ArrayList<String> membersId, String projectId) {
        this.membersId = membersId;
        this.context = context;
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.members_list_item,
                parent,
                false);

        return new MembersListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        db.collection(Constants.KEY_USER_LIST)
                .document(membersId.get(position))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            holder.memberName.setText(document.getString(Constants.KEY_USERNAME));
                            holder.memberEmail.setText(document.getString(Constants.KEY_EMAIL));

                            //image
                            pathForProfileImage.child(
                                    Constants.KEY_USER_LIST
                                    + "/"
                                    + membersId.get(position)
                                    + "/"
                                    + Constants.KEY_PROFILE_IMAGE
                            ).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(context).load(uri).into(holder.memberImage);
                                }
                            });

                            if (document.getString(Constants.KEY_USER_ID).equals(
                                    preferenceManager.getString(Constants.KEY_USER_ID))) {
                                holder.leaderIcon.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        //check if the user is leader and not himself
        //Yea i have no idea how to make this readable
        //Even if i know, i dont quite dare to touch this as it is an adapter class
        //Yea yea i messed around quite a number of recycler view adapters and i still dont dare to touch it
        //If it works, it works
        //Just know that it checks for the current user if it is leader of this project
        //if the user is, then set the settings button visible for the leader
        //It also checks if the membersId(position) is the same as the user
        //If the user ID is the same as the membersID(position), dont show the settings button
        db.collection(Constants.KEY_PROJECT_LISTS)
                .document(projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            if (document.getString(Constants.KEY_PROJECT_LEADER)
                                    .equals(preferenceManager.getString(Constants.KEY_USER_ID))
                                    && !membersId.get(position).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                holder.memberSetting.setOnClickListener(v -> {
                                    PopupMenu popupMenu = new PopupMenu(context, holder.memberSetting);
                                    popupMenu.getMenuInflater().inflate(R.menu.members_options_menu, popupMenu.getMenu());
                                    popupMenu.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) item -> {
                                        int selectedItemId = item.getItemId();
                                        String memberId = membersId.get(position);
                                        Dialog dialog = new Dialog(context);

                                        if (selectedItemId == R.id.kickMember) {
                                            dialog.setContentView(R.layout.confirm_remove_members_dialog);
                                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            dialog.setCancelable(false);
                                            dialog.show();

                                            Button cancelButton = dialog.findViewById(R.id.buttonCancelKick);
                                            Button confirmButton = dialog.findViewById(R.id.buttonConfirmKick);

                                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });

                                            confirmButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Map<String, Object> removeUserFromProject = new HashMap<>();
                                                    removeUserFromProject.put(Constants.KEY_PROJECT_MEMBERS_ID, FieldValue.arrayRemove(memberId));

                                                    db.collection(Constants.KEY_PROJECT_LISTS)
                                                            .document(projectId)
                                                            .update(removeUserFromProject)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(context, "Member removed", Toast.LENGTH_SHORT).show();
                                                                    dialog.dismiss();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                        } else {
                                            dialog.setContentView(R.layout.confirm_transfer_leader_dialog);
                                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            dialog.setCancelable(false);
                                            dialog.show();

                                            Button cancelButton = dialog.findViewById(R.id.buttonCancelTransfer);
                                            Button confirmButton = dialog.findViewById(R.id.buttonConfirmTransfer);

                                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                }
                                            });

                                            confirmButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    db.collection(Constants.KEY_PROJECT_LISTS)
                                                            .document(projectId)
                                                            .update(Constants.KEY_PROJECT_LEADER, memberId)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(context, "Transfer Successful", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context, "Transfer error", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                        return true;
                                    });
                                    popupMenu.show();
                                });
                            } else {
                                holder.memberSetting.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return membersId.size();
    }

    private void initDefine() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        pathForProfileImage = storage.getReference();
        preferenceManager = new PreferenceManager(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberName, memberEmail;
        ImageView memberImage, memberSetting, leaderIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.textProfileNameMembersList);
            memberEmail = itemView.findViewById(R.id.textProfileEmailMembersList);
            memberImage = itemView.findViewById(R.id.imageProfileImageMembersList);
            memberSetting = itemView.findViewById(R.id.buttonMemberOption);
            leaderIcon = itemView.findViewById(R.id.imageLeaderMemberList);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(String memberId);
    }
}
