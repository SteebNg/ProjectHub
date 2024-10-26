package com.capstone.projecthub;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.capstone.projecthub.Adapter.AnnouncementHomeListAdapter;
import com.capstone.projecthub.Model.Announcement;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.FragmentAnnouncementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnnouncementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnnouncementFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AnnouncementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnnouncementFragment.
     */
    public static AnnouncementFragment newInstance(String param1, String param2) {
        AnnouncementFragment fragment = new AnnouncementFragment();
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

    private FragmentAnnouncementBinding binding;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private ArrayList<Project> projects;
    private ArrayList<String> projectIds;
    private ArrayList<Announcement> announcements;
    private AnnouncementHomeListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAnnouncementBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initDefine();
        setListeners();
        findProjectThatTheUserIsIn();

        return view;
    }

    private void findProjectThatTheUserIsIn() {
        db.collection(Constants.KEY_PROJECT_LISTS).whereArrayContains(
                Constants.KEY_PROJECT_MEMBERS_ID, preferenceManager.getString(Constants.KEY_USER_ID)
                ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    projectIds = new ArrayList<>();
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
                        projectIds.add(project.projectId);
                    }
                    loadAnnouncements();
                } else {
                    binding.imageNoAnnouncement.setVisibility(View.VISIBLE);
                    isRecyclerLoading(false);
                }
            }
        });
    }

    private void loadAnnouncements() {
        int[] projectsIdWentThrough = {0};
        announcements = new ArrayList<>();
        for (String projectId : projectIds) {
            projectsIdWentThrough[0]++;
            db.collection(Constants.KEY_ANNOUNCEMENTS).whereEqualTo(Constants.KEY_PROJECT_ID, projectId).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Announcement announcement = new Announcement();
                                    announcement.announcerId = document.getString(Constants.KEY_ANNOUNCEMENTS_ANNOUNCER_ID);
                                    announcement.announcerName = document.getString(Constants.KEY_ANNOUNCEMENTS_ANNOUNCER_NAME);
                                    announcement.body = document.getString(Constants.KEY_ANNOUNCEMENTS_BODY);
                                    announcement.date = document.getDate(Constants.KEY_ANNOUNCEMENTS_DATE);
                                    announcement.projectId = document.getString(Constants.KEY_ANNOUNCEMENTS_PROJECT_ID);
                                    announcement.title = document.getString(Constants.KEY_ANNOUNCEMENTS_TITLE);
                                    announcement.announcementId = document.getId();

                                    announcements.add(announcement);
                                }
                                if (projectsIdWentThrough[0] == projectIds.size()) {
                                    adapter = getAnnouncementHomeAdapter();
                                    binding.recyclerHomeAnnouncement.setAdapter(adapter);
                                    announcements.sort(Comparator.comparing(obj -> obj.date));
                                    Collections.reverse(announcements);
                                    adapter.notifyDataSetChanged();
                                    isRecyclerLoading(false);
                                    binding.imageNoAnnouncement.setVisibility(View.GONE);
                                }
                            } else {
                                isRecyclerLoading(false);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.imageNoAnnouncement.setVisibility(View.VISIBLE);
                            isRecyclerLoading(false);
                        }
                    });
        }
    }

    private AnnouncementHomeListAdapter getAnnouncementHomeAdapter() {
        AnnouncementHomeListAdapter adapterAnnounce = new AnnouncementHomeListAdapter(announcements, getContext());

        adapterAnnounce.setOnItemClickListener(new AnnouncementHomeListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                Intent intent = new Intent(getContext(), ProjectHomeActivity.class);
                intent.putExtra("Project Details For ProjectHome", project);
                startActivity(intent);
            }
        });

        return adapterAnnounce;
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerHomeAnnouncement.setVisibility(View.GONE);
            binding.progressHomeAnnouncement.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerHomeAnnouncement.setVisibility(View.VISIBLE);
            binding.progressHomeAnnouncement.setVisibility(View.GONE);
        }
    }

    private void initDefine() {
        isRecyclerLoading(true);

        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
    }

    private void setListeners() {
        binding.buttonRefreshAnnouncementListHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(TODO) Refresh
                findProjectThatTheUserIsIn();
            }
        });
    }
}