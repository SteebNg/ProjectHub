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

import com.capstone.projecthub.databinding.ActivityResetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
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

    private void isResetButtonLoading(boolean isLoading) {
        if (isLoading) {
            binding.textResetPasswordInButton.setVisibility(View.GONE);
            binding.progressBarResetPassword.setVisibility(View.VISIBLE);
            binding.buttonResetPassword.setOnClickListener(null);
        } else {
            binding.textResetPasswordInButton.setVisibility(View.VISIBLE);
            binding.progressBarResetPassword.setVisibility(View.GONE);
            setListeners();
        }
    }

    private void setListeners() {
        binding.buttonResetPasswordBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailGetter();
                isResetButtonLoading(true);
                if (email != null) {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, "A password reset email is sent", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                isResetButtonLoading(false);
                            }
                        }
                    });
                } else {
                    isResetButtonLoading(false);
                }
            }
        });
    }

    private String emailGetter() {
        String email = binding.editTextEmailResetPassword.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please fill in the email", Toast.LENGTH_SHORT).show();
            return null;
        } else if (!(Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            return email;
        }
    }

    private void init() {
        isResetButtonLoading(false);
        auth = FirebaseAuth.getInstance();
    }
}