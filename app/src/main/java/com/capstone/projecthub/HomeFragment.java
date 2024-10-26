package com.capstone.projecthub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.capstone.projecthub.Adapter.MyProjectHomeListAdapter;
import com.capstone.projecthub.Adapter.MyTaskHomeListAdapter;
import com.capstone.projecthub.Adapter.ProjectInviteListAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private ArrayList<Project> projectsInvited;
    private ArrayList<Project> projects;
    private ArrayList<Tasks> tasks;
    private ProjectInviteListAdapter projectInvitedListAdapter;
    private StorageReference profileImageReference;
    private String userId;
    private MyProjectHomeListAdapter myProjectHomeListAdapter;
    private MyTaskHomeListAdapter myTaskHomeListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initDefine();
        setListeners();
        loadUserDetails();
        loadInvitationDetails();
        loadProjectDetails();
        loadTaskDetails();

        return view;
    }

    private void loadTaskDetails() {
        db.collection(Constants.KEY_TASKS_LIST)
                .whereArrayContains(Constants.KEY_PROJECT_MEMBERS_ID, userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            tasks = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Tasks currentTask = new Tasks();
                                currentTask.projectId = document.getString(Constants.KEY_PROJECT_ID);
                                currentTask.tasksName = document.getString(Constants.KEY_TASK_NAME);
                                currentTask.tasksDesc = document.getString(Constants.KEY_TASK_DESC);
                                currentTask.status = getStatusCode(document.getString(Constants.KEY_TASK_STATUS));
                                currentTask.usersId = (ArrayList<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID);
                                currentTask.dueDate = document.getDate(Constants.KEY_TASK_DUE_DATE);
                                currentTask.assignedDate = document.getDate(Constants.KEY_TASK_ASSIGNED_DATE);
                                currentTask.tasksId = document.getId();

                                tasks.add(currentTask);
                            }
                            binding.imageNoTask.setVisibility(View.GONE);
                            binding.recyclerMyTaskHome.setVisibility(View.VISIBLE);
                            myTaskHomeListAdapter = getMyTaskHomeListAdapter();
                            binding.recyclerMyTaskHome.setAdapter(myTaskHomeListAdapter);
                            tasks.sort(Comparator.comparing(obj -> obj.dueDate));
                            Collections.reverse(tasks);
                            myTaskHomeListAdapter.notifyDataSetChanged();
                        } else {
                            binding.recyclerMyTaskHome.setVisibility(View.GONE);
                            binding.imageNoTask.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private MyTaskHomeListAdapter getMyTaskHomeListAdapter() {
        MyTaskHomeListAdapter adapter = new MyTaskHomeListAdapter(tasks, getContext());

        adapter.setOnItemClickListener(new MyTaskHomeListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                Intent intent = new Intent(getContext(), ProjectHomeActivity.class);
                intent.putExtra("Project Details For ProjectHome", project);
                startActivity(intent);
            }
        });

        return adapter;
    }

    private String getStatusCode(String statusCode) {
        if (statusCode != null) {
            switch (statusCode) {
                case Constants.KEY_TASK_STATUS_DONE: {
                    return "Done";
                }
                case Constants.KEY_TASK_STATUS_ERROR: {
                    return "Error Encountered";
                }
                default: {
                    return "Unfinished";
                }
            }
        } else {
            return "Unfinished";
        }
    }

    private void loadProjectDetails() {
        db.collection(Constants.KEY_PROJECT_LISTS)
                .whereArrayContains(Constants.KEY_PROJECT_MEMBERS_ID, userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            projects = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Project project = new Project();
                                project.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                project.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                project.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);
                                project.projectId = document.getId();
                                project.projectImage = document.getString(Constants.KEY_PROJECT_IMAGE);
                                project.projectLeaderId = document.getString(Constants.KEY_PROJECT_LEADER);
                                project.memberList = ((List<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID)).toArray(new String[0]);
                                project.projectColor = document.getString(Constants.KEY_PROJECT_COLOR);

                                projects.add(project);
                            }
                            myProjectHomeListAdapter = getMyProjectHomeListAdapter();
                            binding.imageNoProject.setVisibility(View.GONE);
                            binding.recyclerMyProjectHome.setAdapter(myProjectHomeListAdapter);
                            myProjectHomeListAdapter.notifyDataSetChanged();
                            isHomeLoading(false);
                        } else {
                            binding.recyclerMyProjectHome.setVisibility(View.GONE);
                            binding.imageNoProject.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void isHomeLoading(boolean isLoading) {
        if(isLoading) {
            binding.progressBarHome.setVisibility(View.VISIBLE);
            binding.scrollViewHome.setVisibility(View.GONE);
        } else {
            binding.progressBarHome.setVisibility(View.GONE);
            binding.scrollViewHome.setVisibility(View.VISIBLE);
        }
    }

    private MyProjectHomeListAdapter getMyProjectHomeListAdapter() {
        MyProjectHomeListAdapter adapter = new MyProjectHomeListAdapter(projects, getContext());

        adapter.setOnItemClickListener(new MyProjectHomeListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                Intent intent = new Intent(getContext(), ProjectHomeActivity.class);
                intent.putExtra("Project Details For ProjectHome", project);
                startActivity(intent);
            }
        });

        return adapter;
    }

    private void loadUserDetails() {
        String welcomeTitle = "Hello, " + preferenceManager.getString(Constants.KEY_USERNAME) + "!";
        binding.textWelcomeTitle.setText(welcomeTitle);

        profileImageReference.child(userId + "/" + Constants.KEY_PROFILE_IMAGE).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getContext()).load(uri).into(binding.imageProfileHome);
                    }
                });

        isHomeLoading(false);
    }

    private void loadInvitationDetails() {
        db.collection(Constants.KEY_PROJECT_LISTS)
                .whereArrayContains(Constants.KEY_PENDING_INVITES, userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() !=  null) {
                            if (!task.getResult().isEmpty()) {
                                binding.layoutAcceptProject.setVisibility(View.VISIBLE);
                                projectsInvited = new ArrayList<>();

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Project currentProject = new Project();
                                    currentProject.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                    currentProject.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                    currentProject.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);
                                    currentProject.projectLeaderId = document.getString(Constants.KEY_PROJECT_LEADER);
                                    currentProject.projectId = document.getId();

                                    projectsInvited.add(currentProject);
                                }
                                projectInvitedListAdapter = getInvitedProjectAdapter();
                                binding.recyclerHomeProjectInvitation.setAdapter(projectInvitedListAdapter);
                                projectInvitedListAdapter.notifyDataSetChanged();
                                isHomeLoading(false);
                            } else {
                                binding.layoutAcceptProject.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    private ProjectInviteListAdapter getInvitedProjectAdapter() {
        ProjectInviteListAdapter currentAdapter = new ProjectInviteListAdapter(projectsInvited, getContext());

        currentAdapter.setOnRejectClickListener(new ProjectInviteListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                db.collection(Constants.KEY_PROJECT_LISTS).document(project.projectId)
                        .update(Constants.KEY_PENDING_INVITES,
                                FieldValue.arrayRemove(userId))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                projectsInvited.remove(project);
                                if (projectsInvited.isEmpty()) {
                                    binding.layoutAcceptProject.setVisibility(View.GONE);
                                }
                                projectInvitedListAdapter.notifyDataSetChanged();
                            }
                        });
            }
        });

        currentAdapter.setOnAcceptClickListener(new ProjectInviteListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                db.collection(Constants.KEY_PROJECT_LISTS).document(project.projectId)
                        .update(Constants.KEY_PENDING_INVITES,
                                FieldValue.arrayRemove(userId))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                projectsInvited.remove(project);
                                projectInvitedListAdapter.notifyDataSetChanged();

                                addUserToProject(project);
                            }
                        });
            }
        });

        return currentAdapter;
    }

    private void addUserToProject(Project project) {
        db.collection(Constants.KEY_PROJECT_LISTS).document(project.projectId)
                .update(Constants.KEY_PROJECT_MEMBERS_ID,
                        FieldValue.arrayUnion(userId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Invitation Accepted", Toast.LENGTH_SHORT).show();
                        projectsInvited.remove(project);
                        projectInvitedListAdapter.notifyDataSetChanged();

                        if (projectsInvited.isEmpty()) {
                            binding.layoutAcceptProject.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void setListeners() {
        binding.buttonRefreshHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInvitationDetails();
                loadProjectDetails();
                loadTaskDetails();
            }
        });
    }

    private void initDefine() {
        isHomeLoading(true);

        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        profileImageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_USER_LIST + "/");
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
    }
}