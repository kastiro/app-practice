package com.example.appdevelopmentprojectfinal.marketplace;

import android.app.Dialog;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.appdevelopmentprojectfinal.R;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;
import com.example.appdevelopmentprojectfinal.model.Course;
import com.example.appdevelopmentprojectfinal.model.Module;
import com.example.appdevelopmentprojectfinal.model.User;
import com.example.appdevelopmentprojectfinal.utils.DataManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

// Full-screen dialog showing course details with video preview and purchase option
public class CourseDetailDialog extends DialogFragment {

    private static final String TAG = "TimetableApp:CourseDialog";
    private static final String ARG_COURSE_ID = "courseId";

    // Allows calling fragment to know when purchase is complete
    public interface PurchaseCompletedListener {
        void onPurchaseCompleted();
    }

    private String courseId;
    private Course course;
    private PurchaseCompletedListener purchaseCompletedListener;

    // Views
    private ImageView ivCourseImage;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private TextView tvModuleInfo;
    private TextView tvAuthor;
    private RatingBar ratingBar;
    private TextView tvRating;
    private TextView tvDescription;
    private TextView tvPreviewTitle;
    private TextView tvPreviewContent;
    private CardView previewContainer;
    private ImageView btnExpandPreview;
    
    // Expanded preview views
    private CardView expandedPreviewContainer;
    private TextView tvExpandedChapterTitle;
    private TextView tvExpandedContent;
    private TextView tvVideoTitle;
    private VideoView videoView;
    private ImageView ivPlayButton;
    private FrameLayout videoContainer;
    private ImageView btnCollapsePreview;
    
    private RecyclerView rvReviews;
    private TextView tvNoReviews;
    private RecyclerView rvRelatedCourses;
    private Button btnCancel;
    private Button btnBuy;

    // Adapters
    private ReviewAdapter reviewAdapter;
    private RelatedCourseAdapter relatedCourseAdapter;

