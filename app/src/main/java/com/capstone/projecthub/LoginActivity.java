package com.capstone.projecthub;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //CAUTION: REMEMBER TO STORE USER DETAILS IN DATABASE TOO
    //(TODO) Do a dialog that will check the internet connection status

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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

    private void init() {
        auth = FirebaseAuth.getInstance();
    }

    private void setListeners() {
        binding.textSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(TODO)redirect to register activity
            }
        });
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the credentials are valid. Of course it is. What do you expect isCredentialsValid() gonna do??
                boolean credentialsValid = isCredentialsValid();

                if (credentialsValid) {
                    signIn();
                }
            }
        });
    }

    private void signIn() {
        auth.signInWithEmailAndPassword(binding.editTextEmail.getText().toString(),
                binding.editTextPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //sign in is success
                        //(TODO)store the value into preference manager
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isCredentialsValid() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email)) { //if email ET is empty
            Toast.makeText(this, "Please fill in your email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in your password", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!(Patterns.EMAIL_ADDRESS.matcher(email).matches())) { //if the email ET text is not in email form
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() < 6) { //for some reason Firebase requires >= 6 char long
            Toast.makeText(this, "Password must be 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}