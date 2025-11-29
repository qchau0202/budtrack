package vn.edu.tdtu.lhqc.budtrack.models;

/**
 * Category model - represents transaction categories (e.g., Food, Transport, Shopping).
 * Simple model for categorizing transactions.
 */
public class Category {
    private long id;
    private String name;
    private int iconResId; // Drawable resource ID for category icon
    private String color; // Optional: category color (e.g., hex code)

    // Default constructor for database
    public Category() {
    }

    // Constructor for creating new categories
    public Category(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    // Constructor with color
    public Category(String name, int iconResId, String color) {
        this.name = name;
        this.iconResId = iconResId;
        this.color = color;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

