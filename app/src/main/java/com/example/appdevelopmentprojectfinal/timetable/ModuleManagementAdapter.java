package com.example.appdevelopmentprojectfinal.timetable;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManagementAdapter extends RecyclerView.Adapter<ModuleManagementAdapter.ModuleViewHolder> {

    // For grouping all schedules of the same module
    private final List<ModuleGroup> moduleGroups = new ArrayList<>();
    private final OnModuleVisibilityChangedListener listener;

    // Colors for different modules
    private static final int[] MODULE_COLORS = {
            Color.parseColor("#FFCDD2"), // Light Red
            Color.parseColor("#C8E6C9"), // Light Green
            Color.parseColor("#BBDEFB"), // Light Blue
            Color.parseColor("#FFE0B2"), // Light Orange
            Color.parseColor("#E1BEE7")  // Light Purple
    };

    // Interface for visibility change callbacks
    public interface OnModuleVisibilityChangedListener {
        void onModuleVisibilityChanged();
    }

    // Class to group schedules by module
    public static class ModuleGroup {
        private final Module module;
        private final List<ModuleSchedule> schedules = new ArrayList<>();
        private boolean allVisible = true;

        public ModuleGroup(Module module) {
            this.module = module;
        }

        public void addSchedule(ModuleSchedule schedule) {
            schedules.add(schedule);
            if (!schedule.isVisible()) {
                allVisible = false;
            }
        }

        public Module getModule() {
            return module;
        }

        public List<ModuleSchedule> getSchedules() {
            return schedules;
        }

        public boolean isAllVisible() {
            return allVisible;
        }

        public void setAllVisible(boolean visible) {
            allVisible = visible;
            for (ModuleSchedule schedule : schedules) {
                schedule.setVisible(visible);
            }
        }

        // Prepare a string with all schedules of this module
        public String getSchedulesString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < schedules.size(); i++) {
                ModuleSchedule schedule = schedules.get(i);
                TimeSlot slot = schedule.getTimeSlot();
                builder.append(slot.getDay())
                        .append(" ")
                        .append(slot.getStartTime())
                        .append("-")
                        .append(slot.getEndTime())
                        .append(" (")
                        .append(slot.getLocation())
                        .append(")");

                if (i < schedules.size() - 1) {
                    builder.append("\n");
                }
            }
            return builder.toString();
        }
    }

    public ModuleManagementAdapter(List<ModuleSchedule> moduleSchedules, OnModuleVisibilityChangedListener listener) {
        this.listener = listener;

        // Group schedules by module
        Map<String, ModuleGroup> moduleMap = new HashMap<>();

        for (ModuleSchedule schedule : moduleSchedules) {
            Module module = schedule.getModule();
            String moduleCode = module.getCode();

            if (!moduleMap.containsKey(moduleCode)) {
                moduleMap.put(moduleCode, new ModuleGroup(module));
            }

            moduleMap.get(moduleCode).addSchedule(schedule);
        }

        // Add all module groups to the list
        moduleGroups.addAll(moduleMap.values());
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_management, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        ModuleGroup moduleGroup = moduleGroups.get(position);
        Module module = moduleGroup.getModule();

        // Set module details
        holder.moduleTitle.setText(module.getCode() + ": " + module.getName());
        holder.moduleLecturer.setText(module.getLecturer());
        holder.moduleSchedule.setText(moduleGroup.getSchedulesString());

        // Set card color
        int colorIndex = Math.abs(module.getCode().hashCode()) % MODULE_COLORS.length;
        holder.cardView.setCardBackgroundColor(MODULE_COLORS[colorIndex]);

        // Set switch state
        holder.visibilityToggle.setChecked(moduleGroup.isAllVisible());

        // Set up toggle listener
        holder.visibilityToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            moduleGroup.setAllVisible(isChecked);
            listener.onModuleVisibilityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return moduleGroups.size();
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        TextView moduleTitle;
        TextView moduleLecturer;
        TextView moduleSchedule;
        Switch visibilityToggle;
        CardView cardView;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleTitle = itemView.findViewById(R.id.module_title);
            moduleLecturer = itemView.findViewById(R.id.module_lecturer);
            moduleSchedule = itemView.findViewById(R.id.module_schedule);
            visibilityToggle = itemView.findViewById(R.id.visibility_toggle);
            cardView = (CardView) itemView;
        }
    }
}