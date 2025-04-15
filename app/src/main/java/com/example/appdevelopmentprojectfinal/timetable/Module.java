package com.example.appdevelopmentprojectfinal.timetable;

import androidx.annotation.NonNull;

public class Module {
    private final String code;
    private final String name;
    private final String lecturer;

    private final boolean show;

    public Module(String code, String name, String lecturer, boolean show) {
        this.code = code;
        this.name = name;
        this.lecturer = lecturer;
        this.show = show;
    }

    // Getters
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isShow() {
        return show;
    }
    // This is used in toString() method
    String getLecturer() { return lecturer; }
    
    @NonNull
    @Override
    public String toString() {
        return code + ": " + name + " (" + lecturer + "), " + show;
    }
}
