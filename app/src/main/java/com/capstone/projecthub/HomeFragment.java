package com.capstone.projecthub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.capstone.projecthub.Adapter.ProjectInviteListAdapter;
import com.capstone.projecthub.Model.Project;
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
    private ProjectInviteListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initDefine();
        setListeners();
        loadInvitationDetails();

        return view;
    }

    private void loadInvitationDetails() {
        db.collection(Constants.KEY_PROJECT_LISTS)
                .whereArrayContains(Constants.KEY_PENDING_INVITES, preferenceManager.getString(Constants.KEY_USER_ID))
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
                                adapter = getInvitedProjectAdapter();
                                binding.recyclerHomeProjectInvitation.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
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
                                FieldValue.arrayRemove(preferenceManager.getString(Constants.KEY_USER_ID)))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                projectsInvited.remove(project);
                                if (projectsInvited.isEmpty()) {
                                    binding.layoutAcceptProject.setVisibility(View.GONE);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        });

        currentAdapter.setOnAcceptClickListener(new ProjectInviteListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                db.collection(Constants.KEY_PROJECT_LISTS).document(project.projectId)
                        .update(Constants.KEY_PENDING_INVITES,
                                FieldValue.arrayRemove(preferenceManager.getString(Constants.KEY_USER_ID)))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                projectsInvited.remove(project);
                                adapter.notifyDataSetChanged();

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
                        FieldValue.arrayUnion(preferenceManager.getString(Constants.KEY_USER_ID)))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Invitation Accepted", Toast.LENGTH_SHORT).show();
                        projectsInvited.remove(project);
                        adapter.notifyDataSetChanged();

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
                //(TODO) refresh home page details
            }
        });
    }

    private void initDefine() {
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
    }
}