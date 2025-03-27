package com.example.appdevelopmentprojectfinal;

import androidx.annotation.NonNull;

public class TimeSlot {
    private final String day;
    private final String startTime;
    private final String endTime;
    private final String location;

    public TimeSlot(String day, String startTime, String endTime, String location) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    // Getters
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    
    @NonNull
    @Override
    public String toString() {
        return day + " " + startTime + "-" + endTime + " at " + location;
    }
}