package com.example.appdevelopmentprojectfinal.utils;

import android.content.Context;
import android.util.Log;

import com.example.appdevelopmentprojectfinal.model.Course;
import com.example.appdevelopmentprojectfinal.model.Module;
import com.example.appdevelopmentprojectfinal.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Central data manager to avoid duplicate loading and keep data consistent across fragments
public class DataManager {
    private static final String TAG = "TimetableApp:DataManager";
    
    private static DataManager instance;
    
    // Data storage
    private User currentUser;
    private List<Module> modules;
    private List<Course> courses;
    private Map<String, Module> moduleMap;
    
    // Private constructor for singleton
    private DataManager() {
        // Private constructor to enforce singleton pattern
        modules = new ArrayList<>();
        courses = new ArrayList<>();
        moduleMap = new HashMap<>();
    }
    
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    public void initialize(Context context) {
        loadUser(context);
        loadModules(context);
        loadCourses(context);
        
        // Create module lookup map for efficiency
        moduleMap.clear();
        for (Module module : modules) {
            moduleMap.put(module.getCode(), module);
        }
    }
    
    private void loadUser(Context context) {
        try {
            String userJson = loadJSONFromAsset(context, "data/user.json");
            if (userJson != null) {
                Gson gson = new Gson();
                currentUser = gson.fromJson(userJson, User.class);
                Log.d(TAG, "User loaded successfully: " + currentUser.getEmail() + ", wallet balance: " + currentUser.getWallet());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage());
        }
    }
    
    private void loadModules(Context context) {
        try {
            String modulesJson = loadJSONFromAsset(context, "data/modules.json");
            if (modulesJson != null) {
                JSONObject jsonObject = new JSONObject(modulesJson);
                JSONArray modulesArray = jsonObject.getJSONArray("modules");
                
                Gson gson = new Gson();
                modules.clear();
                
                for (int i = 0; i < modulesArray.length(); i++) {
                    JSONObject moduleObject = modulesArray.getJSONObject(i);
                    Module module = gson.fromJson(moduleObject.toString(), Module.class);
                    modules.add(module);
                }
                
                Log.d(TAG, "Loaded " + modules.size() + " modules");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing modules JSON: " + e.getMessage());
        }
    }
    
    private void loadCourses(Context context) {
        try {
            String coursesJson = loadJSONFromAsset(context, "data/courses.json");
            if (coursesJson != null) {
                JSONObject jsonObject = new JSONObject(coursesJson);
                JSONArray coursesArray = jsonObject.getJSONArray("courses");
                
                Gson gson = new Gson();
                courses.clear();
                
                for (int i = 0; i < coursesArray.length(); i++) {
                    JSONObject courseObject = coursesArray.getJSONObject(i);
                    Course course = gson.fromJson(courseObject.toString(), Course.class);
                    courses.add(course);
                }
                
                Log.d(TAG, "Loaded " + courses.size() + " courses");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing courses JSON: " + e.getMessage());
        }
    }
    
    private String loadJSONFromAsset(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            int bytesRead = inputStream.read(buffer);
            inputStream.close();
            
            if (bytesRead != size) {
                Log.e(TAG, "Failed to read entire file: " + fileName);
            }
            
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "Error loading JSON file: " + fileName + " - " + e.getMessage());
            return null;
        }
    }
    
    // User-related methods
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean purchaseCourse(String courseId) {
        if (currentUser == null) {
            Log.e(TAG, "Cannot purchase course: user not found");
            return false;
        }
        
        Course course = getCourseById(courseId);
        if (course == null) {
            Log.e(TAG, "Cannot purchase course: course not found with ID " + courseId);
            return false;
        }
        
        if (currentUser.ownsModule(courseId)) {
            Log.w(TAG, "User already owns course: " + courseId);
            return false;
        }
        
        if (!currentUser.hasEnoughFunds(course.getPrice())) {
            Log.w(TAG, "Insufficient funds for course purchase. Required: " + course.getPrice() 
                + ", Available: " + currentUser.getWallet());
            return false;
        }
        
        // All checks passed, proceed with purchase
        currentUser.purchaseCourse(courseId, course.getPrice());
        Log.i(TAG, "Purchase successful: " + course.getName() + " (" + courseId 
            + ") for " + course.getPrice() + ". New balance: " + currentUser.getWallet());
        return true;
    }
    
