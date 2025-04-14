package com.example.appdevelopmentprojectfinal.timetable;

import androidx.annotation.NonNull;

public class Module {
    private final String code;
    private final String name;
    private final String lecturer;

    public Module(String code, String name, String lecturer) {
        this.code = code;
        this.name = name;
        this.lecturer = lecturer;
    }

    // Getters
    public String getCode() { return code; }
    public String getName() { return name; }
    
    // This is used in toString() method
    String getLecturer() { return lecturer; }
    
    @NonNull
    @Override
    public String toString() {
        return code + ": " + name + " (" + lecturer + ")";
    }
}
