package com.example.appdevelopmentprojectfinal.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.ImageView;
import android.util.Log;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;
import com.example.appdevelopmentprojectfinal.utils.DataManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Collections;

public class CalendarFragment extends Fragment implements EventAdapter.OnEventClickListener {
    private static final String TAG = "TimetableApp:CalendarFragment";

    private TextView textViewCalendarTitle;
    private CustomCalendarView customCalendarView;
    private TextView textViewMonth;
    private TextView textViewEvents;
    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;
    
    // Tab views
    private TextView tabTodo;
    private TextView tabEvents;
    private TextView tabAll;
    
    // Current selected tab (0 = Todo, 1 = Events, 2 = All)
    private int currentTab = 1; // Default to Events
    
    // Selected date (milliseconds since epoch)
    private long selectedDate;
    
    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        
        // Initialize views
        textViewCalendarTitle = view.findViewById(R.id.textView_calendar_title);
        customCalendarView = view.findViewById(R.id.customCalendarView);
        textViewMonth = view.findViewById(R.id.textView_month);
        textViewEvents = view.findViewById(R.id.textView_events);
        recyclerViewEvents = view.findViewById(R.id.recyclerView_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);
        
        // Initialize tabs
        tabTodo = view.findViewById(R.id.tab_todo);
        tabEvents = view.findViewById(R.id.tab_events);
        tabAll = view.findViewById(R.id.tab_all);
        
        // Set default selected date to today
        selectedDate = System.currentTimeMillis();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup tab click listeners
        setupTabListeners();
        
        // Setup calendar and month controls
        setupCalendarControls();
        
        // Setup FAB click listener
        setupFabClickListener();
        
        // Update month text (must be called after setupCalendarControls)
        updateMonthText();
        
        // Initialize the default tab (Events)
        updateTabSelection();
        
        // Load events for today with the default tab
        loadItems();
        
        // Update event markers on calendar
        updateCalendarEvents();
        
