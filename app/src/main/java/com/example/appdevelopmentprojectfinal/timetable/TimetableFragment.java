package com.example.appdevelopmentprojectfinal.timetable;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
import androidx.appcompat.widget.SwitchCompat;
public class TimetableFragment extends Fragment implements ModuleManagementAdapter.OnModuleVisibilityChangedListener {
    private static final String TIMETABLE_FILENAME = "timetable.json";

    private final List<ModuleSchedule> moduleSchedules = new ArrayList<>();
    private TableLayout timetableGrid;
    private TextView emptyView;
    private RecyclerView moduleListView;
    private ModuleManagementAdapter moduleAdapter;

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
        moduleListView = view.findViewById(R.id.module_list);

        // Setup RecyclerView for module management
        moduleListView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load timetable data
        loadTimetableData();

        // Setup module adapter
        moduleAdapter = new ModuleManagementAdapter(moduleSchedules, this);
        moduleListView.setAdapter(moduleAdapter);

        // Display the timetable
        displayTimetable();
    }

    // Callback for module visibility changes
    @Override
    public void onModuleVisibilityChanged() {
        // Refresh the timetable when module visibility changed
        timetableGrid.removeAllViews();
        displayTimetable();
    }


    private void displayTimetable() {
        // Clear existing content to avoid duplication
        timetableGrid.removeAllViews();

        // if timetable is empty we make (a message notice) visible
        if (moduleSchedules.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        // Here, the emptyView message will be gone and we continue.
        emptyView.setVisibility(View.GONE);

        // Create a map to store modules by time and day
        Map<String, Map<String, List<ModuleSchedule>>> timetableMap = new HashMap<>();

        // Gathering the time and day information (module)
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
        LayoutInflater inflater = LayoutInflater.from(getContext());

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
                        // Filter out hidden that are currently hidden (upgradable)
                        for (ModuleSchedule slot : slots) {
                            if (slot.isVisible()) {
                                schedulesForSlot.add(slot);
                            }
                        }
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

                    // Inflate  module item layout
                    View moduleView = inflater.inflate(R.layout.item_timetable_module, null);

                    // Setting module details
                    TextView codeText = moduleView.findViewById(R.id.module_code);
                    TextView nameText = moduleView.findViewById(R.id.module_name);
                    TextView locationText = moduleView.findViewById(R.id.module_location);

                    codeText.setText(module.getCode());
                    nameText.setText(module.getName());
                    locationText.setText(schedule.getTimeSlot().getLocation());

                    // Sets a background color based on module code
                    CardView cardView = (CardView) moduleView;
                    int colorIndex = Math.abs(module.getCode().hashCode()) % MODULE_COLORS.length;
                    cardView.setCardBackgroundColor(MODULE_COLORS[colorIndex]);

                    TableRow.LayoutParams params = new TableRow.LayoutParams(120, 150);
                    params.setMargins(2, 2, 2, 2);
                    moduleView.setLayoutParams(params);

                    // click listener for module details
                    moduleView.setOnClickListener(v -> handleModuleClick(schedule));

                    row.addView(moduleView);
                }
            }

            timetableGrid.addView(row);
        }
    }

    private boolean isTimeInRange(String timeToCheck, String startTime, String endTime) {
        // Assumes all times are in 24-hour format
        return timeToCheck.compareTo(startTime) >= 0 && timeToCheck.compareTo(endTime) < 0;
    }

    private void handleModuleClick(ModuleSchedule schedule) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        // Inflates layout for the bottom sheet
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_module_code, null);

        // Find views in the bottom sheet layout
        TextView moduleCode = bottomSheetView.findViewById(R.id.module_code);
        TextView moduleName = bottomSheetView.findViewById(R.id.module_name);
        TextView moduleLecturer = bottomSheetView.findViewById(R.id.module_lecturer);
        TextView moduleLocation = bottomSheetView.findViewById(R.id.module_location);
        TextView moduleDay = bottomSheetView.findViewById(R.id.module_day);
        TextView moduleStartTime = bottomSheetView.findViewById(R.id.module_start_time);
        TextView moduleEndTime = bottomSheetView.findViewById(R.id.module_end_time);
        androidx.appcompat.widget.SwitchCompat notificationToggle = bottomSheetView.findViewById(R.id.notification_toggle);

        // Canceled hideShow button from inside the bottom sheet (when pressing on modules)
        // View hideShowButton = bottomSheetView.findViewById(R.id.hide_show_button);
        // hideShowButton.setVisibility(View.GONE);

        // Setting details in the bottom sheet
        Module module = schedule.getModule();
        TimeSlot timeSlot = schedule.getTimeSlot();
        moduleCode.setText(module.getCode());
        moduleName.setText(module.getName());
        moduleLecturer.setText(module.getLecturer());
        moduleLocation.setText(timeSlot.getLocation());
        moduleDay.setText(timeSlot.getDay());
        moduleStartTime.setText(timeSlot.getStartTime());
        moduleEndTime.setText(timeSlot.getEndTime());

        notificationToggle.setChecked(schedule.isNotificationsEnabled());
        // listener for the notification toggle
        notificationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            schedule.setNotificationsEnabled(isChecked);

            Context context = requireContext();

            if (isChecked) {
                // Schedule notification
                TimetableNotificationManager.scheduleNotification(context, schedule);
                Toast.makeText(context,
                        "Notifications enabled for " + module.getCode(),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Cancel notification
                TimetableNotificationManager.cancelNotification(context, schedule);
                Toast.makeText(context,
                        "Notifications disabled for " + module.getCode(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        // Setting the content view
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void loadTimetableData() {
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString == null) {
                Log.e("TimetableFragment", "Failed to load timetable JSON");
                Toast.makeText(requireContext(), "Failed to load timetable data", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray modulesArray = jsonObject.getJSONArray("modules");
            moduleSchedules.clear();
            for (int i = 0; i < modulesArray.length(); i++) {
                JSONObject moduleObj = modulesArray.getJSONObject(i);
                Module module = new Module(
                        moduleObj.getString("code"),
                        moduleObj.getString("name"),
                        moduleObj.getString("lecturer")
                );

                JSONArray slotsArray = moduleObj.getJSONArray("slots");
                for (int j = 0; j < slotsArray.length(); j++) {
                    JSONObject slotObj = slotsArray.getJSONObject(j);
                    TimeSlot timeSlot = new TimeSlot(
                            slotObj.getString("day"),
                            slotObj.getString("startTime"),
                            slotObj.getString("endTime"),
                            slotObj.getString("location")
                    );

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