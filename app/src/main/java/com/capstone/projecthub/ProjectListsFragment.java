package com.capstone.projecthub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectListsFragment} factory method to
 * create an instance of this fragment.
 */
public class ProjectListsFragment extends Fragment {

    private FragmentProjectListsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

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

                            ArrayList<Project> projects = new ArrayList<>();
                            ProjectListsAdapter adapter = getProjectListsAdapter(projects);
                            binding.recyclerProjectList.setAdapter(adapter);

                            adapter.setOnItemClickListener(new ProjectListsAdapter.OnItemClickListener() {
                                @Override
                                public void onClick(Project project) {
                                    //(TODO) Direct the user to the correct activity
                                    Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
                                }
                            });

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Project project = new Project();
                                project.projectName = document.getString(Constants.KEY_PROJECT_NAME);
                                project.projectDescription = document.getString(Constants.KEY_PROJECT_DESC);
                                project.dueDate = document.getDate(Constants.KEY_PROJECT_DUE_DATE);

                                projects.add(project);
                            }
                            binding.textNoProjects.setVisibility(View.GONE);
                            projects.sort(Comparator.comparing(obj -> obj.dueDate));
                            adapter.notifyDataSetChanged();
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

    private @NonNull ProjectListsAdapter getProjectListsAdapter(ArrayList<Project> projects) {
        ProjectListsAdapter adapter = new ProjectListsAdapter(getContext(), projects);

        adapter.setOnItemClickListener(new ProjectListsAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                //(TODO) Direct user to the correct activity
                Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
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
                //(TODO) direct user to add projects activity
            }
        });
    }
}