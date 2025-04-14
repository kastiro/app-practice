package com.example.appdevelopmentprojectfinal.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for displaying calendar events in a RecyclerView.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    
    private List<com.example.appdevelopmentprojectfinal.calendar.Event> events;
    private Context context;
    private SimpleDateFormat dateFormat;
    private OnEventClickListener listener;
    
    public interface OnEventClickListener {
        void onEventClick(com.example.appdevelopmentprojectfinal.calendar.Event event, int position);
        void onTodoStatusChanged(com.example.appdevelopmentprojectfinal.calendar.Event event, int position, boolean isChecked);
    }
    
    public EventAdapter(Context context, List<com.example.appdevelopmentprojectfinal.calendar.Event> events) {
        this.context = context;
        this.events = events;
        this.dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    }
    
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        com.example.appdevelopmentprojectfinal.calendar.Event event = events.get(position);
        
        holder.titleTextView.setText(event.getTitle());
        holder.descriptionTextView.setText(event.getDescription());
        holder.timeTextView.setText(dateFormat.format(event.getDate()));
        
        // Calculate and display countdown
        String countdownText = getCountdownText(event.getDate());
        holder.countdownTextView.setText(countdownText);
        
        // Set countdown text color
        setCountdownColor(holder.countdownTextView, event.getDate());
        
        // Show/hide checkbox based on event type
        if (event.isTodo()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(event.isCompleted());
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        
        // Setup click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event, position);
            }
        });
        
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && event.isTodo()) {
                event.setCompleted(isChecked);
                listener.onTodoStatusChanged(event, position, isChecked);
            }
        });
    }
    
    /**
     * Get countdown text
     */
    private String getCountdownText(Date eventDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(eventDate);
        eventCal.set(Calendar.HOUR_OF_DAY, 0);
        eventCal.set(Calendar.MINUTE, 0);
        eventCal.set(Calendar.SECOND, 0);
        eventCal.set(Calendar.MILLISECOND, 0);
        
        long diffInMillis = eventCal.getTimeInMillis() - today.getTimeInMillis();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        
        if (diffInDays < 0) {
            return context.getString(R.string.past);
        } else if (diffInDays == 0) {
            return context.getString(R.string.today);
        } else if (diffInDays == 1) {
            return context.getString(R.string.tomorrow);
        } else {
            return diffInDays + " " + context.getString(R.string.days);
        }
    }
    
    /**
     * Set countdown text color based on date
     */
    private void setCountdownColor(TextView textView, Date eventDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(eventDate);
        eventCal.set(Calendar.HOUR_OF_DAY, 0);
        eventCal.set(Calendar.MINUTE, 0);
        eventCal.set(Calendar.SECOND, 0);
        eventCal.set(Calendar.MILLISECOND, 0);
        
        long diffInMillis = eventCal.getTimeInMillis() - today.getTimeInMillis();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        
        if (diffInDays < 0) {
            // Expired events - Gray
            textView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else if (diffInDays <= 1) {
            // Today or tomorrow - Red
            textView.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else if (diffInDays <= 3) {
            // Within 3 days - Orange
            textView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            // More than 3 days - Blue
            textView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        }
    }
    
    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }
    
    public void updateEvents(List<com.example.appdevelopmentprojectfinal.calendar.Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
    
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView timeTextView;
        TextView countdownTextView;
        CheckBox checkBox;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            descriptionTextView = itemView.findViewById(R.id.tv_event_description);
            timeTextView = itemView.findViewById(R.id.tv_event_time);
            countdownTextView = itemView.findViewById(R.id.tv_event_countdown);
            checkBox = itemView.findViewById(R.id.checkbox_todo);
        }
    }
} 