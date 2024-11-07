package com.capstone.projecthub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Adapter.MemberListsForTasksAdapter;
import com.capstone.projecthub.Adapter.MemberYetToAssignListsAdapter;
import com.capstone.projecthub.Adapter.UserListsAdapter;
import com.capstone.projecthub.Model.DataWrapper;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.User;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.databinding.ActivityAssignMembersToTaskBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;

public class AssignMembersToTaskActivity extends AppCompatActivity {

    private ActivityAssignMembersToTaskBinding binding;
    private FirebaseFirestore db;
    private Project currentProject;
    private String projectId;
    private ArrayList<String> membersId;
    private ArrayList<User> projectMembers;
    private ArrayList<User> assignedProjectMembers;
    private MemberYetToAssignListsAdapter yetToAssignListsAdapter;
    private MemberListsForTasksAdapter membersListAdapter;
    private final int KEY_RESULT_FOR_FINISH = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssignMembersToTaskBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        loadMembersId();
    }

    private void loadMembersId() {
        db.collection(Constants.KEY_PROJECT_LISTS).document(projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                membersId = new ArrayList<>((ArrayList<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID));
                                loadMembers();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AssignMembersToTaskActivity.this, "Failed to get member details", Toast.LENGTH_SHORT).show();
                        binding.progressBarAssignMembersList.setVisibility(View.GONE);
                    }
                });
    }

    private void loadMembers() {
        final int[] membersIdWentThrough = {0};
        for (String membersIdFromDb : membersId) {
            db.collection(Constants.KEY_USER_LIST).document(membersIdFromDb)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    membersIdWentThrough[0]++;
                                    User user = new User();
                                    user.userId = document.getId();
                                    user.username = document.getString(Constants.KEY_USERNAME);
                                    user.email = document.getString(Constants.KEY_EMAIL);
                                    projectMembers.add(user);

                                    //have to have a counter cause idk how to wait for multi threading tasks
                                    if (membersIdWentThrough[0] == membersId.size()) {
                                        loadRecyclerDetails();
                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AssignMembersToTaskActivity.this, "Failed to get member details", Toast.LENGTH_SHORT).show();
                            binding.progressBarAssignMembersList.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void loadRecyclerDetails() {
        isRecyclerLoading(false);

        membersListAdapter = getMemberListAdapter(projectMembers);
        binding.recyclerTasksAssignMembersList.setAdapter(membersListAdapter);

        projectMembers.sort(userComparator);
        membersListAdapter.notifyDataSetChanged();
    }

    private MemberListsForTasksAdapter getMemberListAdapter(ArrayList<User> projectMembers) {
        MemberListsForTasksAdapter adapter = new MemberListsForTasksAdapter(projectMembers, AssignMembersToTaskActivity.this);

        adapter.setOnItemClickListener(new UserListsAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                int position = projectMembers.indexOf(user);
                projectMembers.remove(user);
                projectMembers.sort(userComparator);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, projectMembers.size());

                updateAddedMemberListForTasks(user);
            }
        });

        return adapter;
    }

    private void updateAddedMemberListForTasks(User user) {
        yetToAssignListsAdapter = getMemberYetToTasksAdapter(assignedProjectMembers);
        binding.recyclerTasksYetToAssignMembers.setAdapter(yetToAssignListsAdapter);

        assignedProjectMembers.add(user);
        yetToAssignListsAdapter.notifyDataSetChanged();

        if (assignedProjectMembers.isEmpty()) {
            binding.recyclerTasksYetToAssignMembers.setVisibility(View.GONE);
        } else {
            binding.recyclerTasksYetToAssignMembers.setVisibility(View.VISIBLE);
        }
    }

    private MemberYetToAssignListsAdapter getMemberYetToTasksAdapter(ArrayList<User> assignedProjectMembers) {
        MemberYetToAssignListsAdapter adapter = new MemberYetToAssignListsAdapter(assignedProjectMembers, AssignMembersToTaskActivity.this);

        adapter.setOnItemClickListener(new MemberYetToAssignListsAdapter.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                int position = assignedProjectMembers.indexOf(user);
                assignedProjectMembers.remove(user);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, assignedProjectMembers.size());

                updateMemberListsForTasksRecycler(user);
            }
        });

        return adapter;
    }

    private void updateMemberListsForTasksRecycler(User user) {
        membersListAdapter = getMemberListAdapter(projectMembers);
        binding.recyclerTasksAssignMembersList.setAdapter(membersListAdapter);

        projectMembers.add(user);

        projectMembers.sort(userComparator);
        membersListAdapter.notifyDataSetChanged();
    }

    private final Comparator<User> userComparator = new Comparator<User>() {
        @Override
        public int compare(User o1, User o2) {
            return o1.username.compareTo(o2.username);
        }
    };

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerTasksAssignMembersList.setVisibility(View.GONE);
            binding.progressBarAssignMembersList.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerTasksAssignMembersList.setVisibility(View.VISIBLE);
            binding.progressBarAssignMembersList.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        binding.buttonBackFromTasksAssignMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        binding.buttonTasksNextAssignMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (assignedProjectMembers.isEmpty()) {
                    Toast.makeText(AssignMembersToTaskActivity.this, "There are no members assigned", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(AssignMembersToTaskActivity.this, AssignTasksDetailsActivity.class);
                    intent.putExtra("putProjectForTasksDetails", currentProject);
                    intent.putExtra("putAssignedMemberForTasksDetails", new DataWrapper(assignedProjectMembers));
                    startActivityForResult(intent, KEY_RESULT_FOR_FINISH);
                }
            }
        });
        binding.searchBarTasksAssignMembers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        for (User user : projectMembers) {
            if (user.email.toLowerCase().contains(query.toLowerCase())
                    || user.username.toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        membersListAdapter.setFilteredUserList(filteredUsers);
    }

    private void init() {
        isRecyclerLoading(true);

        db = FirebaseFirestore.getInstance();
        currentProject = (Project) getIntent().getSerializableExtra("projectToAssignMembers");
        projectId = currentProject.projectId;
        projectMembers = new ArrayList<>();
        assignedProjectMembers = new ArrayList<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_RESULT_FOR_FINISH) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}