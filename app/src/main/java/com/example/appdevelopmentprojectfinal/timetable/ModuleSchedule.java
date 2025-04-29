package com.example.appdevelopmentprojectfinal.timetable;

public class ModuleSchedule {
    private Module module;
    private TimeSlot timeSlot;
    private boolean isMovable;
    private boolean isVisible;
    public ModuleSchedule(Module module, TimeSlot timeSlot, boolean isMovable, boolean isVisible) {
        this.module = module;
        this.timeSlot = timeSlot;
        this.isMovable = isMovable;
        this.isVisible = isVisible;
    }

    public Module getModule() { return module; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public boolean isMovable() { return isMovable; }
    public boolean isVisible() { return isVisible; }
    public void setVisible( boolean visible) { isVisible = visible; }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }
}