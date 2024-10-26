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

public class UserYetToAddListsAdapter extends RecyclerView.Adapter<UserYetToAddListsAdapter.ViewHolder>{
    private ArrayList<User> users;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private FirebaseStorage storage;
    private StorageReference profileImageReference;

    public UserYetToAddListsAdapter(ArrayList<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.yet_to_add_user_item_list,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        initDefine();

        holder.username.setText(users.get(position).username);
        profileImageReference.child(users.get(position).userId + "/" + Constants.KEY_PROFILE_IMAGE)
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
        profileImageReference = storage.getReference().child(Constants.KEY_USER_LIST + "/");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textProfileNameUserListYetToAdd);
            userImage = itemView.findViewById(R.id.imageProfileUserListYetToAdd);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(User user);
    }
}
