package dao;

import db.DBConnection;
import model.Category;
import model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * QuestionDAO.java
 *
 * Data Access Object responsible for all CRUD operations on Questions and Categories.
 * Strictly uses PreparedStatements and try-with-resources.
 */
public class QuestionDAO {

    // =========================================================================
    // CATEGORY OPERATIONS
    // =========================================================================

    /**
     * Retrieves all categories sorted alphabetically.
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")
                ));
            }
        }
        return list;
    }

    /**
     * Adds a new subject category.
     */
    public boolean addCategory(String name, String description) throws SQLException {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.setString(2, description.trim());
            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // QUESTION OPERATIONS
    // =========================================================================

    /**
     * Retrieves all questions belonging to a specific category.
     * Used by the Student QuizFrame.
     */
    public List<Question> getQuestionsByCategory(int categoryId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, category_id, question_text, option_a, option_b, option_c, option_d, correct_option " +
                     "FROM questions WHERE category_id = ? ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Question(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getString("question_text"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        rs.getString("correct_option").charAt(0)
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all questions across all categories for Admin table management.
     */
    public List<Question> getAllQuestionsWithCategory() throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT q.id, q.category_id, c.name AS category_name, q.question_text, " +
                     "q.option_a, q.option_b, q.option_c, q.option_d, q.correct_option " +
                     "FROM questions q " +
                     "JOIN categories c ON q.category_id = c.id " +
                     "ORDER BY q.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Question q = new Question(
                    rs.getInt("id"),
                    rs.getInt("category_id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_option").charAt(0)
                );
                q.setCategoryName(rs.getString("category_name"));
                list.add(q);
            }
        }
        return list;
    }

    /**
     * Inserts a new question.
     */
    public boolean addQuestion(Question q) throws SQLException {
        String sql = "INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_option) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, q.getCategoryId());
            stmt.setString(2, q.getQuestionText().trim());
            stmt.setString(3, q.getOptionA().trim());
            stmt.setString(4, q.getOptionB().trim());
            stmt.setString(5, q.getOptionC().trim());
            stmt.setString(6, q.getOptionD().trim());
            stmt.setString(7, String.valueOf(q.getCorrectOption()));

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing question by ID.
     */
    public boolean updateQuestion(Question q) throws SQLException {
        String sql = "UPDATE questions SET category_id = ?, question_text = ?, " +
                     "option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_option = ? " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, q.getCategoryId());
            stmt.setString(2, q.getQuestionText().trim());
            stmt.setString(3, q.getOptionA().trim());
            stmt.setString(4, q.getOptionB().trim());
            stmt.setString(5, q.getOptionC().trim());
            stmt.setString(6, q.getOptionD().trim());
            stmt.setString(7, String.valueOf(q.getCorrectOption()));
            stmt.setInt(8, q.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a question by ID.
     */
    public boolean deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            return stmt.executeUpdate() > 0;
        }
    }
}