    public static CourseDetailDialog newInstance(String courseId) {
        CourseDetailDialog dialog = new CourseDetailDialog();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        final String TAG = "CourseDetailDialog";
        super.onCreate(savedInstanceState);
        
        // Set full screen dialog style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppDevelopmentProjectFinal_FullScreenDialog);
        Log.d(TAG, "Dialog style set to full screen");

        // Extract course ID from arguments
        if (getArguments() != null) {
            courseId = getArguments().getString(ARG_COURSE_ID);
            Log.d(TAG, "Course ID extracted from arguments: " + courseId);
        } else {
            Log.w(TAG, "No arguments provided to CourseDetailDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        final String TAG = "CourseDetailDialog";
        Log.d(TAG, "Creating view for course detail dialog");
        return inflater.inflate(R.layout.dialog_course_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final String TAG = "CourseDetailDialog";
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "View created, initializing components");

        // Initialize views
        initViews(view);

        // Set up toolbar navigation (back button)
        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Navigation back clicked, dismissing dialog");
            dismiss();
        });

        // Load course data from DataManager
        if (courseId == null || courseId.isEmpty()) {
            Log.e(TAG, "Cannot load course: courseId is null or empty");
            Toast.makeText(requireContext(), getString(R.string.course_id_missing), Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        
        course = DataManager.getInstance().getCourseById(courseId);
        if (course != null) {
            Log.i(TAG, "Course loaded successfully: " + course.getName());
            displayCourseDetails();
        } else {
            Log.e(TAG, "Course not found for ID: " + courseId);
            Toast.makeText(requireContext(), getString(R.string.course_not_found), Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Set up button listeners for actions
        setupButtons();
        
        Log.i(TAG, "Course detail dialog initialization complete");
    }

    private void initViews(View view) {
        ivCourseImage = view.findViewById(R.id.ivCourseImage);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        toolbar = view.findViewById(R.id.toolbar);
        tvModuleInfo = view.findViewById(R.id.tvModuleInfo);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvRating = view.findViewById(R.id.tvRating);
        tvDescription = view.findViewById(R.id.tvDescription);
        
        // Collapsed preview views
        tvPreviewTitle = view.findViewById(R.id.tvPreviewTitle);
        tvPreviewContent = view.findViewById(R.id.tvPreviewContent);
        previewContainer = view.findViewById(R.id.previewContainer);
        btnExpandPreview = view.findViewById(R.id.btnExpandPreview);
        
        // Expanded preview views
        expandedPreviewContainer = view.findViewById(R.id.expandedPreviewContainer);
        tvExpandedChapterTitle = view.findViewById(R.id.tvExpandedChapterTitle);
        tvExpandedContent = view.findViewById(R.id.tvExpandedContent);
        tvVideoTitle = view.findViewById(R.id.tvVideoTitle);
        videoView = view.findViewById(R.id.videoView);
        ivPlayButton = view.findViewById(R.id.ivPlayButton);
        videoContainer = view.findViewById(R.id.videoContainer);
        btnCollapsePreview = view.findViewById(R.id.btnCollapsePreview);
        
        rvReviews = view.findViewById(R.id.rvReviews);
        tvNoReviews = view.findViewById(R.id.tvNoReviews);
        rvRelatedCourses = view.findViewById(R.id.rvRelatedCourses);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnBuy = view.findViewById(R.id.btnBuy);
        
        // Set up preview expansion/collapse listeners
        setupPreviewExpansion();
    }

    private void displayCourseDetails() {
        final String TAG = "CourseDetailDialog";
        
        Log.d(TAG, "Displaying details for course: " + course.getId() + " - " + course.getName());
        
        // Set course title in the collapsing toolbar
        collapsingToolbar.setTitle(course.getName());

        // Set course image - use color based on module code for consistent display
        setCourseImage();

        // Set module info
        setModuleInfo();

        // Set author
        tvAuthor.setText(getString(R.string.by_author, course.getAuthor()));
        Log.d(TAG, "Author: " + course.getAuthor());

        // Set rating
        setRatingInfo();

        // Set description
        tvDescription.setText(course.getDescription());
        Log.d(TAG, "Description set: " + 
              (course.getDescription().length() > 50 ? 
               course.getDescription().substring(0, 50) + "..." : 
               course.getDescription()));

        // Set preview content
        setPreviewContent();

        // Set reviews
        setReviewsSection();

        // Set related courses
        setRelatedCourses();

        // Set buy button text and state
        configureBuyButton();
        
        Log.i(TAG, "Course detail display completed");
    }
    
    private void setCourseImage() {
        final String TAG = "CourseDetailDialog";
        
        // Get a consistent color based on the course ID - this ensures the same course
        // always gets the same color for better recognition
        int colorCode;
        if (course.getRelatedModule() != null) {
            // Generate color based on module code hash for consistency
            int hash = course.getRelatedModule().hashCode();
            // Use limited palette of nice colors
            int[] colors = {
                0xFF4CAF50, // Green
                0xFF2196F3, // Blue
                0xFFFF9800, // Orange
                0xFF9C27B0, // Purple
                0xFFE91E63  // Pink
            };
            colorCode = colors[Math.abs(hash) % colors.length];
        } else {
            // Default color if no module code
            colorCode = 0xFF4CAF50; // Green
        }
        
        // Set background color
        ivCourseImage.setBackgroundColor(colorCode);
        
        Log.d(TAG, "Set course image background color: #" + 
              Integer.toHexString(colorCode).substring(2).toUpperCase());
    }
    
    private void setModuleInfo() {
        final String TAG = "CourseDetailDialog";
        
        Module module = DataManager.getInstance().getModuleByCode(course.getRelatedModule());
        if (module != null) {
            String moduleInfo = String.format("%s: %s", module.getCode(), module.getName());
            tvModuleInfo.setText(moduleInfo);
            Log.d(TAG, "Module info set: " + moduleInfo);
        } else {
            tvModuleInfo.setText(course.getRelatedModule());
            Log.w(TAG, "Module not found for code: " + course.getRelatedModule());
        }
    }
    
    private void setRatingInfo() {
        final String TAG = "CourseDetailDialog";
        
        double averageRating = course.getAverageRating();
        int numReviews = course.getReviews() != null ? course.getReviews().size() : 0;
        
        ratingBar.setRating((float) averageRating);
        tvRating.setText(getString(R.string.rating_count, averageRating, numReviews));
        
        Log.d(TAG, String.format("Rating: %.1f from %d reviews", averageRating, numReviews));
    }
    
    private void setPreviewContent() {
        final String TAG = "CourseDetailDialog";
        
        if (course.getContent() == null || course.getContent().getPreview() == null) {
            Log.w(TAG, "No preview content available for this course");
            previewContainer.setVisibility(View.GONE);
            return;
        }
        
        Course.Preview preview = course.getContent().getPreview();
        if (preview.getTitle() == null || preview.getTitle().isEmpty()) {
            Log.w(TAG, "Preview has no title");
            tvPreviewTitle.setText(getString(R.string.preview));
        } else {
            tvPreviewTitle.setText(preview.getTitle());
            Log.d(TAG, "Preview title: " + preview.getTitle());
        }
        
        if (preview.getItems() == null || preview.getItems().isEmpty()) {
            Log.w(TAG, "Preview has no content items");
            previewContainer.setVisibility(View.GONE);
            return;
        }
        
        // Build a preview description from all text items
        StringBuilder previewText = new StringBuilder();
        int textItemsFound = 0;
        
        // First pass - collect all text content
        for (Course.ContentItem item : preview.getItems()) {
            if ("text".equals(item.getType()) && item.getContent() != null) {
                if (textItemsFound > 0) {
                    previewText.append("\n\n");
                }
                previewText.append(item.getContent());
                textItemsFound++;
                
                Log.d(TAG, "Added preview text item: " + 
                     (item.getContent().length() > 50 ? 
                      item.getContent().substring(0, 50) + "..." : 
                      item.getContent()));
            }
        }
        
        // If no text items, mention other content types found
        if (textItemsFound == 0) {
            // Count content types
            int videoCount = 0;
            int imageCount = 0;
            
            for (Course.ContentItem item : preview.getItems()) {
                if ("video".equals(item.getType())) {
                    videoCount++;
                } else if ("image".equals(item.getType())) {
                    imageCount++;
                }
            }
            
            // Build preview description based on available content
            if (videoCount > 0 || imageCount > 0) {
                StringBuilder contentDesc = new StringBuilder(getString(R.string.preview_contains));
                
                if (videoCount > 0) {
                    contentDesc.append(" ").append(videoCount).append(" ");
                    String videoText = videoCount == 1 ? 
                                     getString(R.string.video) : 
                                     getString(R.string.videos);
                    contentDesc.append(videoText);
                }
                
                if (videoCount > 0 && imageCount > 0) {
                    contentDesc.append(" ").append(getString(R.string.and)).append(" ");
                }
                
                if (imageCount > 0) {
                    contentDesc.append(" ").append(imageCount).append(" ");
                    String imageText = imageCount == 1 ? 
                                     getString(R.string.image) : 
                                     getString(R.string.images);
                    contentDesc.append(imageText);
                }
                
                previewText.append(contentDesc);
                Log.d(TAG, "No text content found in preview, showing content summary");
            } else {
                // No recognizable content
                Log.w(TAG, "No usable content found in preview items");
                previewText.append(getString(R.string.preview_tap_to_see));
            }
        }
        
        // Set the preview text
        if (previewText.length() > 0) {
            tvPreviewContent.setText(previewText.toString());
        } else {
            // Fallback if nothing could be built
            tvPreviewContent.setText(getString(R.string.preview_tap_to_see));
        }
    }
    
    private void setReviewsSection() {
        final String TAG = "CourseDetailDialog";
        
        if (course.getReviews() == null || course.getReviews().isEmpty()) {
            Log.w(TAG, "No reviews available for this course");
            rvReviews.setVisibility(View.GONE);
            tvNoReviews.setVisibility(View.VISIBLE);
            return;
        }
        
        reviewAdapter = new ReviewAdapter(course.getReviews());
        rvReviews.setAdapter(reviewAdapter);
        tvNoReviews.setVisibility(View.GONE);
        
        Log.d(TAG, "Set up " + course.getReviews().size() + " reviews");
    }
    
    private void setRelatedCourses() {
        final String TAG = "CourseDetailDialog";
        
        List<Course> relatedCourses = DataManager.getInstance().getRelatedCourses(course);
        if (relatedCourses.isEmpty()) {
            Log.w(TAG, "No related courses found");
            rvRelatedCourses.setVisibility(View.GONE);
            return;
        }
        
        relatedCourseAdapter = new RelatedCourseAdapter(relatedCourses, this::showRelatedCourseDetail);
        rvRelatedCourses.setAdapter(relatedCourseAdapter);
        
        Log.d(TAG, "Set up " + relatedCourses.size() + " related courses");
    }
    
    private void configureBuyButton() {
        final String TAG = "CourseDetailDialog";
        
        // Format currency for display
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        format.setCurrency(Currency.getInstance("EUR"));
        String formattedPrice = format.format(course.getPrice());
        
        // Set default buy button state
        btnBuy.setText(getString(R.string.buy_price, formattedPrice));
        btnBuy.setEnabled(true);
        
        // Check if user already owns this course
        User currentUser = DataManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.ownsModule(course.getId())) {
                btnBuy.setText(getString(R.string.owned));
                btnBuy.setEnabled(false);
                Log.d(TAG, "User already owns this course");
            } else {
                Log.d(TAG, "Course available for purchase at " + formattedPrice);
            }
        } else {
            Log.w(TAG, "Current user is null, cannot check ownership status");
        }
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnBuy.setOnClickListener(v -> {
            User currentUser = DataManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser.ownsModule(course.getId())) {
                Toast.makeText(requireContext(), getString(R.string.already_owned), Toast.LENGTH_SHORT).show();
                return;
            }

            // Show purchase confirmation dialog
            showPurchaseConfirmationDialog();
        });
    }
    
    private void setupPreviewExpansion() {
        // Set up expand button click listener
        btnExpandPreview.setOnClickListener(v -> {
            // Hide collapsed preview, show expanded preview
            previewContainer.setVisibility(View.GONE);
            expandedPreviewContainer.setVisibility(View.VISIBLE);
            
            // Prepare the first chapter content from the course
            prepareExpandedPreview();
        });
        
        // Set up collapse button click listener
        btnCollapsePreview.setOnClickListener(v -> {
            // Hide expanded preview, show collapsed preview
            expandedPreviewContainer.setVisibility(View.GONE);
            previewContainer.setVisibility(View.VISIBLE);
            
            // Stop video if playing
            if (videoView.isPlaying()) {
                videoView.stopPlayback();
            }
        });
        
        // Set up play button click listener
        ivPlayButton.setOnClickListener(v -> {
            playVideo();
        });
    }
    
    private void prepareExpandedPreview() {
        final String TAG = "CourseDetailDialog";
        
        if (course == null || course.getContent() == null || course.getContent().getChapters() == null 
                || course.getContent().getChapters().isEmpty()) {
            Log.w(TAG, "Cannot prepare expanded preview: course content is missing or empty");
            expandedPreviewContainer.setVisibility(View.GONE);
            return;
        }
            
        // Get the first chapter
        Course.Chapter firstChapter = course.getContent().getChapters().get(0);
        Log.d(TAG, "Preparing expanded preview for chapter: " + firstChapter.getTitle());
        tvExpandedChapterTitle.setText(firstChapter.getTitle());
        
        // Set expanded content text
        StringBuilder contentBuilder = new StringBuilder();
        String videoTitle = "";
        String videoUrl = null;
        
        // Find text and video items for the expanded content
        if (firstChapter.getItems() != null) {
            // Log the number of content items in this chapter
            Log.d(TAG, "Chapter has " + firstChapter.getItems().size() + " content items");
            
            for (Course.ContentItem item : firstChapter.getItems()) {
                // Process each content item based on its type
                String itemType = item.getType();
                if (itemType == null) {
                    Log.w(TAG, "Skipping content item with null type");
                    continue;
                }
                
                switch (itemType) {
                    case "text":
                        if (item.getContent() != null) {
                            Log.d(TAG, "Adding text content: " + 
                                  (item.getContent().length() > 50 ? 
                                   item.getContent().substring(0, 50) + "..." : 
                                   item.getContent()));
                            contentBuilder.append(item.getContent()).append("\n\n");
                        } else {
                            Log.w(TAG, "Text content item has null content");
                        }
                        break;
                        
                    case "video":
                        Log.d(TAG, "Found video content item");
                        // Set video title if available
                        if (item.getTitle() != null) {
                            videoTitle = item.getTitle();
                            Log.d(TAG, "Video title: " + videoTitle);
                        }
                        
                        // Store video URL for later use
                        if (item.getUrl() != null) {
                            videoUrl = item.getUrl();
                            Log.d(TAG, "Video URL: " + videoUrl);
                        }
                        break;
                        
                    case "image":
                        Log.d(TAG, "Found image content item" + 
                             (item.getCaption() != null ? ": " + item.getCaption() : ""));
                        // We could add image handling here if needed
                        break;
                        
                    default:
                        Log.w(TAG, "Unknown content item type: " + itemType);
                        break;
                }
            }
        } else {
            Log.w(TAG, "Chapter has no content items");
        }
        
        // Set video title
        if (!videoTitle.isEmpty()) {
            tvVideoTitle.setText(videoTitle);
            tvVideoTitle.setVisibility(View.VISIBLE);
        } else {
            tvVideoTitle.setVisibility(View.GONE);
        }
        
        // Configure video view based on whether we have a video
        if (videoUrl != null) {
            // Show video container
            videoContainer.setVisibility(View.VISIBLE);
            
            // Set up video view behavior
            setUpVideoView(videoUrl);
        } else {
            // No video to show
            Log.d(TAG, "No video URL available, hiding video container");
            videoContainer.setVisibility(View.GONE);
        }
        
        // Set the content text if available
        if (contentBuilder.length() > 0) {
            tvExpandedContent.setText(contentBuilder.toString());
            tvExpandedContent.setVisibility(View.VISIBLE);
            Log.d(TAG, "Content text set with " + contentBuilder.length() + " characters");
        } else {
            // If no content is available, hide the text view
            tvExpandedContent.setVisibility(View.GONE);
            Log.w(TAG, "No content text available for chapter");
        }
        
        Log.i(TAG, "Expanded preview prepared successfully");
    }
    
    private void setUpVideoView(String videoUrl) {
        final String TAG = "CourseDetailDialog";
        Log.d(TAG, "Setting up video view for URL: " + videoUrl);
        
        try {
            // Lookup video based on course ID and video URL
            // This creates a mapping system between the JSON data and local resources
            try {
                // Step 1: Try to find the exact video based on course ID and content URL
                String courseSpecificResource = generateResourceName(course.getId(), videoUrl);
                Log.d(TAG, "Looking for course-specific video resource: " + courseSpecificResource);
                
                // Try to find this resource
                int resourceId = findResourceId(courseSpecificResource);
                if (resourceId != 0) {
                    // Found the exact video for this course's content
                    String videoPath = "android.resource://" + requireContext().getPackageName() + "/" + resourceId;
                    videoView.setVideoURI(Uri.parse(videoPath));
                    Log.d(TAG, "Found course-specific video: " + courseSpecificResource + " (id: " + resourceId + ")");
                    return;
                }
                
                // Step 2: Try to find generic video based on just the URL
                String filename = getFilenameFromUrl(videoUrl);
                if (filename != null && !filename.isEmpty()) {
                    String genericResource = convertToResourceName(filename);
                    Log.d(TAG, "Looking for generic video resource by filename: " + genericResource);
                    
                    resourceId = findResourceId(genericResource);
                    if (resourceId != 0) {
                        // Found generic video that matches the URL filename
                        String videoPath = "android.resource://" + requireContext().getPackageName() + "/" + resourceId;
                        videoView.setVideoURI(Uri.parse(videoPath));
                        Log.d(TAG, "Found generic video by filename: " + genericResource + " (id: " + resourceId + ")");
                        return;
                    }
                }
                
                // Step 3: Try course-specific default video
                String courseDefaultResource = "course_" + convertToResourceName(course.getId());
                Log.d(TAG, "Looking for course default video: " + courseDefaultResource);
                
                resourceId = findResourceId(courseDefaultResource);
                if (resourceId != 0) {
                    // Found default video for this course
                    String videoPath = "android.resource://" + requireContext().getPackageName() + "/" + resourceId;
                    videoView.setVideoURI(Uri.parse(videoPath));
                    Log.d(TAG, "Found course default video: " + courseDefaultResource + " (id: " + resourceId + ")");
                    return;
                }
                
                // Step 4: Fall back to the global default video
                Log.d(TAG, "No specific video found, using default demo video");
                String videoPath = "android.resource://" + requireContext().getPackageName() + "/raw/course_preview_demo";
                videoView.setVideoURI(Uri.parse(videoPath));
                
            } catch (Exception e) {
                // Handle any errors in video setup process
                Log.w(TAG, "Error looking up video resources: " + e.getMessage());
                Log.d(TAG, "Using default demo video instead");
                
                // Set up the demo video (course_preview_demo.mp4 in raw folder)
                String videoPath = "android.resource://" + requireContext().getPackageName() + "/raw/course_preview_demo";
                videoView.setVideoURI(Uri.parse(videoPath));
            }
            
            // Set up completion listener to show play button when video ends
            videoView.setOnCompletionListener(mp -> {
                Log.d(TAG, "Video playback completed");
                ivPlayButton.setVisibility(View.VISIBLE);
            });
            
            // Set up the remaining video listeners
            setupVideoListeners();
            
            // Show play button initially
            ivPlayButton.setVisibility(View.VISIBLE);
            Log.d(TAG, "Video view configured successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set up video view: " + e.getMessage(), e);
            // Hide video container on critical setup failure
            videoContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Extract filename from a URL
     */
    private String getFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // Get the part after the last slash
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }
        
        return url; // No slash found, return the whole string
    }
    
    /**
     * Convert a filename to a valid Android resource name
     */
    private String convertToResourceName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "course_preview_demo"; // Default fallback
        }
        
