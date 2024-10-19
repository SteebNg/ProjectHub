package com.capstone.projecthub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.capstone.projecthub.Adapter.ProjectListsAdapter;
import com.capstone.projecthub.Listeners.ProjectListListener;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.FragmentProjectListsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectListsFragment extends Fragment implements ProjectListListener {

    private FragmentProjectListsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private ArrayList<Project> projects;
    private ProjectListsAdapter projectListsAdapter;

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
        projects = new ArrayList<>();
        projectListsAdapter = new ProjectListsAdapter(projects, this);
        binding.recyclerProjectList.setAdapter(projectListsAdapter);
    }

    private void loadProjects() {
        db.collection(Constants.KEY_PROJECT_LISTS)
                .whereArrayContains(Constants.KEY_PROJECT_MEMBERS_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        projects.clear();
                        if (task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
                            isRecyclerLoading(false);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Project project = new Project();
                                project.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                project.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                project.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);

                                projects.add(project);
                            }
                            binding.textNoProjects.setVisibility(View.GONE);
                            projectListsAdapter.notifyDataSetChanged();
                        } else {
                            binding.textNoProjects.setVisibility(View.VISIBLE);
                            isRecyclerLoading(false);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to retrieve projects", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBarProjectLists.setVisibility(View.VISIBLE);
            binding.recyclerProjectList.setVisibility(View.GONE);
        } else {
            binding.progressBarProjectLists.setVisibility(View.GONE);
            binding.recyclerProjectList.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners() {
        binding.buttonAddProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(TODO) direct user to add projects activity
            }
        });
    }

    @Override
    public void onProjectClicked(Project project) {
        Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
    }
}