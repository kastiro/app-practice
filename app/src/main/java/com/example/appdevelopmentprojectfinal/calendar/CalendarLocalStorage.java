package com.example.appdevelopmentprojectfinal.calendar;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for storing and loading calendar events locally
 * This class provides local storage functionality independent of Firebase
 */
public class CalendarLocalStorage {
    private static final String TAG = "CalendarLocalStorage";
    private static final String FILENAME = "calendar_events.json";
    
    /**
     * Save event to local storage
     * @param context Context
     * @param event Event to save
     * @return Whether save was successful
     */
    public static boolean saveEvent(Context context, Event event) {
        try {
            JSONObject jsonData = readEventsFile(context);
            if (jsonData == null) {
                jsonData = new JSONObject();
                jsonData.put("events", new JSONArray());
            }
            
            JSONArray eventsArray = jsonData.getJSONArray("events");
            
            // Check if event with same ID already exists
            boolean found = false;
            String eventId = event.getId();
            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject existingEvent = eventsArray.getJSONObject(i);
                if (existingEvent.has("id") && existingEvent.getString("id").equals(eventId)) {
                    // Replace existing event
                    eventsArray.put(i, convertEventToJson(event));
                    found = true;
                    break;
                }
            }
            
            // Add new event if it doesn't exist
            if (!found) {
                eventsArray.put(convertEventToJson(event));
            }
            
            // Write back to file
            return writeEventsFile(context, jsonData);
            
        } catch (JSONException e) {
            return false;
        }
    }
    
    /**
     * Delete event from local storage
     * @param context Context
     * @param eventId ID of event to delete
     * @return Whether deletion was successful
     */
    public static boolean deleteEvent(Context context, String eventId) {
        try {
            JSONObject jsonData = readEventsFile(context);
            if (jsonData == null || !jsonData.has("events")) {
                return false;
            }
            
            JSONArray eventsArray = jsonData.getJSONArray("events");
            JSONArray newEventsArray = new JSONArray();
            
            boolean found = false;
            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject existingEvent = eventsArray.getJSONObject(i);
                if (existingEvent.has("id") && existingEvent.getString("id").equals(eventId)) {
                    found = true;
                } else {
                    newEventsArray.put(existingEvent);
                }
            }
            
            if (!found) {
                return false;
            }
            
            jsonData.put("events", newEventsArray);
            return writeEventsFile(context, jsonData);
            
        } catch (JSONException e) {
            return false;
        }
    }
    
    /**
     * Load all events from local storage
     * @param context Context
     * @return List of events
     */
    public static List<Event> loadEvents(Context context) {
        List<Event> events = new ArrayList<>();
        
        try {
            JSONObject jsonData = readEventsFile(context);
            if (jsonData == null || !jsonData.has("events")) {
                return events;
            }
            
            JSONArray eventsArray = jsonData.getJSONArray("events");
            
            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject eventJson = eventsArray.getJSONObject(i);
                Event event = convertJsonToEvent(eventJson);
                if (event != null) {
                    events.add(event);
                }
            }
            
        } catch (JSONException e) {
            // Return empty list on error
        }
        
        return events;
    }
    
    /**
     * Convert Event object to JSON object
     */
    private static JSONObject convertEventToJson(Event event) throws JSONException {
        JSONObject eventJson = new JSONObject();
        eventJson.put("id", event.getId());
        eventJson.put("title", event.getTitle());
        eventJson.put("description", event.getDescription());
        eventJson.put("date", event.getDate().getTime());
        eventJson.put("type", event.getType());
        eventJson.put("isCompleted", event.isCompleted());
        eventJson.put("userId", event.getUserId() != null ? event.getUserId() : "local_user");
        
        return eventJson;
    }
    
    /**
     * Convert JSON object to Event object
     */
    private static Event convertJsonToEvent(JSONObject json) {
        try {
            Event event = new Event();
            event.setId(json.getString("id"));
            event.setTitle(json.getString("title"));
            event.setDescription(json.getString("description"));
            event.setDate(new Date(json.getLong("date")));
            event.setType(json.getInt("type"));
            event.setCompleted(json.getBoolean("isCompleted"));
            event.setUserId(json.getString("userId"));
            
            return event;
        } catch (JSONException e) {
            return null;
        }
    }
    
    /**
     * Read events file
     */
    private static JSONObject readEventsFile(Context context) {
        File file = new File(context.getFilesDir(), FILENAME);
        if (!file.exists()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            if (sb.length() == 0) {
                return null;
            }
            
            return new JSONObject(sb.toString());
            
        } catch (IOException | JSONException e) {
            return null;
        }
    }
    
    /**
     * Write events file
     */
    private static boolean writeEventsFile(Context context, JSONObject jsonData) {
        File file = new File(context.getFilesDir(), FILENAME);
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(jsonData.toString(4).getBytes());
            return true;
        } catch (IOException | JSONException e) {
            return false;
        }
    }
} 