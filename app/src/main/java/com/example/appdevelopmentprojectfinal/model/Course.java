package com.example.appdevelopmentprojectfinal.model;

import java.util.List;

public class Course {
    private String id;
    private String name;
    private String relatedModule;
    private String description;
    private CourseContent content;
    private double price;
    private String logo;
    private List<String> tags;
    private String author;
    private List<Review> reviews;
    private double averageRating;
    private CourseStatistics statistics;

    public Course() {
        // Empty constructor required for JSON deserialization
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelatedModule() {
        return relatedModule;
    }

    public void setRelatedModule(String relatedModule) {
        this.relatedModule = relatedModule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CourseContent getContent() {
        return content;
    }

    public void setContent(CourseContent content) {
        this.content = content;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public CourseStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(CourseStatistics statistics) {
        this.statistics = statistics;
    }

    // Helper method to get the module code from the related module
    public String getModuleCode() {
        return relatedModule != null ? relatedModule : "";
    }

    // Inner classes for nested objects
    public static class CourseContent {
        private List<Chapter> chapters;
        private Preview preview;

        public CourseContent() {
            // Empty constructor required for JSON deserialization
        }

        public List<Chapter> getChapters() {
            return chapters;
        }

        public void setChapters(List<Chapter> chapters) {
            this.chapters = chapters;
        }

        public Preview getPreview() {
            return preview;
        }

        public void setPreview(Preview preview) {
            this.preview = preview;
        }
    }

    public static class Chapter {
        private String title;
        private List<ContentItem> items;

        public Chapter() {
            // Empty constructor required for JSON deserialization
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<ContentItem> getItems() {
            return items;
        }

        public void setItems(List<ContentItem> items) {
            this.items = items;
        }
    }

    public static class Preview {
        private String title;
        private List<ContentItem> items;

        public Preview() {
            // Empty constructor required for JSON deserialization
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<ContentItem> getItems() {
            return items;
        }

        public void setItems(List<ContentItem> items) {
            this.items = items;
        }
    }

    public static class ContentItem {
        private String type;
        private String url;
        private String title;
        private String content;
        private String caption;

        public ContentItem() {
            // Empty constructor required for JSON deserialization
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class Review {
        private String user;
        private double rating;
        private String comment;

        public Review() {
            // Empty constructor required for JSON deserialization
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public static class CourseStatistics {
        private int totalPurchases;
        private int viewsToday;
        private int purchasesToday;

        public CourseStatistics() {
            // Empty constructor required for JSON deserialization
        }

        public int getTotalPurchases() {
            return totalPurchases;
        }

        public void setTotalPurchases(int totalPurchases) {
            this.totalPurchases = totalPurchases;
        }

        public int getViewsToday() {
            return viewsToday;
        }

        public void setViewsToday(int viewsToday) {
            this.viewsToday = viewsToday;
        }

        public int getPurchasesToday() {
            return purchasesToday;
        }

        public void setPurchasesToday(int purchasesToday) {
            this.purchasesToday = purchasesToday;
        }
    }
}