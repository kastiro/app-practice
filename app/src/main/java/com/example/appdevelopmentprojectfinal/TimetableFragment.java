package com.example.appdevelopmentprojectfinal;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TimetableFragment extends Fragment {
    private static final String TIMETABLE_FILENAME = "timetable.json";

    private final List<ModuleSchedule> moduleSchedules = new ArrayList<>();
    private TableLayout timetableGrid;
    private TextView emptyView;

    // Define time slots for the timetable
    private static final String[] TIME_SLOTS = {
            "09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00",
            "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"
    };

    // Define days for the timetable
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    // Colors for different modules
    private static final int[] MODULE_COLORS = {
            Color.parseColor("#FFCDD2"), // Light Red
            Color.parseColor("#C8E6C9"), // Light Green
            Color.parseColor("#BBDEFB"), // Light Blue
            Color.parseColor("#FFE0B2"), // Light Orange
            Color.parseColor("#E1BEE7")  // Light Purple
    };

    public TimetableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timetable, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        timetableGrid = view.findViewById(R.id.timetable_grid);
        emptyView = view.findViewById(R.id.empty_view);

        // Load timetable data
        loadTimetableData();

        // Display the timetable
        displayTimetable();
    }

    private void displayTimetable() {
        // if timetable is empty we make emptyView (a message notice) visible
        if (moduleSchedules.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        // Here, the emptyView message will be gone and we continue.
        emptyView.setVisibility(View.GONE);

        // Create a map to store modules by time and day
        Map<String, Map<String, List<ModuleSchedule>>> timetableMap = new HashMap<>();

        // Gathering the time and day information (module information)
        for (String timeSlot : TIME_SLOTS) {
            timetableMap.put(timeSlot, new HashMap<>());
            for (String day : DAYS) {
                Map<String, List<ModuleSchedule>> dayMap = timetableMap.get(timeSlot);
            if (dayMap != null) {
                dayMap.put(day, new ArrayList<>());
            }
            }
        }

        // Populate the map with module schedules
        for (ModuleSchedule schedule : moduleSchedules) {
            TimeSlot slot = schedule.getTimeSlot();
            String day = slot.getDay();
            String startTime = slot.getStartTime();
            String endTime = slot.getEndTime();

            // Find all time slots that this module spans
            for (String timeSlot : TIME_SLOTS) {
                String[] times = timeSlot.split("-");
                String slotStart = times[0];

                // If this time slot is within the module's time range
                if (isTimeInRange(slotStart, startTime, endTime)) {
                    Map<String, List<ModuleSchedule>> dayMap = timetableMap.get(timeSlot);
                    if (dayMap != null) {
                        List<ModuleSchedule> schedules = dayMap.get(day);
                        if (schedules != null) {
                            schedules.add(schedule);
                        }
                    }
                }
            }
        }

        // Create the timetable rows
        LayoutInflater inflater = getLayoutInflater();

        for (String timeSlot : TIME_SLOTS) {
            TableRow row = new TableRow(getContext());

            // Add time label
            TextView timeLabel = new TextView(getContext());
            timeLabel.setText(timeSlot);
            timeLabel.setPadding(8, 8, 8, 8);
            timeLabel.setWidth(250);
            row.addView(timeLabel);

            // Add cells for each day
            for (String day : DAYS) {
                List<ModuleSchedule> schedulesForSlot = new ArrayList<>();
                Map<String, List<ModuleSchedule>> dayMap = timetableMap.get(timeSlot);
                if (dayMap != null) {
                    List<ModuleSchedule> slots = dayMap.get(day);
                    if (slots != null) {
                        schedulesForSlot = slots;
                    }
                }

                if (schedulesForSlot.isEmpty()) {
                    // Empty cell
                    View emptyCell = new View(getContext());
                    TableRow.LayoutParams params = new TableRow.LayoutParams(300, 150);
                    params.setMargins(2, 2, 2, 2);
                    emptyCell.setLayoutParams(params);
                    emptyCell.setBackgroundColor(Color.LTGRAY);
                    row.addView(emptyCell);
                } else {
                    // Module cell
                    // TODO: Handle multiple modules in the same time slot properly
                    ModuleSchedule schedule = schedulesForSlot.get(0); // Just take the first one if multiple
                    Module module = schedule.getModule();

                    // Inflate the module item layout
                    View moduleView = inflater.inflate(R.layout.item_timetable_module, null);

                    // Set module details
                    TextView codeText = moduleView.findViewById(R.id.module_code);
                    TextView nameText = moduleView.findViewById(R.id.module_name);
                    TextView locationText = moduleView.findViewById(R.id.module_location);

                    codeText.setText(module.getCode());
                    nameText.setText(module.getName());
                    locationText.setText(schedule.getTimeSlot().getLocation());

                    // Set a background color based on the module code
                    CardView cardView = (CardView) moduleView;
                    int colorIndex = Math.abs(module.getCode().hashCode()) % MODULE_COLORS.length;
                    cardView.setCardBackgroundColor(MODULE_COLORS[colorIndex]);

                    // Set layout parameters
                    TableRow.LayoutParams params = new TableRow.LayoutParams(120, 150);
                    params.setMargins(2, 2, 2, 2);
                    moduleView.setLayoutParams(params);

                    // Add click listener for rescheduling
                    if (schedule.isMovable()) {
                        moduleView.setOnClickListener(v -> handleModuleClick(schedule));
                    }

                    row.addView(moduleView);
                }
            }

            timetableGrid.addView(row);
        }
    }

    private boolean isTimeInRange(String timeToCheck, String startTime, String endTime) {
        // Simple string comparison for HH:MM format
        // This assumes all times are in 24-hour format
        return timeToCheck.compareTo(startTime) >= 0 && timeToCheck.compareTo(endTime) < 0;
    }

    private void handleModuleClick(ModuleSchedule schedule) {
        // Display basic module information for now
        String moduleInfo = String.format("%s\n%s\n%s", 
                schedule.getModule().toString(),
                schedule.getTimeSlot().toString(),
                schedule.isMovable() ? "Can be rescheduled" : "Fixed schedule");
                
        Toast.makeText(requireContext(), moduleInfo, Toast.LENGTH_LONG).show();
        
        // TODO: Implement rescheduling functionality in a future update
        // This will involve displaying alternative slots from the JSON and
        // allowing the user to select one
    }

    private void loadTimetableData() {
        try {
            // Reading the JSON file
            String jsonString = loadJSONFromAsset();
            if (jsonString == null) {
                Log.e("TimetableFragment", "Failed to load timetable JSON");
                Toast.makeText(requireContext(), "Failed to load timetable data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the JSON
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray modulesArray = jsonObject.getJSONArray("modules");

            // Clear existing data
            moduleSchedules.clear();

            // Process each module
            for (int i = 0; i < modulesArray.length(); i++) {
                JSONObject moduleObj = modulesArray.getJSONObject(i);

                // Create module object
                Module module = new Module(
                        moduleObj.getString("code"),
                        moduleObj.getString("name"),
                        moduleObj.getString("lecturer")
                );

                // Process current slots
                JSONArray slotsArray = moduleObj.getJSONArray("slots");
                for (int j = 0; j < slotsArray.length(); j++) {
                    JSONObject slotObj = slotsArray.getJSONObject(j);
                    
                    // Create time slot
                    TimeSlot timeSlot = new TimeSlot(
                            slotObj.getString("day"),
                            slotObj.getString("startTime"),
                            slotObj.getString("endTime"),
                            slotObj.getString("location")
                    );

                    // isMovable flag for future rescheduling functionality
                    boolean isMovable = slotObj.getBoolean("isMovable");

                    moduleSchedules.add(new ModuleSchedule(module, timeSlot, isMovable));
                }
            }

            Log.d("TimetableFragment", "Loaded " + moduleSchedules.size() + " module schedules");

        } catch (JSONException e) {
            Log.e("TimetableFragment", "JSON parsing error: " + e.getMessage());
            Toast.makeText(requireContext(), "Error parsing timetable data", Toast.LENGTH_SHORT).show();
        }
    }

    private String loadJSONFromAsset() {
        try {
            InputStream inputStream = requireActivity().getAssets().open(TIMETABLE_FILENAME);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            int bytesRead = inputStream.read(buffer);
            inputStream.close();
            if (bytesRead != size) {
                Log.e("TimetableFragment", "Failed to read entire file. Expected: " + size + ", Read: " + bytesRead);
            }
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e("TimetableFragment", "Error loading JSON: " + e.getMessage());
            return null;
        }
    }
}