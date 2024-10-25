package com.capstone.projecthub;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.capstone.projecthub.databinding.ActivityHomeBinding;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkInternetAvailable();

        final Fragment fragmentHome = new HomeFragment();
        final Fragment fragmentProjects = new ProjectListsFragment();
        final Fragment fragmentTasks = new TasksFragment();
        final Fragment fragmentAnnouncement = new AnnouncementFragment();
        final Fragment fragmentProfile = new ProfileFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();

        final Fragment[] activeFragment = {fragmentHome};

        fragmentManager.beginTransaction().add(R.id.frameHome, fragmentHome, "1").commit();
        fragmentManager.beginTransaction().add(R.id.frameHome, fragmentProjects, "2").hide(fragmentProjects).commit();
        fragmentManager.beginTransaction().add(R.id.frameHome, fragmentTasks, "3").hide(fragmentTasks).commit();
        fragmentManager.beginTransaction().add(R.id.frameHome, fragmentAnnouncement, "4").hide(fragmentAnnouncement).commit();
        fragmentManager.beginTransaction().add(R.id.frameHome, fragmentProfile, "5").hide(fragmentProfile).commit();

        binding.naviBarHome.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selectedItemId = item.getItemId();

                if (selectedItemId == R.id.home) {
                    fragmentManager.beginTransaction().hide(activeFragment[0]).show(fragmentHome).commit();
                    activeFragment[0] = fragmentHome;
                    return true;
                } else if (selectedItemId == R.id.projectList) {
                    fragmentManager.beginTransaction().hide(activeFragment[0]).show(fragmentProjects).commit();
                    activeFragment[0] = fragmentProjects;
                    return true;
                } else if (selectedItemId == R.id.tasks) {
                    fragmentManager.beginTransaction().hide(activeFragment[0]).show(fragmentTasks).commit();
                    activeFragment[0] = fragmentTasks;
                    return true;
                } else if (selectedItemId == R.id.announcements) {
                    fragmentManager.beginTransaction().hide(activeFragment[0]).show(fragmentAnnouncement).commit();
                    activeFragment[0] = fragmentAnnouncement;
                    return true;
                } else if (selectedItemId == R.id.profile) {
                    fragmentManager.beginTransaction().hide(activeFragment[0]).show(fragmentProfile).commit();
                    activeFragment[0] = fragmentProfile;
                    return true;
                } else {
                    return false;
                }
            }
        });
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
                            Dialog dialog = new Dialog(HomeActivity.this);
                            dialog.setContentView(R.layout.no_internet_dialog);
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.setCancelable(false);
                            dialog.show();

                            Button button = dialog.findViewById(R.id.buttonInternetRetry);
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