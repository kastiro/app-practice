package com.example.appdevelopmentprojectfinal.marketplace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdevelopmentprojectfinal.R;
import com.example.appdevelopmentprojectfinal.model.Course;
import com.example.appdevelopmentprojectfinal.model.Module;
import com.example.appdevelopmentprojectfinal.utils.DataManager;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public interface CourseClickListener {
        void onCourseClicked(Course course);
    }

    private List<Course> courses;
    private CourseClickListener listener;
    private NumberFormat currencyFormat;

    public CourseAdapter(List<Course> courses, CourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
        
        // Set up currency formatter for Euro
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        currencyFormat.setCurrency(Currency.getInstance("EUR"));
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourseLogo;
        TextView tvModuleCode;
        TextView tvCourseTitle;
        TextView tvAuthor;
        RatingBar ratingBar;
        TextView tvRating;
        TextView tvPrice;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourseLogo = itemView.findViewById(R.id.ivCourseLogo);
            tvModuleCode = itemView.findViewById(R.id.tvModuleCode);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCourseClicked(courses.get(position));
                }
            });
        }

        public void bind(Course course) {
            // Set course title and author
            tvCourseTitle.setText(course.getName());
            tvAuthor.setText(itemView.getContext().getString(R.string.by_author, course.getAuthor()));

            // Set module code
            tvModuleCode.setText(course.getRelatedModule());

            // Set rating
            ratingBar.setRating((float) course.getAverageRating());
            
            int numReviews = course.getReviews() != null ? course.getReviews().size() : 0;
            tvRating.setText(itemView.getContext().getString(R.string.rating_count, course.getAverageRating(), numReviews));

            // Set price
            tvPrice.setText(currencyFormat.format(course.getPrice()));

            // Set logo (in a real app, you would load an image here)
            // For now, we'll just set a placeholder based on the module code
            Module module = DataManager.getInstance().getModuleByCode(course.getRelatedModule());
            if (module != null) {
                // In a real app, you would use an image loading library like Glide or Picasso
                // For now, we'll just use different colors based on the module code
                int colorRes;
                switch (module.getCode().charAt(0)) {
                    case 'C':
                        ivCourseLogo.setBackgroundColor(0xFF4CAF50); // Green
                        break;
                    case 'E':
                        ivCourseLogo.setBackgroundColor(0xFF2196F3); // Blue
                        break;
                    case 'M':
                        ivCourseLogo.setBackgroundColor(0xFFFF9800); // Orange
                        break;
                    default:
                        ivCourseLogo.setBackgroundColor(0xFF9C27B0); // Purple
                        break;
                }
                
                // Set the first two letters of the module code as text on the image
                // This would be replaced by actual images in a real app
            }
        }
    }
}