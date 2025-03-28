package com.example.appdevelopmentprojectfinal.calendar;

import java.util.Date;

/**
 * Represents a calendar event with a title, description, date, and type.
 */
public class Event {
    
    public static final int TYPE_EVENT = 0;
    public static final int TYPE_TODO = 1;
    
    private String title;
    private String description;
    private Date date;
    private int type; // 0 for event, 1 for todo
    private boolean isCompleted; // for todo items only
    
    public Event(String title, String description, Date date, int type) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.isCompleted = false;
    }
    
    // Getters and setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    public boolean isEvent() {
        return type == TYPE_EVENT;
    }
    
    public boolean isTodo() {
        return type == TYPE_TODO;
    }
} 