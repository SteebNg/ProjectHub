package com.capstone.projecthub;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private PreferenceManager preferenceManager;
    private Random random;
    private Uri defaultProfileImg;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
    }

    private void setListeners() {
        binding.textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //this will redirect back to LoginActivity as LoginActivity has not been destroyed
            }
        });
        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRegisterButtonLoading(true);
                boolean credentialsValid = isCredentialsValid();

                if (credentialsValid) {
                    checkEmailExist();
                } else {
                    isRegisterButtonLoading(false);
                }
            }
        });
        //show password
        binding.imageEyeHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageEyeHide.setVisibility(View.GONE);
                binding.imageEyeShow.setVisibility(View.VISIBLE);
                binding.editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        binding.imageEyeShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageEyeHide.setVisibility(View.VISIBLE);
                binding.imageEyeShow.setVisibility(View.GONE);
                binding.editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        binding.imageEyeHideConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageEyeHideConfirm.setVisibility(View.GONE);
                binding.imageEyeShowConfirm.setVisibility(View.VISIBLE);
                binding.editTextConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        binding.imageEyeShowConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageEyeHideConfirm.setVisibility(View.VISIBLE);
                binding.imageEyeShowConfirm.setVisibility(View.GONE);
                binding.editTextConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    private void checkEmailExist() {
        db.collection(Constants.KEY_USER_LIST)
                .whereEqualTo(Constants.KEY_EMAIL, binding.editTextEmail.getText().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                registerUser();
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        "This email has already been registered",
                                        Toast.LENGTH_SHORT).show();
                                isRegisterButtonLoading(false);
                            }
                        }
                    }
                });
    }

    private void registerUser() {
        auth.createUserWithEmailAndPassword(binding.editTextEmail.getText().toString().trim(),
                binding.editTextPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    currentUser = auth.getCurrentUser();

                    defaultProfileImg = getRandomProfileImage();

                    //setDisplayName
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(binding.editTextUsername.getText().toString())
                            .build();
                    currentUser.updateProfile(profileUpdates)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Setting Display Name", "Fail");
                                        }
                                    });
                    sendVerificationEmail();
                } else {
                    Toast.makeText(RegisterActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    isRegisterButtonLoading(false);
                }
            }
        });
    }

    private void sendVerificationEmail() {

        currentUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                registerUserToDb();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                currentUser.delete();
            }
        });
    }

    private void setProfilePic() {
        storageReference.child(currentUser.getUid() + "/" + Constants.KEY_PROFILE_IMAGE)
                .putFile(defaultProfileImg)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Intent intent = new Intent(RegisterActivity.this, NotifyEmailVerifyActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void registerUserToDb() {

        Map<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_EMAIL, binding.editTextEmail.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, encryptString(binding.editTextPassword.getText().toString()));
        user.put(Constants.KEY_USERNAME, binding.editTextUsername.getText().toString());

        db.collection(Constants.KEY_USER_LIST).document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        setProfilePic();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error getting document", Toast.LENGTH_SHORT).show();
                        isRegisterButtonLoading(false);
                    }
                });
    }

    private boolean isCredentialsValid() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.editTextUsername.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() < 6){
            Toast.makeText(this, "Password must be 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!(Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!(password.equals(confirmPassword))) { //if the password and confirm does not match
            Toast.makeText(this, "The confirmed password does not match", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void isRegisterButtonLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBarRegister.setVisibility(View.VISIBLE);
            binding.buttonRegister.setText("");
            binding.buttonRegister.setOnClickListener(null);
        } else {
            binding.progressBarRegister.setVisibility(View.GONE);
            binding.buttonRegister.setText("Register");
            binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isRegisterButtonLoading(true);
                    boolean credentialsValid = isCredentialsValid();

                    if (credentialsValid) {
                        checkEmailExist();
                    } else {
                        isRegisterButtonLoading(false);
                    }
                }
            });
        }
    }

    private Uri getRandomProfileImage() {
        int number = random.nextInt(6);
        Context context = RegisterActivity.this;
        int imageChoosenId;

        switch (number) {
            case 0: {
                imageChoosenId = R.drawable.capybara;
                break;
            }
            case 1: {
                imageChoosenId = R.drawable.ketchup;
                break;
            }
            case 2: {
                imageChoosenId = R.drawable.pizza;
                break;
            }
            case 3: {
                imageChoosenId = R.drawable.santa_hat;
                break;
            }
            case 4: {
                imageChoosenId = R.drawable.teddybear;
                break;
            }
            default: {
                imageChoosenId = R.drawable.watermelon;
                break;
            }
        }
        String packageName = getPackageName();

        return Uri.parse("android.resource://" + packageName + "/" + imageChoosenId);
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        random = new Random();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child(Constants.KEY_USER_LIST + "/");
    }

    private String encryptString(String stringValue) {
        byte[] stringInBytes = new byte[0];

        stringInBytes = stringValue.getBytes(StandardCharsets.UTF_8);

        return Base64.getEncoder().encodeToString(stringInBytes);
    }
}