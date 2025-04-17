package com.example.appdevelopmentprojectfinal.timetable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class    Module {
    private final String code;
    private final String name;
    private final String lecturer;

    private final boolean show;

    private String type;

    private List<TimeSlot> timeSlotList;

    private List<TimeSlot> alternativeSlots;

    public Module(String code, String name, String lecturer, boolean show) {
        this.code = code;
        this.name = name;
        this.lecturer = lecturer;
        this.show = show;
        this.timeSlotList = new ArrayList<>();
        this.alternativeSlots = new ArrayList<>();
    }

    // Getters
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isShow() {
        return show;
    }

    public String getType() {
        return type;
    }

    public List<TimeSlot> getTimeSlotList() {
        return timeSlotList;
    }

    public void setTimeSlotList(List<TimeSlot> timeSlotList) {
        this.timeSlotList = timeSlotList;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TimeSlot> getAlternativeSlots() {
        return alternativeSlots;
    }

    public void setAlternativeSlots(List<TimeSlot> alternativeSlots) {
        this.alternativeSlots = alternativeSlots;
    }

    // This is used in toString() method
    String getLecturer() { return lecturer; }

    @Override
    public String toString() {
        StringBuilder slotsString = new StringBuilder("[");
        if (timeSlotList != null && !timeSlotList.isEmpty()) {
            for (int i = 0; i < timeSlotList.size(); i++) {
                slotsString.append(timeSlotList.get(i).toString());
                if (i < timeSlotList.size() - 1) {
                    slotsString.append(",");
                }
            }
        }
        slotsString.append("]");

        StringBuilder alternativeSlotsString = new StringBuilder("[");
        if (alternativeSlots != null && !alternativeSlots.isEmpty()) {
            for (int i = 0; i < alternativeSlots.size(); i++) {
                alternativeSlotsString.append(alternativeSlots.get(i).toString());
                if (i < alternativeSlots.size() - 1) {
                    alternativeSlotsString.append(",");
                }
            }
        }
        alternativeSlotsString.append("]");

        return "{" +
                "\"code\":\"" + code + "\"," +
                "\"name\":\"" + name + "\"," +
                "\"lecturer\":\"" + lecturer + "\"," +
                "\"type\":\"" + type + "\"," +
                "\"show\":" + show + "," +
                "\"slots\":" + slotsString.toString() + "," +
                "\"alternativeSlots\":" + alternativeSlotsString.toString() +
                "}";
    }
}
