package com.capstone.projecthub;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.capstone.projecthub.Adapter.TaskHomeListAdapter;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.Model.Tasks;
import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.FragmentTasksBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public TasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TasksFragment.
     */
    public static TasksFragment newInstance(String param1, String param2) {
        TasksFragment fragment = new TasksFragment();
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

    private FragmentTasksBinding binding;
    private FirebaseFirestore db;
    private ArrayList<Tasks> tasks;
    private PreferenceManager preferenceManager;
    private TaskHomeListAdapter taskAdapter;
    private TaskHomeListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTasksBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initDefine();
        setListeners();
        loadTaskDetails();

        return view;
    }

    private void setListeners() {
        binding.buttonRefreshTaskListHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecyclerLoading(true);
                loadTaskDetails();
            }
        });
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.recyclerHomeTask.setVisibility(View.GONE);
            binding.progressHomeTask.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerHomeTask.setVisibility(View.VISIBLE);
            binding.progressHomeTask.setVisibility(View.GONE);
        }
    }

    private void initDefine() {
        isRecyclerLoading(true);

        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
    }

    private void loadTaskDetails() {
        db.collection(Constants.KEY_TASKS_LIST).whereArrayContains(
                Constants.KEY_PROJECT_MEMBERS_ID, preferenceManager.getString(Constants.KEY_USER_ID)
        ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    tasks = new ArrayList<>();
                    adapter = getTaskHomeAdapter();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Tasks taskGotten = new Tasks();

                        taskGotten.projectId = document.getString(Constants.KEY_PROJECT_ID);
                        taskGotten.usersId = (ArrayList<String>) document.get(Constants.KEY_PROJECT_MEMBERS_ID);
                        taskGotten.dueDate = document.getDate(Constants.KEY_TASK_DUE_DATE);
                        taskGotten.tasksDesc = document.getString(Constants.KEY_TASK_DESC);
                        taskGotten.assignedDate = document.getDate(Constants.KEY_TASK_ASSIGNED_DATE);
                        taskGotten.tasksName = document.getString(Constants.KEY_TASK_NAME);
                        taskGotten.status = document.getString(Constants.KEY_TASK_STATUS);
                        taskGotten.tasksId = document.getId();

                        tasks.add(taskGotten);
                    }
                    binding.recyclerHomeTask.setAdapter(adapter);
                    isRecyclerLoading(false);
                    binding.viewTimelineTask.setVisibility(View.VISIBLE);
                    binding.imageNoTaskHome.setVisibility(View.GONE);
                    tasks.sort(Comparator.comparing(obj -> obj.dueDate));
                    adapter.notifyDataSetChanged();

                    if (tasks.isEmpty()) {
                        //show no found task image
                        binding.viewTimelineTask.setVisibility(View.GONE);
                        binding.imageNoTaskHome.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.imageNoTaskHome.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private TaskHomeListAdapter getTaskHomeAdapter() {
        TaskHomeListAdapter adapterCurrent = new TaskHomeListAdapter(tasks, getContext());

        adapterCurrent.setOnItemClickListener(new TaskHomeListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Project project) {
                Intent intent = new Intent(getContext(), ProjectHomeActivity.class);
                intent.putExtra("Project Details For ProjectHome", project);
                startActivity(intent);
            }
        });

        return adapterCurrent;
    }
}