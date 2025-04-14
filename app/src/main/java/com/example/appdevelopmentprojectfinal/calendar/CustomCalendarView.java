package com.example.appdevelopmentprojectfinal.calendar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.example.appdevelopmentprojectfinal.R;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom calendar view supporting blue circle for selected date and gray circle for today
 */
public class CustomCalendarView extends View {
    private Calendar currentCalendar = Calendar.getInstance();
    private Calendar selectedCalendar = Calendar.getInstance();
    private Calendar todayCalendar = Calendar.getInstance();
    
    private Paint dayTextPaint;
    private Paint headerTextPaint;
    private Paint eventDotPaint;
    private Paint todoDotPaint;
    private Drawable selectedDrawable;
    private Drawable todayDrawable;
    
    private float cellWidth;
    private float cellHeight;
    private float paddingTop = 40; // Reduced top padding
    private float dayTextSize = 14;
    private float headerTextSize = 16;
    private float eventDotRadius = 3; // Size of the dot
    
    // Maps to store dates with events by type
    private Map<String, Integer> eventDatesMap = new HashMap<>();
    
    // Event types
    public static final int TYPE_EVENT = 1;
    public static final int TYPE_TODO = 2;
    public static final int TYPE_BOTH = 3;
    
    // Animation related
    private float animOffset = 0;
    private ValueAnimator slideAnimator;
    private int animDirection = 0; // -1:left swipe, 1:right swipe, 0:no animation
    
