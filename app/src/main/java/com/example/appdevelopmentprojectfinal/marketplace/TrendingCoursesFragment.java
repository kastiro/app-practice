package com.example.appdevelopmentprojectfinal.marketplace;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;
import com.example.appdevelopmentprojectfinal.model.Course;
import com.example.appdevelopmentprojectfinal.model.User;
import com.example.appdevelopmentprojectfinal.utils.DataManager;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class TrendingCoursesFragment extends Fragment implements CourseAdapter.CourseClickListener {

    private TextView tvWalletBalance;
    private TextView tvSectionTitle;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private TextView tvEmptyMessage;
    
    private CourseAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marketplace_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);
        tvSectionTitle = view.findViewById(R.id.tvSectionTitle);
        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        
        // Set section title
        tvSectionTitle.setText(getString(R.string.trending_courses));
        
        // Set up wallet balance
        updateWalletBalance();
        
        // Set up search functionality
        setUpSearch();
        
        // Load and display trending courses
        loadTrendingCourses();
    }
    
    private void setUpSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            searchCourses(etSearch.getText().toString());
            return true;
        });
    }
    
    private void searchCourses(String query) {
        List<Course> searchResults = DataManager.getInstance().searchCourses(query);
        updateCoursesList(searchResults);
    }
    
    private void loadTrendingCourses() {
        List<Course> trendingCourses = DataManager.getInstance().getTrendingCourses();
        Log.d("TrendingCoursesFragment", "Loaded " + trendingCourses.size() + " trending courses");
        updateCoursesList(trendingCourses);
    }
    
    private void updateCoursesList(List<Course> courses) {
        if (courses.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(getString(R.string.no_trending_courses));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            
            // Create and set adapter if not already created
            if (adapter == null) {
                adapter = new CourseAdapter(courses, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateCourses(courses);
            }
        }
    }
    
    private void updateWalletBalance() {
        User currentUser = DataManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.GERMANY); // Using Euro format
            format.setCurrency(Currency.getInstance("EUR"));
            String balanceText = getString(R.string.wallet_balance, format.format(currentUser.getWallet()));
            tvWalletBalance.setText(balanceText);
        }
    }
    
    @Override
    public void onCourseClicked(Course course) {
        // Show course detail dialog
        CourseDetailDialog dialog = CourseDetailDialog.newInstance(course.getId());
        dialog.show(getParentFragmentManager(), "CourseDetail");
        
        // Set callback for purchase completion
        dialog.setPurchaseCompletedListener(() -> {
            // Refresh trending courses and wallet balance
            loadTrendingCourses();
            updateWalletBalance();
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadTrendingCourses();
        updateWalletBalance();
    }
}