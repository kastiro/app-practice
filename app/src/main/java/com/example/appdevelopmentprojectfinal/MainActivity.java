package com.example.appdevelopmentprojectfinal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appdevelopmentprojectfinal.databinding.ActivityMainBinding;
import com.example.appdevelopmentprojectfinal.timetable.TimetableFragment;
import com.example.appdevelopmentprojectfinal.calendar.CalendarFragment;
//import com.example.appdevelopmentprojectfinal.timetable.TimetableNotificationManager;

import android.util.Log;

import com.example.appdevelopmentprojectfinal.utils.JsonUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.appdevelopmentprojectfinal.model.User;
import com.example.appdevelopmentprojectfinal.utils.DataManager;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Testing user loading from firebase
        DataManager.getInstance().initialize(this, "user1", new DataManager.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                Log.d("MainActivity", "User loaded successfully: " + user.getFullName() + " - Wallet: €" + user.getWallet());

                setupNavigation();
                replaceFragment(new HomepageFragment());
            }

            @Override
            public void onError(String error) {
                Log.e("MainActivity", "Error loading user: " + error);
            }
        });


        // Create notification channel and load fragments
//        TimetableNotificationManager.createNotificationChannel(this);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomepageFragment());
            } else if (itemId == R.id.market) {
                replaceFragment(new StoreFragment());
            } else if (itemId == R.id.timetable) {
                replaceFragment(new TimetableFragment());
            } else if (itemId == R.id.calendar) {
                replaceFragment(new CalendarFragment());
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
    private void setupNavigation() {
//        TimetableNotificationManager.createNotificationChannel(this);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomepageFragment());
            } else if (itemId == R.id.market) {
                replaceFragment(new StoreFragment());
            } else if (itemId == R.id.timetable) {
                replaceFragment(new TimetableFragment());
            } else if (itemId == R.id.calendar) {
                replaceFragment(new CalendarFragment());
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });
    }

}