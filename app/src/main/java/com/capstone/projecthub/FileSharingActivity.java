package com.capstone.projecthub;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.capstone.projecthub.Adapter.FileSharingListsAdapter;
import com.capstone.projecthub.Model.File;
import com.capstone.projecthub.Model.Project;
import com.capstone.projecthub.databinding.ActivityFileSharingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

public class FileSharingActivity extends AppCompatActivity {

    private ActivityFileSharingBinding binding;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Project project;
    private ArrayList<File> files;
    private FileSharingListsAdapter adapter;
    private String projectId;
    private final int KEY_PICK_FILES_TO_UPLOAD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileSharingBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
        loadFileList();
    }

    private void isRecyclerLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBarFileSharing.setVisibility(View.VISIBLE);
            binding.recyclerFiles.setVisibility(View.GONE);
        } else {
            binding.progressBarFileSharing.setVisibility(View.GONE);
            binding.recyclerFiles.setVisibility(View.VISIBLE);
        }
    }

    private void checkForStoragePermissionThenDownloadLocally(File file) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            downloadFileLocally(file);
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else {
                downloadFileLocally(file);
            }
        }
    }

    private void downloadFileLocally(File file) {
        DownloadManager downloadManager = (DownloadManager) FileSharingActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file.url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.fileName);

        downloadManager.enqueue(request);

        Toast.makeText(this, "Saved into Downloads folder", Toast.LENGTH_LONG).show();
    }

    private void requestStoragePermission() {
        Dialog dialog = new Dialog(FileSharingActivity.this);
        dialog.setContentView(R.layout.storage_permission_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();

        Button giveStoragePermissionButton = dialog.findViewById(R.id.buttonYesStoragePermission);
        giveStoragePermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                            startActivityIfNeeded(intent, 101);
                        } catch (Exception e) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            startActivityIfNeeded(intent, 101);
                        }
                    }
                }
            }
        });
    }

    private void deleteFileFromDb(File file) {
        Dialog dialog = new Dialog(FileSharingActivity.this);
        dialog.setContentView(R.layout.confirm_delete_file_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        Button buttonCancel = dialog.findViewById(R.id.buttonCancelDeleteFile);
        Button buttonConfirm = dialog.findViewById(R.id.buttonConfirmDeleteFile);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storageReference.child(projectId + "/" + file.fileName).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(FileSharingActivity.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                                loadFileList();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FileSharingActivity.this, "No such file exist", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.dismiss();
            }
        });
    }

    private void loadFileList() {
        isRecyclerLoading(true);

        storageReference.child(projectId + "/").listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        files = new ArrayList<>();
                        adapter = new FileSharingListsAdapter(FileSharingActivity.this, files);

                        adapter.setOnItemClickListenerForDownload(new FileSharingListsAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(File file) {
                                checkForStoragePermissionThenDownloadLocally(file);
                            }
                        });
                        adapter.setOnItemClickListenerForDelete(new FileSharingListsAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(File file) {
                                deleteFileFromDb(file);
                            }
                        });
                        binding.recyclerFiles.setAdapter(adapter);

                        if (!listResult.getItems().isEmpty()) {
                            listResult.getItems().forEach(new Consumer<StorageReference>() {
                                @Override
                                public void accept(StorageReference storageReference) {
                                    File file = new File();
                                    file.fileName = storageReference.getName();
                                    storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            file.url = task.getResult().toString();
                                            files.add(file);
                                            adapter.notifyDataSetChanged();
                                            isRecyclerLoading(false);
                                        }
                                    });
                                }
                            });
                        } else {
                            binding.imageNothingFile.setVisibility(View.VISIBLE);
                            isRecyclerLoading(false);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FileSharingActivity.this, "An error occurred while getting files", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setListeners() {
        binding.buttonBackFromFileSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.buttonUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    private void init() {
        project = (Project) getIntent().getSerializableExtra("passedProjectToFileShare");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child(Constants.KEY_PROJECT_LISTS + "/");
        projectId = project.projectId;
    }

    private void uploadFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, KEY_PICK_FILES_TO_UPLOAD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KEY_PICK_FILES_TO_UPLOAD && resultCode == RESULT_OK
            && data != null && data.getData() != null) {
            String fileName;
            Uri fileChoosen = data.getData();
            DocumentFile documentFile = DocumentFile.fromSingleUri(this, fileChoosen);

            if (documentFile != null && documentFile.getName() != null) {
                fileName = documentFile.getName();
            } else {
                Date date = new Date();
                String datePattern = "dd/MM/yyyy";
                DateFormat df = new SimpleDateFormat(datePattern);
                fileName = df.format(date);
            }

            storageReference.child(projectId + "/" + fileName).putFile(fileChoosen)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            binding.buttonUploadFile.setVisibility(View.VISIBLE);
                            binding.progressBarUploading.setVisibility(View.GONE);
                            binding.imageNothingFile.setVisibility(View.GONE);

                            Toast.makeText(FileSharingActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            loadFileList();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            binding.buttonUploadFile.setVisibility(View.GONE);
                            binding.progressBarUploading.setVisibility(View.VISIBLE);

                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            int currentProgress = (int) progress;
                            binding.progressBarUploading.setProgress(currentProgress);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.buttonUploadFile.setVisibility(View.VISIBLE);
                            binding.progressBarUploading.setVisibility(View.GONE);

                            Toast.makeText(FileSharingActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}