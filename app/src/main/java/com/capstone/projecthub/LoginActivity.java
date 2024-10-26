package com.capstone.projecthub;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.projecthub.PreferenceManager.Constants;
import com.capstone.projecthub.PreferenceManager.PreferenceManager;
import com.capstone.projecthub.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private PreferenceManager preferenceManager;
    private Button button;
    private FirebaseUser currentUser;

    @Override
    protected void onStart() {
        super.onStart();

        //check if the user is signed in
        //Please DO NOT USER FIREBASE DOCUMENTED METHOD TO CHECK
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.contains(Constants.KEY_EMAIL)) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

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

        checkInternetAvailable();
        init();
        setListeners();
    }

    private void checkInternetAvailable() {
        Handler handler = new Handler();

        Runnable checkInternetTask = new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!InternetIsConnected() || !NetworkIsConnected()) {
                            Dialog dialog = new Dialog(LoginActivity.this);
                            dialog.setContentView(R.layout.no_internet_dialog);
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.setCancelable(false);
                            dialog.show();

                            button = dialog.findViewById(R.id.buttonInternetRetry);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    finishAndRemoveTask(); //relaunch app
                                }
                            });
                        }
                    }
                });
            }
        };

        Thread threadForInternetStatus = new Thread(checkInternetTask);
        threadForInternetStatus.start(); //no need to free the thread as Java will automatically free it
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
    }

    private void setListeners() {
        binding.textSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoginButtonLoading(true);

                //check if the credentials are valid. Of course it is. What do you expect isCredentialsValid() gonna do??
                boolean credentialsValid = isCredentialsValid();

                if (credentialsValid) {
                    signIn();
                } else {
                    isLoginButtonLoading(false);
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
    }

    private void signIn() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //sign in is success
                        if (task.isSuccessful()) {
                            currentUser = auth.getCurrentUser();

                            if (currentUser.isEmailVerified()) {
                                preferenceManager.putString(Constants.KEY_EMAIL, email);
                                preferenceManager.putString(Constants.KEY_USER_ID, currentUser.getUid());
                                preferenceManager.putString(Constants.KEY_USERNAME, currentUser.getDisplayName());

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                auth.signOut();
                                Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                                isLoginButtonLoading(false);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        isLoginButtonLoading(false);
                    }
                });
    }

    private void isLoginButtonLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBarLogin.setVisibility(View.VISIBLE);
            binding.buttonLogin.setText("");
            binding.buttonLogin.setOnClickListener(null);
        } else {
            binding.progressBarLogin.setVisibility(View.GONE);
            binding.buttonLogin.setText("Login");
            binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isLoginButtonLoading(true);

                    //check if the credentials are valid. Of course it is. What do you expect isCredentialsValid() gonna do??
                    boolean credentialsValid = isCredentialsValid();

                    if (credentialsValid) {
                        signIn();
                    } else {
                        isLoginButtonLoading(false);
                    }
                }
            });
        }
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

    private boolean NetworkIsConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null;
    }

    private boolean InternetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
}