package com.example.appdevelopmentprojectfinal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appdevelopmentprojectfinal.marketplace.RecommendedCoursesFragment;
import com.example.appdevelopmentprojectfinal.marketplace.TrendingCoursesFragment;
import com.example.appdevelopmentprojectfinal.model.User;
import com.example.appdevelopmentprojectfinal.utils.DataManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// Shows marketplace with tabs for different course categories
public class StoreFragment extends Fragment {
    private static final String TAG = "TimetableApp:StoreFragment";
    
    // Fragment argument keys for future filter implementation
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_FILTER = "filter";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public StoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Arguments processing can be added here if needed in the future

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Removed local DataManager initialization so added below incase
        User user = DataManager.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "User loaded: " + user.getFullName());
        }

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Set up ViewPager with the tabs
        setUpViewPager();
    }

    // Setup tabs for viewpager
    private void setUpViewPager() {
        // Create adapter for the viewpager
        StoreTabAdapter adapter = new StoreTabAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Recommended");
                    break;
                case 1:
                    tab.setText("Trending");
                    break;
            }
        }).attach();
    }

    // Tab adapter for marketplace sections
    private static class StoreTabAdapter extends FragmentStateAdapter {
        public StoreTabAdapter(@NonNull FragmentManager fragmentManager, @NonNull androidx.lifecycle.Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        // Create fragment based on tab position
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new RecommendedCoursesFragment();
                case 1:
                    return new TrendingCoursesFragment();
                default:
                    return new RecommendedCoursesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Number of tabs (Recommended and Trending)
        }
    }
}