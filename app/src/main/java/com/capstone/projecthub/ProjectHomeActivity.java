package com.capstone.projecthub;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityProjectHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ProjectHomeActivity extends AppCompatActivity {

    //(TODO) find and extract the url

    private ActivityProjectHomeBinding binding;
    private FirebaseFirestore db;
    private Project currentProject;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectHomeBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        updateProjectDetails();
    }

    private void updateProjectDetails() {
        db.collection(Constants.KEY_ANNOUNCEMENTS)
                .document(currentProject.projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                //convert Date to String
                                String datePattern = "dd/MM/yyyy";
                                DateFormat df = new SimpleDateFormat(datePattern);
                                String projectDueDate = df.format(document.getDate(Constants.KEY_PROJECT_DUE_DATE));

                                binding.textProjectNameHome.setText(
                                        document.getString(Constants.KEY_PROJECT_NAME)
                                );
                                binding.textProjectDueDateHome.setText(projectDueDate);
                                updateLeaderName(document.getString(Constants.KEY_PROJECT_LEADER));
                                checkIfLeader(document.getString(Constants.KEY_PROJECT_LEADER));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProjectHomeActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfLeader(String leaderId) {
        if (leaderId != null && !leaderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
            binding.textMakeAnnounement.setText("Only the leader can make an announcement");
        } else {
            binding.buttonAddAnnouncementProjectHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //(TODO) Direct user to activity
                }
            });
        }
    }

    private void updateLeaderName(String projectLeaderId) {
        db.collection(Constants.KEY_USER_LIST).document(projectLeaderId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                binding.textProjectLeaderHome.setText(
                                        documentSnapshot.getString(Constants.KEY_USERNAME)
                                );
                            }
                        }
                    }
                });
    }

    private void setListeners() {
        binding.buttonTaskProjectHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(TODO) Direct to tasks activity
            }
        });
        binding.buttonFileProjectHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(TODO) Direct to file activity
            }
        });
        binding.buttonMembersProjectHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(TODO) Direct to members activity
            }
        });
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        currentProject = (Project) getIntent().getSerializableExtra("Project Details For ProjectHome");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        preferenceManager = new PreferenceManager(ProjectHomeActivity.this);
    }
}