package com.example.appdevelopmentprojectfinal.model;

import java.util.List;

public class User {
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phoneNumber;
    private int year;
    private String course;
    private String department;
    private List<String> modules;
    private String profilePhoto;
    private List<String> ownedCourses;
    private double wallet;

    public User() {
        // Empty constructor required for JSON deserialization
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public List<String> getOwnedCourses() {
        return ownedCourses;
    }

    public void setOwnedCourses(List<String> ownedCourses) {
        this.ownedCourses = ownedCourses;
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    // Helper methods
    public boolean ownsModule(String courseId) {
        return ownedCourses != null && ownedCourses.contains(courseId);
    }

    public boolean hasEnoughFunds(double amount) {
        return wallet >= amount;
    }

    public void purchaseCourse(String courseId, double price) {
        if (hasEnoughFunds(price) && !ownsModule(courseId)) {
            wallet -= price;
            ownedCourses.add(courseId);
        }
    }

    public String getFullName() {
        if (middleName != null && !middleName.isEmpty()) {
            return firstName + " " + middleName + " " + lastName;
        }
        return firstName + " " + lastName;
    }
}