package com.capstone.projecthub;

import android.content.Intent;
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
import com.capstone.projecthub.databinding.ActivityAddAnnouncementBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddAnnouncementActivity extends AppCompatActivity {

    private ActivityAddAnnouncementBinding binding;
    private FirebaseFirestore db;
    private Project currentProject;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAnnouncementBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        setDate();
    }

    private void setDate() {
        Date currentDate = Calendar.getInstance().getTime();

        String pattern = "dd/MM/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        String todayAsString = df.format(currentDate);

        binding.textDateToday.setText(todayAsString);
    }

    private void setListeners() {
        binding.buttonBackFromAddAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        binding.buttonConfirmAddAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoValid()) {
                    Map<String, Object> announcement = new HashMap<>();
                    announcement.put(Constants.KEY_ANNOUNCEMENTS_PROJECT_ID, currentProject.projectId);
                    announcement.put(Constants.KEY_ANNOUNCEMENTS_TITLE,
                            binding.editTextAnnouncementTitle.getText().toString());
                    announcement.put(Constants.KEY_ANNOUNCEMENTS_DATE, Calendar.getInstance().getTime());
                    announcement.put(Constants.KEY_ANNOUNCEMENTS_BODY,
                            binding.editTextAnnouncementBody.getText().toString());
                    announcement.put(Constants.KEY_ANNOUNCEMENTS_ANNOUNCER_ID,
                            preferenceManager.getString(Constants.KEY_USER_ID));
                    announcement.put(Constants.KEY_ANNOUNCEMENTS_ANNOUNCER_NAME,
                            preferenceManager.getString(Constants.KEY_USERNAME));

                    db.collection(Constants.KEY_ANNOUNCEMENTS).add(announcement)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddAnnouncementActivity.this, "Failed to add announcement", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private boolean infoValid() {
        if (binding.editTextAnnouncementTitle.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please add a title", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.editTextAnnouncementBody.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please add context", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(AddAnnouncementActivity.this);

        currentProject = (Project) getIntent().getSerializableExtra("ProjectIdForAddAnnouncement");
    }
}