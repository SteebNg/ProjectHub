package com.capstone.projecthub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.capstone.projecthub.Adapter.AnnouncementListAdapter;
import com.capstone.projecthub.Model.Announcement;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityProjectHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;

public class ProjectHomeActivity extends AppCompatActivity {

    //(TODO) find and extract the url

    private ActivityProjectHomeBinding binding;
    private FirebaseFirestore db;
    private Project currentProject;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private PreferenceManager preferenceManager;
    private final int KEY_ACTIVITY_RESULT_FOR_REFRESH_ANNOUNCEMENT = 2;
    private final int KEY_ACTIVITY_RESULT_FOR_CHECK_IF_LEAVE = 3;

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
        updateAnnouncementList();
    }

    private void updateAnnouncementList() {
        //(TODO) Update announcement
        db.collection(Constants.KEY_ANNOUNCEMENTS)
                .whereEqualTo(Constants.KEY_ANNOUNCEMENTS_PROJECT_ID, currentProject.projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
                            ArrayList<Announcement> announcements = new ArrayList<>();
                            AnnouncementListAdapter adapter = getAnnouncementListsAdapter(announcements);
                            binding.recyclerAnnouncementList.setAdapter(adapter);

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
                            announcements.sort(Comparator.comparing(obj -> obj.date, Comparator.reverseOrder()));
                            adapter.notifyDataSetChanged();
                            binding.imageNoAnnouncementHome.setVisibility(View.GONE);
                            binding.textNoAnnouncementHome.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorLoadingAnnouncement();
                    }
                });
    }

    private void errorLoadingAnnouncement() {
        binding.imageNoAnnouncementHome.setVisibility(View.GONE);
        binding.textNoAnnouncementHome.setVisibility(View.GONE);
        binding.textErrorAnnouncementHome.setVisibility(View.VISIBLE);
        binding.imageErrorAnnouncementHome.setVisibility(View.VISIBLE);
    }

    private @NonNull AnnouncementListAdapter getAnnouncementListsAdapter(ArrayList<Announcement> announcements) {
        AnnouncementListAdapter announcementListAdapter = new AnnouncementListAdapter(
                ProjectHomeActivity.this, announcements);

        //put onClick here if want
        return announcementListAdapter;
    }

    private void updateProjectDetails() {
        db.collection(Constants.KEY_PROJECT_LISTS)
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
                                updateProfileImage();
                                updateProjectImage(document.getString(Constants.KEY_PROJECT_IMAGE));
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

    private void updateProjectImage(String imageString) {
        if (imageString != null) {
            ProjectHomeActivity context = ProjectHomeActivity.this;
            switch (imageString) {
                case "0":
                    binding.imageProjectHome.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.discuss1)
                    );
                    break;
                case "1":
                    binding.imageProjectHome.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.discuss2)
                    );
                    break;
                case "2":
                    binding.imageProjectHome.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.discuss3)
                    );
                    break;
                case "3":
                    binding.imageProjectHome.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.discuss4)
                    );
                    break;
                case "4":
                    binding.imageProjectHome.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.discuss5)
                    );
                    break;
                default:
                    binding.imageProjectHome.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.discuss6)
                    );
                    break;
            }
        }
    }

    private void updateProfileImage() {
        storageReference.child(Constants.KEY_USER_LIST
                + "/"
                + preferenceManager.getString(Constants.KEY_USER_ID)
                + "/"
                + Constants.KEY_PROFILE_IMAGE)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (uri != null) {
                            Glide.with(ProjectHomeActivity.this).load(uri).into(binding.imageProfileProjectHomeAnnouncement);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProjectHomeActivity.this, "Failed to retrieved profile image", Toast.LENGTH_SHORT).show();
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
                    Intent intent = new Intent(ProjectHomeActivity.this, AddAnnouncementActivity.class);
                    intent.putExtra("ProjectIdForAddAnnouncement", currentProject);
                    startActivityForResult(intent, KEY_ACTIVITY_RESULT_FOR_REFRESH_ANNOUNCEMENT);
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
                Intent intent = new Intent(ProjectHomeActivity.this, ProjectMembersListActivity.class);
                intent.putExtra("Project for members list", currentProject);
                startActivityForResult(intent, KEY_ACTIVITY_RESULT_FOR_CHECK_IF_LEAVE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_ACTIVITY_RESULT_FOR_REFRESH_ANNOUNCEMENT) {
            if (resultCode == ProjectHomeActivity.RESULT_OK) {
                updateAnnouncementList();
            }
        } else if (requestCode == KEY_ACTIVITY_RESULT_FOR_CHECK_IF_LEAVE) {
            if (resultCode == ProjectHomeActivity.RESULT_OK && data != null) {
                if (data.getStringExtra("quit").equals("true")) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }
}