        return view;
    }
    
    private void setupRecyclerView() {
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(getContext(), new ArrayList<Event>());
        eventAdapter.setOnEventClickListener(this);
        
        // Add todo completion listener
        eventAdapter.setOnTodoCompletedListener((event, isCompleted, position) -> {
            // Update completed status
            event.setCompleted(isCompleted);
            
            // Save to Firebase
            saveEventToFirebase(event);
        });
        
        recyclerViewEvents.setAdapter(eventAdapter);
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
    
    private void setupCalendarControls() {
        // Set date selection listener
        customCalendarView.setOnDateSelectedListener(date -> {
            selectedDate = date.getTimeInMillis();
            
            // Update month text
            updateMonthText();
            
            // Only load filtered items for Todo and Events tabs
            if (currentTab != 2) {
                loadItems();
            }
        });
        
        // Set month change listener
        customCalendarView.setOnMonthChangedListener(newMonth -> {
            updateMonthText();
        });
    }
    
    private void setupFabClickListener() {
        fabAddEvent.setOnClickListener(v -> {
            showAddEventDialog();
        });
    }
    
    private void showAddEventDialog() {
        // Create dialog for adding new event
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Item");
        
        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);
        
        // Find views in dialog
        RadioGroup radioGroupType = dialogView.findViewById(R.id.radioGroup_type);
        RadioButton radioButtonEvent = dialogView.findViewById(R.id.radioButton_event);
        RadioButton radioButtonTodo = dialogView.findViewById(R.id.radioButton_todo);
        EditText editTextTitle = dialogView.findViewById(R.id.editText_title);
        EditText editTextDescription = dialogView.findViewById(R.id.editText_description);
        TextView textViewDate = dialogView.findViewById(R.id.textView_date);
        TextView textViewTime = dialogView.findViewById(R.id.textView_time);
        ImageView imageViewTitleWarning = dialogView.findViewById(R.id.imageView_title_warning);
        
        // Initially hide warning icon
        imageViewTitleWarning.setVisibility(View.GONE);
        
        // Set initial date to selected date
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDate);
        final int[] year = {selectedCal.get(Calendar.YEAR)};
        final int[] month = {selectedCal.get(Calendar.MONTH)};
        final int[] day = {selectedCal.get(Calendar.DAY_OF_MONTH)};
        final int[] hour = {selectedCal.get(Calendar.HOUR_OF_DAY)};
        final int[] minute = {selectedCal.get(Calendar.MINUTE)};
        
        // Format and display initial date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        textViewDate.setText(dateFormat.format(selectedCal.getTime()));
        textViewTime.setText(timeFormat.format(selectedCal.getTime()));
        
        // Set click listener for date selection - using Material Date Picker
        textViewDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(selectedCal.getTimeInMillis())
                .build();
            
            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Adjust for time zone
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                calendar.setTimeZone(TimeZone.getDefault());
                
                // Keep time from previous selection
                calendar.set(Calendar.HOUR_OF_DAY, hour[0]);
                calendar.set(Calendar.MINUTE, minute[0]);
                
                // Update values
                year[0] = calendar.get(Calendar.YEAR);
                month[0] = calendar.get(Calendar.MONTH);
                day[0] = calendar.get(Calendar.DAY_OF_MONTH);
                selectedCal.set(Calendar.YEAR, year[0]);
                selectedCal.set(Calendar.MONTH, month[0]);
                selectedCal.set(Calendar.DAY_OF_MONTH, day[0]);
                
                textViewDate.setText(dateFormat.format(calendar.getTime()));
            });
            
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
        
        // Set click listener for time selection - using Material Time Picker
        textViewTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour[0])
                .setMinute(minute[0])
                .setTitleText("Select Time")
                .build();
            
            timePicker.addOnPositiveButtonClickListener(view -> {
                hour[0] = timePicker.getHour();
                minute[0] = timePicker.getMinute();
                
                selectedCal.set(Calendar.HOUR_OF_DAY, hour[0]);
                selectedCal.set(Calendar.MINUTE, minute[0]);
                
                textViewTime.setText(timeFormat.format(selectedCal.getTime()));
            });
            
            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });
        
        // Create AlertDialog
        AlertDialog dialog = builder.create();
        
        // Add action buttons
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save", (DialogInterface.OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog1, which) -> dialog1.cancel());
        
        dialog.show();
        
        // Override the positive button click to handle validation without dismissing dialog
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate input
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            
            if (title.isEmpty()) {
                // Show warning icon
                imageViewTitleWarning.setVisibility(View.VISIBLE);
                // Don't dismiss dialog
                return;
            } else {
                // Hide warning icon if previously shown
                imageViewTitleWarning.setVisibility(View.GONE);
            }
            
            // Check if event or todo is selected
            int eventType = radioButtonEvent.isChecked() ? Event.TYPE_EVENT : Event.TYPE_TODO;
            
            // Create new item
            Event newItem = new Event(title, description, selectedCal.getTime(), eventType);
            
            // Save to Firebase
            saveEventToFirebase(newItem);
            
            // Dismiss the dialog
            dialog.dismiss();
        });
    }
    
    private void saveEventToFirebase(Event event) {
        // Use CalendarHelper instead of direct DataManager call
        CalendarHelper.saveEvent(requireContext(), event, new CalendarHelper.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                // Update event markers on calendar
                Calendar calendar = getCalendarFromDate(event.getDate());
                int type = event.getType() == Event.TYPE_EVENT ? 
                    CustomCalendarView.TYPE_EVENT : CustomCalendarView.TYPE_TODO;
                customCalendarView.addEvent(calendar, type);
                
                // Refresh display
                loadItems();
            }

            @Override
            public void onError(String errorMessage) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error saving event: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void updateMonthText() {
        // Use current calendar from the custom calendar view
        Calendar cal = customCalendarView.getCurrentMonth();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthText = sdf.format(cal.getTime());
        textViewMonth.setText(monthText);
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
                customCalendarView.setVisibility(View.VISIBLE);
                textViewMonth.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabEvents.setBackgroundColor(Color.parseColor("#E0E0E0"));
                textViewEvents.setText(R.string.events);
                customCalendarView.setVisibility(View.VISIBLE);
                textViewMonth.setVisibility(View.VISIBLE);
                break;
            case 2:
                tabAll.setBackgroundColor(Color.parseColor("#E0E0E0"));
                textViewEvents.setText(R.string.all);
                customCalendarView.setVisibility(View.GONE);
                textViewMonth.setVisibility(View.GONE);
                break;
        }
    }
    
    private void loadItems() {
        StringBuilder headerBuilder = new StringBuilder();
        List<Event> allEvents = DataManager.getInstance().getAllEvents();
        List<Event> filteredEvents = new ArrayList<>();
        
        // Filter events based on the selected tab and date
        for (Event event : allEvents) {
            // Check if event is on the selected date for Todo and Events tabs
            boolean isOnSelectedDate = isSameDay(event.getDate(), new Date(selectedDate));
            
            if (currentTab == 0 && event.isTodo() && (isOnSelectedDate || currentTab == 2)) {
                filteredEvents.add(event);
            } else if (currentTab == 1 && event.isEvent() && (isOnSelectedDate || currentTab == 2)) {
                filteredEvents.add(event);
            } else if (currentTab == 2) {
                filteredEvents.add(event);
            }
        }
        
        // If showing "All", sort events by time remaining (closest to expiration first)
        if (currentTab == 2) {
            Collections.sort(filteredEvents, (event1, event2) -> {
                // Special case: if either event is completed, put completed ones at the end
                if (event1.isTodo() && event2.isTodo()) {
                    if (event1.isCompleted() && !event2.isCompleted()) {
                        return 1; // event1 (completed) goes after event2 (not completed)
                    }
                    if (!event1.isCompleted() && event2.isCompleted()) {
                        return -1; // event1 (not completed) goes before event2 (completed)
                    }
                    if (event1.isCompleted() && event2.isCompleted()) {
                        return 0; // Both completed, order doesn't matter
                    }
                }
                
                // For non-completed events or regular events, sort by date
                return event1.getDate().compareTo(event2.getDate());
            });
        }
        
        // Update adapter with filtered events
        eventAdapter.updateEvents(filteredEvents);
        
        // Build header text
        switch (currentTab) {
            case 0:
                headerBuilder.append(getString(R.string.todo));
                break;
            case 1:
                headerBuilder.append(getString(R.string.events));
                break;
            case 2:
                headerBuilder.append(getString(R.string.all_tasks));
                break;
        }
        
        // If not showing "All", append the date in a more compact format
        if (currentTab != 2) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(selectedDate);
            
            headerBuilder.append(" ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            headerBuilder.append(sdf.format(calendar.getTime()));
        }
        
        textViewEvents.setText(headerBuilder.toString());
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    @Override
    public void onEventClick(Event event, int position) {
        // Display event details or edit event
        showEventDetailsDialog(event, position);
    }
    
    private void showEventDetailsDialog(Event event, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(event.isEvent() ? "Event Details" : "Todo Details");
        
        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_details, null);
        builder.setView(dialogView);
        
        // Find views in dialog
        TextView textViewTitle = dialogView.findViewById(R.id.textView_details_title);
        TextView textViewDescription = dialogView.findViewById(R.id.textView_details_description);
        TextView textViewDate = dialogView.findViewById(R.id.textView_details_date);
        TextView textViewTime = dialogView.findViewById(R.id.textView_details_time);
        
        // Set values
        textViewTitle.setText(event.getTitle());
        textViewDescription.setText(event.getDescription());
        
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        textViewDate.setText(dateFormat.format(event.getDate()));
        textViewTime.setText(timeFormat.format(event.getDate()));
        
        // Add action buttons
        builder.setPositiveButton("Edit", (dialog, which) -> showEditEventDialog(event, position));
        
        builder.setNegativeButton("Delete", (dialog, which) -> {
            // Delete event from Firebase
            deleteEventFromFirebase(event);
        });
        
        builder.setNeutralButton("Close", (dialog, which) -> dialog.dismiss());
        
        // Show dialog
        builder.create().show();
    }
    
    private void deleteEventFromFirebase(Event event) {
        // Use CalendarHelper instead of direct DataManager call
        CalendarHelper.deleteEvent(requireContext(), event, new CalendarHelper.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                // Update event markers on calendar
                updateCalendarEvents();
                
                // Refresh display
                loadItems();
            }

            @Override
            public void onError(String errorMessage) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error deleting event: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void showEditEventDialog(Event event, int position) {
        // Create dialog for editing event
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit " + (event.isEvent() ? "Event" : "Todo"));
        
        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);
        
        // Find views in dialog
        RadioGroup radioGroupType = dialogView.findViewById(R.id.radioGroup_type);
        RadioButton radioButtonEvent = dialogView.findViewById(R.id.radioButton_event);
        RadioButton radioButtonTodo = dialogView.findViewById(R.id.radioButton_todo);
        EditText editTextTitle = dialogView.findViewById(R.id.editText_title);
        EditText editTextDescription = dialogView.findViewById(R.id.editText_description);
        TextView textViewDate = dialogView.findViewById(R.id.textView_date);
        TextView textViewTime = dialogView.findViewById(R.id.textView_time);
        ImageView imageViewTitleWarning = dialogView.findViewById(R.id.imageView_title_warning);
        
        // Initially hide warning icon
        imageViewTitleWarning.setVisibility(View.GONE);
        
        // Set initial values from event
        editTextTitle.setText(event.getTitle());
        editTextDescription.setText(event.getDescription());
        
        // Set event type radio button
        if (event.getType() == Event.TYPE_EVENT) {
            radioButtonEvent.setChecked(true);
        } else {
            radioButtonTodo.setChecked(true);
        }
        
        // Set up calendar with event date
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(event.getDate());
        final int[] year = {eventCal.get(Calendar.YEAR)};
        final int[] month = {eventCal.get(Calendar.MONTH)};
        final int[] day = {eventCal.get(Calendar.DAY_OF_MONTH)};
        final int[] hour = {eventCal.get(Calendar.HOUR_OF_DAY)};
        final int[] minute = {eventCal.get(Calendar.MINUTE)};
        
        // Format and display initial date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        textViewDate.setText(dateFormat.format(eventCal.getTime()));
        textViewTime.setText(timeFormat.format(eventCal.getTime()));
        
        // Set click listener for date selection - using Material Date Picker
        textViewDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(eventCal.getTimeInMillis())
                .build();
            
            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Adjust for time zone
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                calendar.setTimeZone(TimeZone.getDefault());
                
                // Keep time from previous selection
                calendar.set(Calendar.HOUR_OF_DAY, hour[0]);
                calendar.set(Calendar.MINUTE, minute[0]);
                
                // Update values
                year[0] = calendar.get(Calendar.YEAR);
                month[0] = calendar.get(Calendar.MONTH);
                day[0] = calendar.get(Calendar.DAY_OF_MONTH);
                eventCal.set(Calendar.YEAR, year[0]);
                eventCal.set(Calendar.MONTH, month[0]);
                eventCal.set(Calendar.DAY_OF_MONTH, day[0]);
                
                textViewDate.setText(dateFormat.format(calendar.getTime()));
            });
            
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
        
        // Set click listener for time selection - using Material Time Picker
        textViewTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour[0])
                .setMinute(minute[0])
                .setTitleText("Select Time")
                .build();
            
            timePicker.addOnPositiveButtonClickListener(view -> {
                hour[0] = timePicker.getHour();
                minute[0] = timePicker.getMinute();
                
                eventCal.set(Calendar.HOUR_OF_DAY, hour[0]);
                eventCal.set(Calendar.MINUTE, minute[0]);
                
                textViewTime.setText(timeFormat.format(eventCal.getTime()));
            });
            
            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });
        
        // Create AlertDialog
        AlertDialog dialog = builder.create();
        
        // Add action buttons
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save", (DialogInterface.OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog1, which) -> dialog1.cancel());
        
        dialog.show();
        
        // Override the positive button click to handle validation without dismissing dialog
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate input
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            
            if (title.isEmpty()) {
                // Show warning icon
                imageViewTitleWarning.setVisibility(View.VISIBLE);
                // Don't dismiss dialog
                return;
            } else {
                // Hide warning icon if previously shown
                imageViewTitleWarning.setVisibility(View.GONE);
            }
            
            // Update event type
            int eventType = radioButtonEvent.isChecked() ? Event.TYPE_EVENT : Event.TYPE_TODO;
            
            // Update event
            event.setTitle(title);
            event.setDescription(description);
            event.setType(eventType);
            event.setDate(eventCal.getTime());
            
            // Save to Firebase
            saveEventToFirebase(event);
            
            // Dismiss the dialog
            dialog.dismiss();
        });
    }
    
    /**
     * Updates all event markers on the calendar
     */
    private void updateCalendarEvents() {
        // Clear all events first
        customCalendarView.clearEvents();
        
        // Add markers for all events
        List<Event> allEvents = DataManager.getInstance().getAllEvents();
        for (Event event : allEvents) {
            Calendar calendar = getCalendarFromDate(event.getDate());
            if (event.getType() == Event.TYPE_EVENT) {
                customCalendarView.addEvent(calendar, CustomCalendarView.TYPE_EVENT);
            } else {
                customCalendarView.addEvent(calendar, CustomCalendarView.TYPE_TODO);
            }
        }
    }
    
    /**
     * Converts java.util.Date to java.util.Calendar
     */
    private Calendar getCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Load events from both local storage and Firebase
        CalendarHelper.loadAllEvents(requireContext());
        
        // Update calendar display
        updateCalendarEvents();
        
        // Load event list
        loadItems();
    }
} 