    // Module-related methods
    public List<Module> getAllModules() {
        return modules;
    }
    
    public Module getModuleByCode(String code) {
        return moduleMap.get(code);
    }
    
    public List<Module> getUserModules() {
        if (currentUser == null || currentUser.getModules() == null) {
            return new ArrayList<>();
        }
        
        List<Module> userModules = new ArrayList<>();
        for (String moduleCode : currentUser.getModules()) {
            Module module = moduleMap.get(moduleCode);
            if (module != null) {
                userModules.add(module);
            }
        }
        
        return userModules;
    }
    
    // Course-related methods
    public List<Course> getAllCourses() {
        return courses;
    }
    
    public Course getCourseById(String id) {
        for (Course course : courses) {
            if (course.getId().equals(id)) {
                return course;
            }
        }
        return null;
    }
    
    public List<Course> getCoursesByModule(String moduleCode) {
        return courses.stream()
                .filter(course -> course.getRelatedModule().equals(moduleCode))
                .collect(Collectors.toList());
    }
    
    public List<Course> getOwnedCourses() {
        if (currentUser == null || currentUser.getOwnedCourses() == null) {
            return new ArrayList<>();
        }
        
        return courses.stream()
                .filter(course -> currentUser.getOwnedCourses().contains(course.getId()))
                .collect(Collectors.toList());
    }
    
    public List<Course> getRecommendedCourses() {
        if (currentUser == null || currentUser.getModules() == null) {
            return new ArrayList<>();
        }
        
        // Get courses related to user's modules
        List<Course> recommended = new ArrayList<>();
        for (String moduleCode : currentUser.getModules()) {
            List<Course> moduleCourses = getCoursesByModule(moduleCode);
            for (Course course : moduleCourses) {
                if (!currentUser.getOwnedCourses().contains(course.getId()) && !recommended.contains(course)) {
                    recommended.add(course);
                }
            }
        }
        
        return recommended;
    }
    
    public List<Course> getTrendingCourses() {
        List<Course> trending = new ArrayList<>(courses);
        
        // Sort by purchases today
        Collections.sort(trending, (c1, c2) -> {
            if (c1.getStatistics() == null || c2.getStatistics() == null) {
                return 0;
            }
            return Integer.compare(c2.getStatistics().getPurchasesToday(), 
                                   c1.getStatistics().getPurchasesToday());
        });
        
        return trending;
    }
    
    public List<Course> searchCourses(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>(courses);
        }
        
        String searchQuery = query.toLowerCase();
        return courses.stream()
                .filter(course -> 
                        course.getName().toLowerCase().contains(searchQuery) ||
                        course.getDescription().toLowerCase().contains(searchQuery) ||
                        course.getAuthor().toLowerCase().contains(searchQuery) ||
                        course.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchQuery)))
                .collect(Collectors.toList());
    }
    
    public List<Course> getRelatedCourses(Course course) {
        if (course == null) {
            return new ArrayList<>();
        }
        
        // First get courses with the same module
        List<Course> related = getCoursesByModule(course.getRelatedModule());
        related.remove(course);  // Remove the current course
        
        // If we need more, add courses with similar tags
        if (related.size() < 3 && course.getTags() != null) {
            for (Course c : courses) {
                if (!related.contains(c) && c != course && hasCommonTags(c, course)) {
                    related.add(c);
                }
                
                if (related.size() >= 5) {
                    break;
                }
            }
        }
        
        return related;
    }
    
    private boolean hasCommonTags(Course c1, Course c2) {
        if (c1.getTags() == null || c2.getTags() == null) {
            return false;
        }
        
        for (String tag : c1.getTags()) {
            if (c2.getTags().contains(tag)) {
                return true;
            }
        }
        
        return false;
    }
}