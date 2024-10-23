package com.capstone.projecthub.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.projecthub.Constants;
import com.capstone.projecthub.Model.Announcement;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AnnouncementListAdapter extends RecyclerView.Adapter<AnnouncementListAdapter.ViewHolder>{
    private ArrayList<Announcement> announcements;
    private Context context;
    private FirebaseStorage storage;
    private StorageReference profileImageReference;

    public AnnouncementListAdapter(Context context, ArrayList<Announcement> announcements) {
        this.context = context;
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.announcement_list_item,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.announcerName.setText(announcements.get(position).announcerName);
        holder.announcementTitle.setText(announcements.get(position).title);
        holder.announcementBody.setText(announcements.get(position).body);

        String displayDate = "Posted on " + announcements.get(position).dateString();
        holder.announcementDate.setText(displayDate);

        storage = FirebaseStorage.getInstance();
        profileImageReference = storage.getReference().child(
                Constants.KEY_USER_LIST
                + "/"
                + announcements.get(position).announcerId
                + "/"
                + Constants.KEY_PROFILE_IMAGE
        );

        profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
                    Glide.with(context).load(uri).into(holder.announcerImage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to fetch profile image", Toast.LENGTH_SHORT).show();
            }
        });

        //set onClickListener here if want
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView announcerName, announcementDate, announcementTitle, announcementBody;
        ImageView announcerImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            announcerName = itemView.findViewById(R.id.announcerName);
            announcementDate = itemView.findViewById(R.id.textDateAnnouncement);
            announcementTitle = itemView.findViewById(R.id.textAnnouncementTitle);
            announcementBody = itemView.findViewById(R.id.textAnnouncementBody);
            announcerImage = itemView.findViewById(R.id.imageProfileAnnouncement);
        }
    }
}
