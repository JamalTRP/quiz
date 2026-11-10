package model;

import java.sql.Timestamp;

/**
 * Result.java
 *
 * Represents an individual quiz submission recorded in the database.
 * Includes user metadata, category metadata, scores, and date.
 */
public class Result {
    private int id;
    private int userId;
    private String username;
    private int categoryId;
    private String categoryName;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int score;
    private double percentage;
    private Timestamp takenAt;

    public Result() {}

    public Result(int id, int userId, String username, int categoryId, String categoryName,
                  int totalQuestions, int correctAnswers, int wrongAnswers,
                  int score, double percentage, Timestamp takenAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.score = score;
        this.percentage = percentage;
        this.takenAt = takenAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public Timestamp getTakenAt() { return takenAt; }
    public void setTakenAt(Timestamp takenAt) { this.takenAt = takenAt; }
}
