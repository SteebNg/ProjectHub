package com.capstone.projecthub;

import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Adapter.UserListsAdapter;
import com.capstone.projecthub.Adapter.UserYetToAddListsAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.User;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.databinding.ActivityAddMembersBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AddMembersActivity extends AppCompatActivity {

    private ActivityAddMembersBinding binding;
    private ArrayList<String> membersAlreadyAddedList;
    private FirebaseFirestore db;
    private ArrayList<User> users;
    private ArrayList<User> addedUsers;
    private UserListsAdapter userListsAdapter;
    private UserYetToAddListsAdapter userYetToAddListsAdapter;
    private Project project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMembersBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        updateUserList();
    }

    private void updateUserList() {
        db.collection(Constants.KEY_USER_LIST)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            isRecyclerLoading(false);

                            users = new ArrayList<>();
                            userListsAdapter = getUserListAdapter(users);
                            binding.recyclerAddMembersList.setAdapter(userListsAdapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!membersAlreadyAddedList.contains(document.getId())) {
                                    User user = new User();
                                    user.userId = document.getId();
                                    user.username = document.getString(Constants.KEY_USERNAME);
                                    user.email = document.getString(Constants.KEY_EMAIL);

                                    users.add(user);
                                }
                            }
                            users.sort(userComparator);
                            userListsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AddMembersActivity.this, "Error getting users",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private final Comparator<User> userComparator = new Comparator<User>() {
        @Override
        public int compare(User o1, User o2) {
            return o1.username.compareTo(o2.username);
        }
    };

    private UserListsAdapter getUserListAdapter(ArrayList<User> users) {
        UserListsAdapter adapter = new UserListsAdapter(users, AddMembersActivity.this);

        adapter.setOnItemClickListener(new UserListsAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                int position = users.indexOf(user);
                users.remove(user);
                users.sort(userComparator);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, users.size());

                //add the user to another recyclerView
                updateAddedMemberList(user);
            }
        });

        return adapter;
    }

    private void updateAddedMemberList(User user) {

        userYetToAddListsAdapter = getUserYetToAddAdapter(addedUsers);
        binding.recyclerYetToAddMembers.setAdapter(userYetToAddListsAdapter);

        addedUsers.add(user);
        userYetToAddListsAdapter.notifyDataSetChanged();

        if (addedUsers.isEmpty()) {
            binding.recyclerYetToAddMembers.setVisibility(View.GONE);
        } else {
            binding.recyclerYetToAddMembers.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserListRecycler(User user) {
        userListsAdapter = getUserListAdapter(users);
        binding.recyclerAddMembersList.setAdapter(userListsAdapter);

        users.add(user);

        users.sort(userComparator);
        userListsAdapter.notifyDataSetChanged();
    }

    private UserYetToAddListsAdapter getUserYetToAddAdapter(ArrayList<User> addedUsers) {
        UserYetToAddListsAdapter adapter = new UserYetToAddListsAdapter(addedUsers, AddMembersActivity.this);

        adapter.setOnItemClickListener(new UserYetToAddListsAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                int position = addedUsers.indexOf(user);
                addedUsers.remove(user);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, addedUsers.size());

                updateUserListRecycler(user);
            }
        });

        return adapter;
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerAddMembersList.setVisibility(View.GONE);
            binding.progressBarAddMembersList.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerAddMembersList.setVisibility(View.VISIBLE);
            binding.progressBarAddMembersList.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        binding.buttonBackFromAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.buttonAddMembersToProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //have to convert to normal array cause for some gawd dam reason firestore thinks
                //that ArrayList<String> is a nested array like, bro????????????????
                String[] addedUsersId = new String[addedUsers.size()];
                int i = 0;
                for (User user : addedUsers) {
                    addedUsersId[i] = user.userId;
                    i++;
                }

                db.collection(Constants.KEY_PROJECT_LISTS)
                        .document(project.projectId)
                        .update(Constants.KEY_PENDING_INVITES, FieldValue.arrayUnion(addedUsersId))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AddMembersActivity.this
                                        , "Invitation Sent"
                                        , Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddMembersActivity.this
                                        , "Error sending invitation"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        binding.searchBarAddMembers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String query) {
        ArrayList<User> filteredUsers = new ArrayList<>();

        for (User user : users) {
            if (user.email.toLowerCase().contains(query.toLowerCase())
            || user.username.toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        userListsAdapter.setFilteredUserList(filteredUsers);
    }

    private void init() {
        project = (Project) getIntent().getSerializableExtra("project");
        membersAlreadyAddedList = new ArrayList<>(Arrays.asList(project.memberList));
        db = FirebaseFirestore.getInstance();
        addedUsers = new ArrayList<>();

        isRecyclerLoading(true);
    }
}