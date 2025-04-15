package com.example.appdevelopmentprojectfinal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appdevelopmentprojectfinal.databinding.ActivityMainBinding;
import com.example.appdevelopmentprojectfinal.timetable.TimetableFragment;
import com.example.appdevelopmentprojectfinal.calendar.CalendarFragment;
import com.example.appdevelopmentprojectfinal.utils.JsonUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TIMETABLE_FILENAME = "timetable.json";

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        JsonUtil jsonUtil = new JsonUtil();
        jsonUtil.copyFileToInternalStorageIfNeeded(this, TIMETABLE_FILENAME);

        replaceFragment(new HomepageFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}