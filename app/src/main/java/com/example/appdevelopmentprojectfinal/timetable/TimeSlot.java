package com.example.appdevelopmentprojectfinal.timetable;

public class TimeSlot {
    private String day;
    private String startTime;
    private String endTime;
    private String location;

    public TimeSlot() {
    }

    public TimeSlot(String day, String startTime, String endTime, String location) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }

    @Override
    public String toString() {
        return "{" +
                "\"day\":\"" + day + "\"," +
                "\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"location\":\"" + location + "\"," +
                "\"isMovable\":" + true +
                "}";
    }
}