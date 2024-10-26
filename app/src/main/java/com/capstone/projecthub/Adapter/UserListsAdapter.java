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
import com.capstone.projecthub.Model.User;
import com.capstone.projecthub.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UserListsAdapter extends RecyclerView.Adapter<UserListsAdapter.ViewHolder>{
    private ArrayList<User> users;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private FirebaseStorage storage;
    private StorageReference referenceToProfilePicture;

    public UserListsAdapter(ArrayList<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        holder.username.setText(users.get(position).username);
        holder.userEmail.setText(users.get(position).email);

        //getImage
        referenceToProfilePicture.child(
                        users.get(position).userId + "/" + Constants.KEY_PROFILE_IMAGE)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (uri != null) {
                            Glide.with(context).load(uri).into(holder.userImage);
                        }
                    }
                });
        holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(users.get(position)));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void initDefine() {
        storage = FirebaseStorage.getInstance();
        referenceToProfilePicture = storage.getReference().child(Constants.KEY_USER_LIST + "/");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, userEmail;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.textProfileNameUserList);
            userEmail = itemView.findViewById(R.id.textProfileEmailUserList);
            userImage = itemView.findViewById(R.id.imageProfileUserList);
        }
    }

    public void setFilteredUserList(ArrayList<User> filteredUserList) {
        users = filteredUserList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(User user);
    }
}
