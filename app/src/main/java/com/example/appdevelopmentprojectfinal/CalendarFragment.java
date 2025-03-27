package com.example.appdevelopmentprojectfinal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.graphics.Color;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarFragment extends Fragment {

    private TextView textViewCalendarTitle;
    private CalendarView calendarView;
    private TextView textViewEvents;
    private RecyclerView recyclerViewEvents;
    
    // Tab views
    private TextView tabTodo;
    private TextView tabEvents;
    private TextView tabAll;
    
    // Current selected tab (0 = Todo, 1 = Events, 2 = All)
    private int currentTab = 1; // Default to Events
    
    // Selected date (milliseconds since epoch)
    private long selectedDate;

    public CalendarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        
        // Initialize views
        textViewCalendarTitle = view.findViewById(R.id.textView_calendar_title);
        calendarView = view.findViewById(R.id.calendarView);
        textViewEvents = view.findViewById(R.id.textView_events);
        recyclerViewEvents = view.findViewById(R.id.recyclerView_events);
        
        // Initialize tabs
        tabTodo = view.findViewById(R.id.tab_todo);
        tabEvents = view.findViewById(R.id.tab_events);
        tabAll = view.findViewById(R.id.tab_all);
        
        // Set default selected date to today
        selectedDate = System.currentTimeMillis();
        
        // Setup tab click listeners
        setupTabListeners();
        
        // Setup calendar date change listener
        setupCalendarListener();
        
        // Initialize the default tab (Events)
        updateTabSelection();
        
        // Load events for today with the default tab
        loadItems();
        
        return view;
    }
    
    private void setupTabListeners() {
        tabTodo.setOnClickListener(v -> {
            currentTab = 0;
            updateTabSelection();
            loadItems();
        });
        
        tabEvents.setOnClickListener(v -> {
            currentTab = 1;
            updateTabSelection();
            loadItems();
        });
        
        tabAll.setOnClickListener(v -> {
            currentTab = 2;
            updateTabSelection();
            loadItems();
        });
    }
    
    private void setupCalendarListener() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Convert date to milliseconds
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
            
            // Only load filtered items for Todo and Events tabs
            if (currentTab != 2) {
                loadItems();
            }
        });
    }
    
    private void updateTabSelection() {
        // Reset all tabs background
        tabTodo.setBackgroundColor(Color.parseColor("#EEEEEE"));
        tabEvents.setBackgroundColor(Color.parseColor("#EEEEEE"));
        tabAll.setBackgroundColor(Color.parseColor("#EEEEEE"));
        
        // Set selected tab background and manage calendar visibility
        switch (currentTab) {
            case 0:
                tabTodo.setBackgroundColor(Color.parseColor("#E0E0E0"));
                textViewEvents.setText(R.string.todo);
                calendarView.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabEvents.setBackgroundColor(Color.parseColor("#E0E0E0"));
                textViewEvents.setText(R.string.events);
                calendarView.setVisibility(View.VISIBLE);
                break;
            case 2:
                tabAll.setBackgroundColor(Color.parseColor("#E0E0E0"));
                textViewEvents.setText(R.string.all);
                calendarView.setVisibility(View.GONE);
                break;
        }
    }
    
    private void loadItems() {
        StringBuilder headerBuilder = new StringBuilder();
        
        switch (currentTab) {
            case 0:
                headerBuilder.append("To-Do");
                break;
            case 1:
                headerBuilder.append("Events");
                break;
            case 2:
                headerBuilder.append("All Task");
                break;
        }
        
        // If not showing "All", append the date
        if (currentTab != 2) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(selectedDate);
            
            headerBuilder.append(" for ");
            headerBuilder.append(calendar.get(java.util.Calendar.MONTH) + 1);
            headerBuilder.append("/");
            headerBuilder.append(calendar.get(java.util.Calendar.DAY_OF_MONTH));
            headerBuilder.append("/");
            headerBuilder.append(calendar.get(java.util.Calendar.YEAR));
        }
        
        textViewEvents.setText(headerBuilder.toString());
    }
} 