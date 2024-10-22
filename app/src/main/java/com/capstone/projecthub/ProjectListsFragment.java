package com.capstone.projecthub;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.capstone.projecthub.Adapter.ProjectListsAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.FragmentProjectListsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectListsFragment} factory method to
 * create an instance of this fragment.
 */
public class ProjectListsFragment extends Fragment {

    private FragmentProjectListsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private final int KEY_GET_ACTIVITY_RESULT = 1;
    private ArrayList<Project> projects;
    private ProjectListsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProjectListsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        init();
        setListeners();
        loadProjects();

        return view;
    }

    private void init() {
        isRecyclerLoading(true);

        preferenceManager = new PreferenceManager(getContext());
        db = FirebaseFirestore.getInstance();
    }

    private void loadProjects() {
        db.collection(Constants.KEY_PROJECT_LISTS)
                .whereArrayContains(Constants.KEY_PROJECT_MEMBERS_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
                            isRecyclerLoading(false);

                            projects = new ArrayList<>();
                            adapter = getProjectListsAdapter(projects);
                            binding.recyclerProjectList.setAdapter(adapter);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Project project = new Project();
                                project.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                project.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                project.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);
                                project.projectId = document.getId();
                                project.projectImage = document.getString(Constants.KEY_PROJECT_IMAGE);
                                project.projectLeaderId = document.getString(Constants.KEY_PROJECT_LEADER);
                                project.memberList = ((List<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID)).toArray(new String[0]);

                                projects.add(project);
                            }
                            binding.textNoProjects.setVisibility(View.GONE);
                            projects.sort(Comparator.comparing(obj -> obj.dueDate));
                            adapter.notifyDataSetChanged();
                        } else {
                            binding.textNoProjects.setVisibility(View.VISIBLE);
                            isRecyclerLoading(false);
                            binding.viewTimelineProject.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to retrieve projects", Toast.LENGTH_SHORT).show();
                    }
                });
    }

private @NonNull ProjectListsAdapter getProjectListsAdapter(ArrayList<Project> projects) {
        ProjectListsAdapter adapter = new ProjectListsAdapter(getContext(), projects);

        adapter.setOnItemClickListener(new ProjectListsAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                Intent intent = new Intent(getContext(), ProjectHomeActivity.class);
                intent.putExtra("Project Details For ProjectHome", project);
                startActivity(intent);
            }
        });
        return adapter;
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.viewTimelineProject.setVisibility(View.GONE);
            binding.progressBarProjectLists.setVisibility(View.VISIBLE);
            binding.recyclerProjectList.setVisibility(View.GONE);
        } else {
            binding.viewTimelineProject.setVisibility(View.VISIBLE);
            binding.progressBarProjectLists.setVisibility(View.GONE);
            binding.recyclerProjectList.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners() {
        binding.buttonAddProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddProjectsActivity.class);
                startActivityForResult(intent, KEY_GET_ACTIVITY_RESULT);
            }
        });
        binding.buttonRefreshProjectList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecyclerLoading(true);
                loadProjects();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_GET_ACTIVITY_RESULT) {
            if (resultCode == HomeActivity.RESULT_OK) {
                loadProjects();
                if (data != null && data.getSerializableExtra("quitConfirm") != null) {
                    Project project = (Project) data.getSerializableExtra("quitConfirm");
                    int position = projects.indexOf(project);

                    projects.remove(project);
                    projects.sort(Comparator.comparing(obj -> obj.dueDate));
                    adapter.notifyItemRemoved(position);
                }
            }
        }
    }
}