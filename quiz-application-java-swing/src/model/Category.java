package model;

/**
 * Category.java
 *
 * Represents a quiz subject category (e.g., Java, DBMS, Aptitude).
 * Used inside JComboBox components so users can select their subject.
 */
public class Category {
    private int id;
    private String name;
    private String description;

    public Category() {}

    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Category(String name, String description) {
        this(-1, name, description);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Crucial for JComboBox: displays the category name in dropdown menus
    @Override
    public String toString() {
        return name;
    }
}