        // Remove extension if present
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1) {
            filename = filename.substring(0, dotIndex);
        }
        
        // Convert to lowercase and replace invalid characters with underscore
        return filename.toLowerCase()
                .replaceAll("[^a-z0-9_]", "_");
    }
    
    // Better than getIdentifier with try/catch - returns 0 if not found
    private int findResourceId(String resourceName) {
        android.content.res.Resources resources = requireContext().getResources();
        String packageName = requireContext().getPackageName();
        
        // First try to find it in the raw folder
        int resourceId = resources.getIdentifier(resourceName, "raw", packageName);
        
        // If not found, try in the drawable folder
        if (resourceId == 0) {
            resourceId = resources.getIdentifier(resourceName, "drawable", packageName);
        }
        
        return resourceId; // Returns 0 if resource not found
    }
    
    // Maps URL to valid Android resource name with course-specific prefix
    private String generateResourceName(String courseId, String videoUrl) {
        if (courseId == null || courseId.isEmpty() || videoUrl == null || videoUrl.isEmpty()) {
            return "course_preview_demo"; // Default fallback
        }
        
        // Get the filename part of the URL
        String filename = getFilenameFromUrl(videoUrl);
        if (filename == null || filename.isEmpty()) {
            return "course_" + convertToResourceName(courseId);
        }
        
        // Remove extension from filename
        String filenameWithoutExt = filename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1) {
            filenameWithoutExt = filename.substring(0, dotIndex);
        }
        
        // Combine course ID and filename to create a unique resource name
        // Format: course_[course_id]_[filename]
        return "course_" + convertToResourceName(courseId) + "_" + 
               convertToResourceName(filenameWithoutExt);
    }
    
    private void playVideo() {
        final String TAG = "CourseDetailDialog";
        
        // Hide play button when starting playback
        ivPlayButton.setVisibility(View.GONE);
        
        try {
            Log.d(TAG, "Starting video playback");
            
            // Check if VideoView has been prepared
            if (videoView.getDuration() <= 0) {
                Log.w(TAG, "VideoView not properly prepared. Setting up listeners again.");
                setupVideoListeners();
            }
            
            // Ensure video view has focus for controls
            videoView.requestFocus();
            // Start video - this triggers prepare followed by the onPrepared callback
            videoView.start();
            
            Log.d(TAG, "Video playback requested");
            
        } catch (Exception e) {
            // Log unexpected errors
            Log.e(TAG, "Unexpected error during video playback: " + e.getMessage(), e);
            
            // Show error message to user
            Toast.makeText(requireContext(), 
                    getString(R.string.video_loading_error_message, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
            
            // Show play button so user can try again
            ivPlayButton.setVisibility(View.VISIBLE);
        }
    }
    
    // Handle successful video load and error conditions
    private void setupVideoListeners() {
        final String TAG = "CourseDetailDialog";
        
        // Set up prepared listener for when the video is ready to play
        videoView.setOnPreparedListener(mp -> {
            Log.d(TAG, "Video prepared successfully, starting playback");
            // Configure media player
            mp.setLooping(false);
            mp.setVolume(1.0f, 1.0f);
            
            // Calculate video dimensions for logging
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            int duration = mp.getDuration();
            Log.d(TAG, String.format("Video dimensions: %dx%d, duration: %dms", 
                                    videoWidth, videoHeight, duration));
            
            // Start playback
            videoView.start();
        });
        
        // Set up error listener for playback problems
        videoView.setOnErrorListener((mp, what, extra) -> {
            // Log detailed error information
            Log.e(TAG, String.format("Video playback error: what=%d, extra=%d", what, extra));
            
            // Show error message to user
            Toast.makeText(requireContext(), 
                    getString(R.string.video_error_message), 
                    Toast.LENGTH_SHORT).show();
            
            // Show play button so user can try again
            ivPlayButton.setVisibility(View.VISIBLE);
            
            return true; // Error handled
        });
    }

    private void showPurchaseConfirmationDialog() {
        final String TAG = "CourseDetailDialog";
        
        // Pre-check conditions
        User currentUser = DataManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot show purchase dialog: current user is null");
            Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (course == null) {
            Log.e(TAG, "Cannot show purchase dialog: course is null");
            return;
        }
        
        Log.d(TAG, "Preparing purchase confirmation dialog for course: " + course.getId());

        try {
            // Inflate the dialog layout
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_purchase_confirmation, null);
            if (view == null) {
                Log.e(TAG, "Failed to inflate purchase confirmation dialog layout");
                return;
            }
    
            // Find all views in the dialog layout
            TextView tvPurchaseCourseName = view.findViewById(R.id.tvPurchaseCourseName);
            TextView tvPurchasePrice = view.findViewById(R.id.tvPurchasePrice);
            TextView tvPurchaseBalance = view.findViewById(R.id.tvPurchaseBalance);
            TextView tvPurchaseNewBalance = view.findViewById(R.id.tvPurchaseNewBalance);
            Button btnCancelPurchase = view.findViewById(R.id.btnCancelPurchase);
            Button btnConfirmPurchase = view.findViewById(R.id.btnConfirmPurchase);
            
            // Update button text from string resources
            btnCancelPurchase.setText(getString(R.string.cancel));
            btnConfirmPurchase.setText(getString(R.string.confirm));
    
            // Format currency values for display
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.GERMANY);
            format.setCurrency(Currency.getInstance("EUR"));
            
            // Calculate purchase details
            double currentBalance = currentUser.getWallet();
            double price = course.getPrice();
            double newBalance = currentBalance - price;
            
            // Check if purchase is valid before showing dialog
            if (currentBalance < price) {
                Log.w(TAG, "Insufficient funds for purchase: " + currentBalance + " < " + price);
                Toast.makeText(requireContext(), getString(R.string.insufficient_funds), 
                               Toast.LENGTH_SHORT).show();
                return;
            }
    
            // Set values in dialog
            tvPurchaseCourseName.setText(course.getName());
            tvPurchasePrice.setText(format.format(price));
            tvPurchaseBalance.setText(format.format(currentBalance));
            tvPurchaseNewBalance.setText(format.format(newBalance));
            
            // Log purchase details
            Log.i(TAG, String.format("Purchase confirmation dialog prepared: Course=%s (%s), " +
                                    "Price=%.2f, Balance=%.2f, NewBalance=%.2f",
                                    course.getName(), course.getId(), price, currentBalance, newBalance));
    
            // Create the confirmation dialog
            Dialog confirmationDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setView(view)
                    .setCancelable(true)
                    .create();
    
            // Set cancel button listener
            btnCancelPurchase.setOnClickListener(v -> {
                Log.d(TAG, "Purchase cancelled by user");
                confirmationDialog.dismiss();
            });
            
            // Set confirm button listener
            btnConfirmPurchase.setOnClickListener(v -> {
                Log.d(TAG, "Purchase confirmed by user, processing...");
                
                // Attempt to make the purchase
                boolean purchaseSuccessful = DataManager.getInstance().purchaseCourse(course.getId());
                confirmationDialog.dismiss();
    
                if (purchaseSuccessful) {
                    Log.i(TAG, "Purchase successful: " + course.getName() + " (" + course.getId() + ")");
                    
                    // Show success message
                    Toast.makeText(requireContext(), getString(R.string.purchase_successful), 
                                  Toast.LENGTH_SHORT).show();
                    
                    // Notify listener about successful purchase
                    if (purchaseCompletedListener != null) {
                        purchaseCompletedListener.onPurchaseCompleted();
                        Log.d(TAG, "Purchase completion callback sent");
                    }
                    
                    // Close the course detail dialog
                    dismiss();
                } else {
                    // Purchase failed - likely insufficient funds
                    Log.w(TAG, "Purchase failed for course: " + course.getId());
                    Toast.makeText(requireContext(), getString(R.string.purchase_failed), 
                                  Toast.LENGTH_SHORT).show();
                }
            });
    
            // Show the dialog
            confirmationDialog.show();
            Log.d(TAG, "Purchase confirmation dialog displayed");
            
        } catch (Exception e) {
            // Handle any unexpected errors in dialog creation
            Log.e(TAG, "Error showing purchase confirmation dialog: " + e.getMessage(), e);
            Toast.makeText(requireContext(), getString(R.string.dialog_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRelatedCourseDetail(Course relatedCourse) {
        // Log the navigation
        Log.d("CourseDetailDialog", "Navigating to related course: " + relatedCourse.getName() 
            + " (" + relatedCourse.getId() + ")");
            
        // Dismiss this dialog
        dismiss();

        // Show the related course detail dialog
        CourseDetailDialog dialog = CourseDetailDialog.newInstance(relatedCourse.getId());
        dialog.setPurchaseCompletedListener(purchaseCompletedListener);
        dialog.show(getParentFragmentManager(), "RelatedCourseDetail");
    }

    public void setPurchaseCompletedListener(PurchaseCompletedListener listener) {
        this.purchaseCompletedListener = listener;
    }

    // Adapter for the reviews list
    private static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
        private List<Course.Review> reviews;

        public ReviewAdapter(List<Course.Review> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Course.Review review = reviews.get(position);
            holder.bind(review);
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        static class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView tvReviewerName;
            RatingBar rbReviewRating;
            TextView tvReviewComment;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
                rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
                tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
            }

            public void bind(Course.Review review) {
                tvReviewerName.setText(review.getUser());
                rbReviewRating.setRating((float) review.getRating());
                tvReviewComment.setText(review.getComment());
            }
        }
    }

    // Adapter for the related courses list
    private static class RelatedCourseAdapter extends RecyclerView.Adapter<RelatedCourseAdapter.RelatedCourseViewHolder> {
        private List<Course> relatedCourses;
        private OnRelatedCourseClickListener listener;

        interface OnRelatedCourseClickListener {
            void onRelatedCourseClicked(Course course);
        }

        public RelatedCourseAdapter(List<Course> relatedCourses, OnRelatedCourseClickListener listener) {
            this.relatedCourses = relatedCourses;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RelatedCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_related_course, parent, false);
            return new RelatedCourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RelatedCourseViewHolder holder, int position) {
            Course course = relatedCourses.get(position);
            holder.bind(course);
        }

        @Override
        public int getItemCount() {
            return relatedCourses.size();
        }

        class RelatedCourseViewHolder extends RecyclerView.ViewHolder {
            ImageView ivRelatedCourseLogo;
            TextView tvRelatedCourseTitle;
            TextView tvRelatedCoursePrice;

            public RelatedCourseViewHolder(@NonNull View itemView) {
                super(itemView);
                ivRelatedCourseLogo = itemView.findViewById(R.id.ivRelatedCourseLogo);
                tvRelatedCourseTitle = itemView.findViewById(R.id.tvRelatedCourseTitle);
                tvRelatedCoursePrice = itemView.findViewById(R.id.tvRelatedCoursePrice);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRelatedCourseClicked(relatedCourses.get(position));
                    }
                });
            }

            public void bind(Course course) {
                tvRelatedCourseTitle.setText(course.getName());

                // Format price
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.GERMANY);
                format.setCurrency(Currency.getInstance("EUR"));
                tvRelatedCoursePrice.setText(format.format(course.getPrice()));

                // Set placeholder logo
                ivRelatedCourseLogo.setBackgroundColor(0xFF2196F3); // Blue placeholder
            }
        }
    }
}