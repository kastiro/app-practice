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
import java.util.List;
import java.util.Locale;

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
        CheckBox checkBox;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            descriptionTextView = itemView.findViewById(R.id.tv_event_description);
            timeTextView = itemView.findViewById(R.id.tv_event_time);
            checkBox = itemView.findViewById(R.id.checkbox_todo);
        }
    }
} 