package com.example.appdevelopmentprojectfinal.timetable;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;
import com.example.appdevelopmentprojectfinal.utils.JsonUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class TimetableFragment extends Fragment implements ModuleManagementAdapter.OnModuleVisibilityChangedListener {
    private static final String TIMETABLE_FILENAME = "timetable.json";

    private final List<ModuleSchedule> moduleSchedules = new ArrayList<>();
    private TableLayout timetableGrid;
    private TextView emptyView;
    private RecyclerView moduleListView;
    private ModuleManagementAdapter moduleAdapter;

    private Button addModuleButton;
    LinearLayout linearLayoutSlots;

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                // Allow RecyclerView to scroll within NestedScrollView
                return true;
            }
        };

        moduleListView.setLayoutManager(layoutManager);
        moduleListView.setHasFixedSize(true);
        moduleListView.setNestedScrollingEnabled(true);

        addModuleButton = view.findViewById(R.id.add_module_button);
        addModuleButton.setOnClickListener(v -> {
            // Inflate custom dialog layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_module, null);

            linearLayoutSlots = dialogView.findViewById(R.id.linear_layout_slots);

            EditText codeInput = dialogView.findViewById(R.id.input_code);
            EditText nameInput = dialogView.findViewById(R.id.input_name);
            EditText lecturerInput = dialogView.findViewById(R.id.input_lecturer);
            EditText typeInput = dialogView.findViewById(R.id.input_type);
            Button confirmButton = dialogView.findViewById(R.id.btn_add_module);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .create();

            dialog.show();

            Button btnAddAnotherSlot = dialogView.findViewById(R.id.btn_add_slot);
            btnAddAnotherSlot.setOnClickListener(innerView -> {
                // Create new slot section dynamically

                // Create new LinearLayout for each slot
                LinearLayout newSlotLayout = new LinearLayout(requireContext());
                newSlotLayout.setOrientation(LinearLayout.VERTICAL);
                newSlotLayout.setTag("mainSlotLayout");

                // Create a parent container for each complete slot
                LinearLayout slotContainer = new LinearLayout(requireContext());
                slotContainer.setOrientation(LinearLayout.VERTICAL);
                slotContainer.setTag("slotContainer"); // Add tag to identify slot containers

                // Create and add EditText for day
                EditText inputDay = new EditText(requireContext());
                inputDay.setHint("Day (e.g., Monday)");
                newSlotLayout.addView(inputDay);

                // Create and add EditText for start time
                EditText inputStartTime = new EditText(requireContext());
                inputStartTime.setHint("Start Time (e.g., 09:00 AM)");
                newSlotLayout.addView(inputStartTime);

                // Create and add EditText for end time
                EditText inputEndTime = new EditText(requireContext());
                inputEndTime.setHint("End Time (e.g., 10:00 AM)");
                newSlotLayout.addView(inputEndTime);

                // Create and add EditText for location
                EditText inputLocation = new EditText(requireContext());
                inputLocation.setHint("Location");
                newSlotLayout.addView(inputLocation);

                // Create and add RadioGroup for isMovable
                TextView textIsMovable = new TextView(requireContext());
                textIsMovable.setText("Is the Slot Movable?");
                newSlotLayout.addView(textIsMovable);

                RadioGroup radioGroupIsMovable = new RadioGroup(requireContext());
                radioGroupIsMovable.setOrientation(LinearLayout.HORIZONTAL);

                RadioButton radioYes = new RadioButton(requireContext());
                radioYes.setText("Yes");
                radioGroupIsMovable.addView(radioYes);

                RadioButton radioNo = new RadioButton(requireContext());
                radioNo.setText("No");
                radioGroupIsMovable.addView(radioNo);

                newSlotLayout.addView(radioGroupIsMovable);

                // Container for alternative slots (initially empty)
                LinearLayout alternativeSlotsContainer = new LinearLayout(requireContext());
                alternativeSlotsContainer.setOrientation(LinearLayout.VERTICAL);
                alternativeSlotsContainer.setTag("alternativeSlotsContainer");
                newSlotLayout.addView(alternativeSlotsContainer);

                // Button to add alternative slot (initially hidden)
                Button btnAddAlternativeSlot = new Button(requireContext());
                btnAddAlternativeSlot.setText("Add Alternative Time Slot");
                btnAddAlternativeSlot.setVisibility(View.GONE);
                newSlotLayout.addView(btnAddAlternativeSlot);

                // RadioGroup listener to show/hide alternative slots option
                radioGroupIsMovable.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == radioYes.getId()) {
                        btnAddAlternativeSlot.setVisibility(View.VISIBLE);
                    } else {
                        btnAddAlternativeSlot.setVisibility(View.GONE);
                        // Remove all alternative slots when switching to "No"
                        alternativeSlotsContainer.removeAllViews();
                    }
                });

                // Button to add alternative slot
                btnAddAlternativeSlot.setOnClickListener(view_alternate -> {
                    // Create alternative slot layout (similar to main slot but without movable option)
                    LinearLayout altSlotLayout = new LinearLayout(requireContext());
                    altSlotLayout.setOrientation(LinearLayout.VERTICAL);
                    altSlotLayout.setTag("alternativeSlot");

                    // Add divider
                    View divider = new View(requireContext());
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    ));
                    divider.setBackgroundColor(Color.GRAY);
                    altSlotLayout.addView(divider);

                    // Add title
                    TextView altTitle = new TextView(requireContext());
                    altTitle.setText("Alternative Time Slot");
                    altSlotLayout.addView(altTitle);

                    // Add day input
                    EditText altInputDay = new EditText(requireContext());
                    altInputDay.setHint("Day (e.g., Tuesday)");
                    altSlotLayout.addView(altInputDay);

                    // Add start time input
                    EditText altInputStartTime = new EditText(requireContext());
                    altInputStartTime.setHint("Start Time (e.g., 02:00 PM)");
                    altSlotLayout.addView(altInputStartTime);

                    // Add end time input
                    EditText altInputEndTime = new EditText(requireContext());
                    altInputEndTime.setHint("End Time (e.g., 03:00 PM)");
                    altSlotLayout.addView(altInputEndTime);

                    // Add location input
                    EditText altInputLocation = new EditText(requireContext());
                    altInputLocation.setHint("Location");
                    altSlotLayout.addView(altInputLocation);

                    // Add remove button for this alternative slot
                    Button btnRemoveAltSlot = new Button(requireContext());
                    btnRemoveAltSlot.setText("Remove This Alternative Slot");
                    btnRemoveAltSlot.setOnClickListener(removeView -> {
                        alternativeSlotsContainer.removeView(altSlotLayout);
                    });
                    altSlotLayout.addView(btnRemoveAltSlot);

                    // Add the alternative slot to the container
                    alternativeSlotsContainer.addView(altSlotLayout);
                });

                slotContainer.addView(newSlotLayout);
                // Add the new slot layout to the parent LinearLayout
                linearLayoutSlots.addView(slotContainer);
            });

            confirmButton.setOnClickListener(confirmView -> {
                String code = codeInput.getText().toString();
                String name = nameInput.getText().toString();
                String lecturer = lecturerInput.getText().toString();
                String type = typeInput.getText().toString();

                List<TimeSlot> timeSlotList = new ArrayList<>();
                List<TimeSlot> alternativeSlotsList = new ArrayList<>();

                int childCount = linearLayoutSlots.getChildCount();

                // Iterate through each child view within the LinearLayout
                for (int i = 0; i < childCount; i++) {
                    View childView = linearLayoutSlots.getChildAt(i);

                    // Only process views that are our slot containers
                    if (childView instanceof LinearLayout && "slotContainer".equals(childView.getTag())) {
                        LinearLayout slotContainer = (LinearLayout) childView;

                        // Find the main slot layout within the container
                        LinearLayout slotLayout = slotContainer.findViewWithTag("mainSlotLayout");
                        if (slotLayout != null) {
                            // Read the main time slot
                            TimeSlot timeSlot = readSlotLayout(slotLayout);
                            timeSlotList.add(timeSlot);

                            // Find the alternative slots container in this layout
                            LinearLayout alternativeSlotsContainer = slotLayout.findViewWithTag("alternativeSlotsContainer");
                            if (alternativeSlotsContainer != null) {
                                // Process all alternative slots
                                int altChildCount = alternativeSlotsContainer.getChildCount();
                                for (int j = 0; j < altChildCount; j++) {
                                    View altChildView = alternativeSlotsContainer.getChildAt(j);
                                    if (altChildView instanceof LinearLayout && "alternativeSlot".equals(altChildView.getTag())) {
                                        TimeSlot altTimeSlot = readSlotLayout((LinearLayout) altChildView);
                                        alternativeSlotsList.add(altTimeSlot);
                                    }
                                }
                            }
                        }
                    }
                }

                // Create and save the module
                Module module = new Module(code, name, lecturer, true);
                module.setType(type);
                module.setTimeSlotList(timeSlotList);
                module.setAlternativeSlots(alternativeSlotsList);

                try {
                    addModule(module);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                dialog.dismiss();
            });
        });



        // Load timetable data
        loadTimetableData();

        // Setup module adapter
        moduleAdapter = new ModuleManagementAdapter(moduleSchedules, this);
        moduleListView.setAdapter(moduleAdapter);

        // Display the timetable
        displayTimetable();
    }

    private TimeSlot readSlotLayout(LinearLayout slotLayout) {
        String day = "";
        String startTime = "";
        String endTime = "";
        String location = "";
        boolean isMovable = false;

        // Iterate through the children of the slot layout (EditText, RadioGroup)
        for (int j = 0; j < slotLayout.getChildCount(); j++) {
            View inputView = slotLayout.getChildAt(j);

            if (inputView instanceof EditText) {
                EditText editText = (EditText) inputView;
                String hint = editText.getHint() != null ? editText.getHint().toString() : "";
                String text = editText.getText().toString();

                if (hint.contains("Day")) {
                    day = text;
                } else if (hint.contains("Start Time")) {
                    startTime = text;
                } else if (hint.contains("End Time")) {
                    endTime = text;
                } else if (hint.contains("Location")) {
                    location = text;
                }
            } else if (inputView instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) inputView;
                int checkedId = radioGroup.getCheckedRadioButtonId();

                if (checkedId != -1) { // Check if a button is selected
                    RadioButton selectedRadioButton = radioGroup.findViewById(checkedId);
                    if (selectedRadioButton != null) {
                        isMovable = selectedRadioButton.getText().toString().equalsIgnoreCase("Yes");
                    }
                }
            }
        }

        // Process the extracted slot data (e.g., print to log)
        Log.d("TimetableFragment", "Slot: Day=" + day + ", Start=" + startTime + ", End=" + endTime + ", Loc=" + location + ", Movable=" + isMovable);
        // Here you might add logic to store this information to ModuleSchedule Object
        return new TimeSlot(day, startTime, endTime, location);
    }

    private void addModule(Module module) throws JSONException {
        Log.d("ModuleEntry", module.toString());
        JsonUtil jsonUtil = new JsonUtil();
        jsonUtil.appendModuleToFile(requireContext(), new JSONObject(module.toString()));
    }

    @Override
    public void onModuleVisibilityChanged() {
        // Refresh the timetable when module visibility changes
        timetableGrid.removeAllViews();
        displayTimetable();
    }

    private void displayTimetable() {
        // Clear existing content
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
                        // Filter out hidden modules
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

                    // Inflate the module item layout
                    View moduleView = inflater.inflate(R.layout.item_timetable_module, null);

                    // Set module details
                    TextView codeText = moduleView.findViewById(R.id.module_code);
                    TextView nameText = moduleView.findViewById(R.id.module_name);
                    TextView locationText = moduleView.findViewById(R.id.module_location);

                    codeText.setText(module.getCode());
                    nameText.setText(module.getName());
                    locationText.setText(schedule.getTimeSlot().getLocation());

                    // Sets a background color based on the module code
                    CardView cardView = (CardView) moduleView;
                    int colorIndex = Math.abs(module.getCode().hashCode()) % MODULE_COLORS.length;
                    cardView.setCardBackgroundColor(MODULE_COLORS[colorIndex]);

                    // Sets layout parameters
                    TableRow.LayoutParams params = new TableRow.LayoutParams(120, 150);
                    params.setMargins(2, 2, 2, 2);
                    moduleView.setLayoutParams(params);

                    // Adds a click listener for showing module details
                    moduleView.setOnClickListener(v -> handleModuleClick(schedule));

                    row.addView(moduleView);
                }
            }

            timetableGrid.addView(row);
        }
    }

    private boolean isTimeInRange(String timeToCheck, String startTime, String endTime) {
        // Simple string comparison for HH:MM format
        // Assumes all times are in 24-hour format
        return timeToCheck.compareTo(startTime) >= 0 && timeToCheck.compareTo(endTime) < 0;
    }

    private void handleModuleClick(ModuleSchedule schedule) {
        // Create and show bottom sheet
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

        // Remove the hide/show button as we now handle this in the module management section
        View hideShowButton = bottomSheetView.findViewById(R.id.hide_show_button);
        hideShowButton.setVisibility(View.GONE);

        // Setting module details in the bottom sheet
        Module module = schedule.getModule();
        TimeSlot timeSlot = schedule.getTimeSlot();
        moduleCode.setText(module.getCode());
        moduleName.setText(module.getName());
        moduleLecturer.setText(module.getLecturer());
        moduleLocation.setText(timeSlot.getLocation());
        moduleDay.setText(timeSlot.getDay());
        moduleStartTime.setText(timeSlot.getStartTime());
        moduleEndTime.setText(timeSlot.getEndTime());

        // Setting the content view for the bottom sheet
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
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
                        moduleObj.getString("lecturer"),
                        Boolean.parseBoolean(moduleObj.getString("show"))
                );
                module.setType(moduleObj.getString("type"));

                // Process alternative slots
                JSONArray alternativeSlotsArray = moduleObj.getJSONArray("alternativeSlots");
                for (int j = 0; j < alternativeSlotsArray.length(); j++) {
                    JSONObject alternativeSlotsArrayJSONObject = alternativeSlotsArray.getJSONObject(j);

                    // Create time slot
                    TimeSlot alternativeTimeSlot = new TimeSlot(
                            alternativeSlotsArrayJSONObject.getString("day"),
                            alternativeSlotsArrayJSONObject.getString("startTime"),
                            alternativeSlotsArrayJSONObject.getString("endTime"),
                            alternativeSlotsArrayJSONObject.getString("location")
                    );

                    module.getAlternativeSlots().add(alternativeTimeSlot);
                    Log.d("ModuleEntry", module.toString());
                }

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

                    boolean isMovable = slotObj.getBoolean("isMovable");
                    module.getTimeSlotList().add(timeSlot);
                    Log.d("ModuleEntry", module.toString());

                    moduleSchedules.add(new ModuleSchedule(module, timeSlot, isMovable, module.isShow()));
                }
            }

            Log.d("TimetableFragment", "Loaded " + moduleSchedules.size() + " module schedules");

        } catch (JSONException e) {
            Log.e("TimetableFragment", "JSON parsing error: " + e.getMessage());
            Toast.makeText(requireContext(), "Error parsing timetable data", Toast.LENGTH_SHORT).show();

            Log.i("TimetableFragment", "Trying to copy file to internal storage...");
            JsonUtil jsonUtil = new JsonUtil();
            jsonUtil.copyFileFromAssetsToInternalStorage(requireContext(), TIMETABLE_FILENAME);
            loadTimetableData();
        }
    }

    private String loadJSONFromAsset() {
            JsonUtil jsonUtil = new JsonUtil();
            Context tempContext = requireContext();
            return jsonUtil.readFileFromInternalStorage(tempContext);
    }
}