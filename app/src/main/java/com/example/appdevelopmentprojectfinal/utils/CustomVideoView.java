package com.example.appdevelopmentprojectfinal.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import java.io.FileDescriptor;
import java.io.IOException;

// Handles playing videos from AssetFileDescriptor instead of just URI
public class CustomVideoView extends VideoView {
    private static final String TAG = "TimetableApp:CustomVideoView";
    private AssetFileDescriptor afd;
    
    // Listener storage
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer.OnErrorListener onErrorListener;
    
    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.onCompletionListener = listener;
        super.setOnCompletionListener(listener);
    }
    
    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        this.onErrorListener = listener;
        super.setOnErrorListener(listener);
    }
    
    
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    // Regular VideoView can't handle assets directly - this bridges that gap
    public void setVideoDescriptor(AssetFileDescriptor afd) throws IOException {
        this.afd = afd;
        try {
            FileDescriptor fd = afd.getFileDescriptor();
            
            // Need to extract duration first before we trick the VideoView
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(fd, afd.getStartOffset(), afd.getLength());
            mp.prepare();
            
            // Store duration for progress tracking (in milliseconds)
            int duration = mp.getDuration();
            mp.release();
            
            // Need to set a dummy URI or VideoView won't initialize properly
            String uriString = "content://media/asset_video";
            setVideoURI(Uri.parse(uriString));
            
            Log.d(TAG, "Video descriptor initialized, duration: " + duration + "ms");
            
            // Override the default MediaPlayer setup in VideoView
            setOnPreparedListener(mediaPlayer -> {
                try {
                    Log.d(TAG, "Configuring media player with asset file descriptor");
                    
                    // Reset and configure the MediaPlayer with our asset
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(fd, afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                    
                    // Apply stored listeners if they've been set
                    if (onCompletionListener != null) {
                        mediaPlayer.setOnCompletionListener(onCompletionListener);
                    }
                    
                    if (onErrorListener != null) {
                        mediaPlayer.setOnErrorListener(onErrorListener);
                    }
                    
                    Log.d(TAG, "Media player successfully configured with asset");
                } catch (IOException e) {
                    // Failed to access video file descriptor - likely an invalid asset path
                    Log.e(TAG, "I/O error preparing media player: " + e.getMessage(), e);
                } catch (Exception e) {
                    // Other unexpected errors during media player setup
                    Log.e(TAG, "Unexpected error preparing media player: " + e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error accessing video asset: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error setting up video asset: " + e.getMessage(), e);
            throw new IOException("Failed to set video descriptor", e);
        }
    }
    
    // Cleanup resources when view is detached
    @Override
    protected void onDetachedFromWindow() {
        // Release the asset file descriptor
        if (afd != null) {
            try {
                afd.close();
                Log.d(TAG, "Asset file descriptor closed successfully");
            } catch (IOException e) {
                Log.e(TAG, "Error closing asset file descriptor: " + e.getMessage());
            }
        }
        super.onDetachedFromWindow();
    }
}