    private OnDateSelectedListener onDateSelectedListener;
    private OnMonthChangedListener onMonthChangedListener;
    private GestureDetectorCompat gestureDetector;
    
    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }
    
    public interface OnMonthChangedListener {
        void onMonthChanged(Calendar newMonth);
    }
    
    public CustomCalendarView(Context context) {
        super(context);
        init(context);
    }
    
    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayTextPaint.setColor(Color.BLACK);
        dayTextPaint.setTextSize(convertDpToPx(dayTextSize));
        dayTextPaint.setTextAlign(Paint.Align.CENTER);
        
        headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(Color.DKGRAY);
        headerTextPaint.setTextSize(convertDpToPx(headerTextSize));
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        
        eventDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventDotPaint.setColor(Color.parseColor("#FFA500")); // Orange for events
        eventDotPaint.setStyle(Paint.Style.FILL);
        
        todoDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todoDotPaint.setColor(Color.parseColor("#FF0000")); // Red for todos
        todoDotPaint.setStyle(Paint.Style.FILL);
        
        selectedDrawable = ContextCompat.getDrawable(context, R.drawable.selected_date_background);
        todayDrawable = ContextCompat.getDrawable(context, R.drawable.today_date_background);
        
        // Initialize gesture detector
        gestureDetector = new GestureDetectorCompat(context, new CalendarGestureListener());
        
        // Initialize animations
        setupAnimations();
    }
    
    /**
     * Setup slide animations
     */
    private void setupAnimations() {
        slideAnimator = new ValueAnimator();
        slideAnimator.setInterpolator(new DecelerateInterpolator());
        slideAnimator.setDuration(300); // 300ms animation duration
        slideAnimator.addUpdateListener(animation -> {
            animOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
    }
    
    /**
     * Start month change animation
     */
    private void startMonthChangeAnimation(int direction) {
        animDirection = direction;
        
        float start = (direction > 0) ? -getWidth() : getWidth();
        float end = 0;
        
        slideAnimator.cancel();
        slideAnimator.setFloatValues(start, end);
        slideAnimator.start();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellWidth = w / 7f;
        cellHeight = (h - paddingTop) / 6f;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Save canvas state
        canvas.save();
        
        // Translate canvas based on animation offset
        if (animDirection != 0 && slideAnimator.isRunning()) {
            canvas.translate(animOffset, 0);
        }
        
        // Draw weekday headers
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            canvas.drawText(weekDays[i], cellWidth * i + cellWidth / 2, paddingTop / 2 + headerTextPaint.getTextSize() / 3, headerTextPaint);
        }
        
        // Get the day of week for the first day of month
        Calendar firstDayCalendar = (Calendar) currentCalendar.clone();
        firstDayCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = firstDayCalendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday, 1 = Monday, ...
        
        // Get days in month
        int daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Calculate required rows
        int rowsNeeded = (int) Math.ceil((firstDayOfWeek + daysInMonth) / 7.0);
        // Adjust cell height to fit required rows
        float adjustedCellHeight = (getHeight() - paddingTop) / Math.max(6, rowsNeeded);
        
        // Draw dates
        int dayCount = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int position = row * 7 + col;
                
                if (position >= firstDayOfWeek && dayCount <= daysInMonth) {
                    float x = col * cellWidth + cellWidth / 2;
                    float y = row * adjustedCellHeight + paddingTop + adjustedCellHeight / 2;
                    
                    Calendar dayCalendar = (Calendar) currentCalendar.clone();
                    dayCalendar.set(Calendar.DAY_OF_MONTH, dayCount);
                    
                    // Check if it's today
                    boolean isToday = isSameDay(dayCalendar, todayCalendar);
                    
                    // Check if it's selected date
                    boolean isSelected = isSameDay(dayCalendar, selectedCalendar);
                    
                    // Check event types for date
                    int eventType = getEventTypeForDate(dayCalendar);
                    
                    // Draw background
                    if (isSelected) {
                        drawCenteredDrawable(canvas, selectedDrawable, x, y);
                    } else if (isToday) {
                        drawCenteredDrawable(canvas, todayDrawable, x, y);
                    }
                    
                    // Draw text
                    canvas.drawText(String.valueOf(dayCount), x, y + dayTextPaint.getTextSize() / 3, dayTextPaint);
                    
                    // Draw event dots based on type
                    if (eventType != 0) {
                        float dotY = y + dayTextPaint.getTextSize() + convertDpToPx(5);
                        
                        if (eventType == TYPE_EVENT || eventType == TYPE_BOTH) {
                            // Draw event dot (right or center)
                            float eventDotX = (eventType == TYPE_BOTH) ? x + convertDpToPx(4) : x;
                            canvas.drawCircle(eventDotX, dotY, convertDpToPx(eventDotRadius), eventDotPaint);
                        }
                        
                        if (eventType == TYPE_TODO || eventType == TYPE_BOTH) {
                            // Draw todo dot (left or center)
                            float todoDotX = (eventType == TYPE_BOTH) ? x - convertDpToPx(4) : x;
                            canvas.drawCircle(todoDotX, dotY, convertDpToPx(eventDotRadius), todoDotPaint);
                        }
                    }
                    
                    dayCount++;
                }
            }
        }
        
        // Restore canvas state
        canvas.restore();
    }
    
    private void drawCenteredDrawable(Canvas canvas, Drawable drawable, float x, float y) {
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        
        int left = (int) (x - drawableWidth / 2);
        int top = (int) (y - drawableHeight / 2);
        int right = left + drawableWidth;
        int bottom = top + drawableHeight;
        
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the gesture detector handle it first
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        
        // Handle click events
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            
            if (y > paddingTop) {
                int col = (int) (x / cellWidth);
                int row = (int) ((y - paddingTop) / cellHeight);
                
                if (row >= 0 && row < 6 && col >= 0 && col < 7) {
                    Calendar firstDayCalendar = (Calendar) currentCalendar.clone();
                    firstDayCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    int firstDayOfWeek = firstDayCalendar.get(Calendar.DAY_OF_WEEK) - 1;
                    
                    int position = row * 7 + col;
                    int dayIndex = position - firstDayOfWeek + 1;
                    
                    if (dayIndex > 0 && dayIndex <= currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                        selectedCalendar = (Calendar) currentCalendar.clone();
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayIndex);
                        
                        if (onDateSelectedListener != null) {
                            onDateSelectedListener.onDateSelected(selectedCalendar);
                        }
                        
                        invalidate();
                        return true;
                    }
                }
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    /**
     * Add an event marker for the specified date with type
     */
    public void addEvent(Calendar date, int type) {
        String dateKey = getDateKey(date);
        
        // Check if date already has events
        Integer existingType = eventDatesMap.get(dateKey);
        if (existingType != null) {
            // If different types, combine them
            if (existingType != type && existingType != TYPE_BOTH) {
                eventDatesMap.put(dateKey, TYPE_BOTH);
            }
        } else {
            // No existing event, add new one
            eventDatesMap.put(dateKey, type);
        }
        
        invalidate();
    }
    
    /**
     * Remove an event marker for the specified date and type
     */
    public void removeEvent(Calendar date, int type) {
        String dateKey = getDateKey(date);
        
        // Check existing event type
        Integer existingType = eventDatesMap.get(dateKey);
        if (existingType != null) {
            if (existingType == TYPE_BOTH) {
                // If removing one type from a combined marker
                if (type == TYPE_EVENT) {
                    eventDatesMap.put(dateKey, TYPE_TODO);
                } else if (type == TYPE_TODO) {
                    eventDatesMap.put(dateKey, TYPE_EVENT);
                }
            } else {
                // If removing the only marker
                eventDatesMap.remove(dateKey);
            }
        }
        
        invalidate();
    }
    
    /**
     * Clear all event markers
     */
    public void clearEvents() {
        eventDatesMap.clear();
        invalidate();
    }
    
    /**
     * Get event type for the specified date
     */
    public int getEventTypeForDate(Calendar date) {
        String dateKey = getDateKey(date);
        Integer type = eventDatesMap.get(dateKey);
        return type != null ? type : 0;
    }
    
    /**
     * Generate a unique key for the date (YYYY-MM-DD format)
     */
    private String getDateKey(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }
    
    /**
     * Set current month
     */
    public void setCurrentMonth(int year, int month) {
        currentCalendar.set(Calendar.YEAR, year);
        currentCalendar.set(Calendar.MONTH, month);
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        
        if (onMonthChangedListener != null) {
            onMonthChangedListener.onMonthChanged((Calendar) currentCalendar.clone());
        }
        
        invalidate();
    }
    
    /**
     * Set selected date
     */
    public void setSelectedDate(Calendar date) {
        selectedCalendar.setTimeInMillis(date.getTimeInMillis());
        
        if (selectedCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH) ||
            selectedCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) {
            currentCalendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR));
            currentCalendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH));
        }
        
        invalidate();
    }
    
    /**
     * Get current month
     */
    public Calendar getCurrentMonth() {
        return (Calendar) currentCalendar.clone();
    }

    /**
     * Get selected date
     */
    public Calendar getSelectedDate() {
        return (Calendar) selectedCalendar.clone();
    }
    
    /**
     * Set date selection listener
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }
    
    /**
     * Set month change listener
     */
    public void setOnMonthChangedListener(OnMonthChangedListener listener) {
        this.onMonthChangedListener = listener;
    }
    
    /**
     * Go to previous month
     */
    public void previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        startMonthChangeAnimation(-1);
        
        if (onMonthChangedListener != null) {
            onMonthChangedListener.onMonthChanged((Calendar) currentCalendar.clone());
        }
    }
    
    /**
     * Go to next month
     */
    public void nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        startMonthChangeAnimation(1);
        
        if (onMonthChangedListener != null) {
            onMonthChangedListener.onMonthChanged((Calendar) currentCalendar.clone());
        }
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    private float convertDpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
    
    /**
     * Calendar gesture listener to handle swipe gestures
     */
    private class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50; // Lower threshold for better sensitivity
        private static final int SWIPE_VELOCITY_THRESHOLD = 50; // Lower velocity threshold
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) {
                return false;
            }
            
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            
            if (Math.abs(diffX) > Math.abs(diffY) && 
                Math.abs(diffX) > SWIPE_THRESHOLD && 
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                
                if (diffX > 0) {
                    // Swipe right - go to previous month
                    previousMonth();
                } else {
                    // Swipe left - go to next month
                    nextMonth();
                }
                return true;
            }
            return false;
        }
    }
} 