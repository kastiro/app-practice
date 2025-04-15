package com.example.appdevelopmentprojectfinal.timetable;

public class ModuleSchedule {
    private Module module;
    private TimeSlot timeSlot;
    private boolean isMovable;
    private boolean isVisible;
    private boolean notificationsEnabled;
    public ModuleSchedule(Module module, TimeSlot timeSlot, boolean isMovable) {
        this.module = module;
        this.timeSlot = timeSlot;
        this.isMovable = isMovable;
        this.isVisible = true;
        this.notificationsEnabled = false;
    }

    // Getters
    public Module getModule() { return module; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public boolean isMovable() { return isMovable; }
    public boolean isVisible() { return isVisible; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setVisible( boolean visible) { isVisible = visible; }
    public void setNotificationsEnabled(boolean enabled) { notificationsEnabled = enabled; }

    // TODO: part of moving functionality
    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }
}