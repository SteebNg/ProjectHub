package com.capstone.projecthub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private PreferenceManager preferenceManager;

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
    }

    private void checkEmailExist() {
        db.collection(Constants.KEY_USER_LIST)
                .whereEqualTo(Constants.KEY_EMAIL, binding.editTextEmail.getText().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            registerUser();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "This email has already been registered",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser() {
        auth.signInWithEmailAndPassword(binding.editTextEmail.getText().toString().trim(),
                binding.editTextPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //(TODO) verify email
                    registerUserToDb();
                } else {
                    Toast.makeText(RegisterActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    isRegisterButtonLoading(false);
                }
            }
        });
    }

    private void registerUserToPreference() {
        preferenceManager.putString(Constants.KEY_EMAIL, binding.editTextEmail.getText().toString().trim());
        preferenceManager.putString(Constants.KEY_USER_ID, currentUser.getUid());


        //(TODO) Redirect the usrer to verify notify page
        finish();
    }

    private void registerUserToDb() {
        currentUser = auth.getCurrentUser();

        Map<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_EMAIL, binding.editTextEmail.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, binding.editTextPassword.getText().toString());
        user.put(Constants.KEY_USERNAME, binding.editTextUsername.getText().toString());

        db.collection(Constants.KEY_USER_LIST).document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        registerUserToPreference();
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
        } else {
            binding.progressBarRegister.setVisibility(View.VISIBLE);
            binding.buttonRegister.setText("Register");
        }
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
}