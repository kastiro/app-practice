package com.example.appdevelopmentprojectfinal.calendar;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.appdevelopmentprojectfinal.utils.DataManager;

import java.util.List;

/**
 * Calendar functionality helper class
 * Provides methods to use both Firebase and local storage
 */
public class CalendarHelper {
    private static final String TAG = "CalendarHelper";
    
    /**
     * Save event (to both Firebase and local storage)
     * @param context Context
     * @param event Event to save
     * @param onComplete Completion callback
     */
    public static void saveEvent(Context context, Event event, OnEventOperationListener onComplete) {
        // First save to local storage
        boolean localSaved = CalendarLocalStorage.saveEvent(context, event);
        
        // Then try to save to Firebase
        DataManager.getInstance().saveEvent(event, new DataManager.OnEventSavedListener() {
            @Override
            public void onEventSaved(Event savedEvent) {
                if (onComplete != null) {
                    onComplete.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if (onComplete != null) {
                    if (localSaved) {
                        onComplete.onSuccess();
                    } else {
                        onComplete.onError("Failed to save both locally and remotely");
                    }
                }
            }
        });
    }
    
    /**
     * Delete event (from both Firebase and local storage)
     * @param context Context
     * @param event Event to delete
     * @param onComplete Completion callback
     */
    public static void deleteEvent(Context context, Event event, OnEventOperationListener onComplete) {
        // First delete from local storage
        boolean localDeleted = CalendarLocalStorage.deleteEvent(context, event.getId());
        
        // Then try to delete from Firebase
        DataManager.getInstance().deleteEvent(event, new DataManager.OnEventDeletedListener() {
            @Override
            public void onEventDeleted() {
                if (onComplete != null) {
                    onComplete.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if (onComplete != null) {
                    if (localDeleted) {
                        onComplete.onSuccess();
                    } else {
                        onComplete.onError("Failed to delete both locally and remotely");
                    }
                }
            }
        });
    }
    
    /**
     * Load all events (merge from Firebase and local storage)
     * @param context Context
     * @return Merged list of events
     */
    public static List<Event> loadAllEvents(Context context) {
        // First load from local storage
        List<Event> localEvents = CalendarLocalStorage.loadEvents(context);
        
        // Add local events to DataManager
        DataManager.getInstance().addLocalEvents(localEvents);
        
        // Return all merged events
        return DataManager.getInstance().getAllEvents();
    }
    
    /**
     * Event operation completion listener
     */
    public interface OnEventOperationListener {
        void onSuccess();
        void onError(String errorMessage);
    }
} 