package com.example.appdevelopmentprojectfinal.model;

import androidx.annotation.NonNull;

public class Module {
    private String code;
    private String name;
    private String lecturer;
    private int year;
    private String tutor;
    private String domain;
    private String department;

    public Module() {
        // Empty constructor required for JSON deserialization
    }

    public Module(String code, String name, String lecturer) {
        this.code = code;
        this.name = name;
        this.lecturer = lecturer;
    }

    // Getters
    public String getCode() { return code; }
    public String getName() { return name; }
    String getLecturer() { return lecturer; }
    
    public int getYear() {
        return year;
    }

    public String getTutor() {
        return tutor;
    }

    public String getDomain() {
        return domain;
    }

    public String getDepartment() {
        return department;
    }

    // Setters
    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    
    @NonNull
    @Override
    public String toString() {
        return code + ": " + name + " (" + lecturer + ")";
    }
}