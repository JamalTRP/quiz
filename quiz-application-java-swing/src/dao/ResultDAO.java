package dao;

import db.DBConnection;
import model.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultDAO.java
 *
 * Handles database operations for the 'results' table.
 * Includes student attempt history, admin total score overview,
 * and top 10 leaderboard queries.
 */
public class ResultDAO {

    /**
     * Saves a completed quiz result into the database.
     *
     * @param result Result object containing scores and stats
     * @return true if successfully saved
     * @throws SQLException On database failure
     */
    public boolean saveResult(Result result) throws SQLException {
        String sql = "INSERT INTO results (user_id, category_id, total_questions, correct_answers, wrong_answers, score, percentage) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, result.getUserId());
            stmt.setInt(2, result.getCategoryId());
            stmt.setInt(3, result.getTotalQuestions());
            stmt.setInt(4, result.getCorrectAnswers());
            stmt.setInt(5, result.getWrongAnswers());
            stmt.setInt(6, result.getScore());
            stmt.setDouble(7, result.getPercentage());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all past quiz attempts for a specific student.
     *
     * @param userId The ID of the student
     * @return List of past Results
     * @throws SQLException On database failure
     */
    public List<Result> getResultsByUser(int userId) throws SQLException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT r.id, r.user_id, u.username, r.category_id, c.name AS category_name, " +
                     "r.total_questions, r.correct_answers, r.wrong_answers, r.score, r.percentage, r.taken_at " +
                     "FROM results r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN categories c ON r.category_id = c.id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.taken_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractResultFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all student scores across all categories (used in Admin Dashboard).
     *
     * @return List of all submitted results
     * @throws SQLException On database failure
     */
    public List<Result> getAllResults() throws SQLException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT r.id, r.user_id, u.username, r.category_id, c.name AS category_name, " +
                     "r.total_questions, r.correct_answers, r.wrong_answers, r.score, r.percentage, r.taken_at " +
                     "FROM results r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN categories c ON r.category_id = c.id " +
                     "ORDER BY r.taken_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractResultFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves the top 10 highest scores for the Leaderboard.
     * Ranked by score descending, percentage descending, then earliest attempt.
     *
     * @return Top 10 Result entries
     * @throws SQLException On database failure
     */
    public List<Result> getTop10Leaderboard() throws SQLException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT r.id, r.user_id, u.username, r.category_id, c.name AS category_name, " +
                     "r.total_questions, r.correct_answers, r.wrong_answers, r.score, r.percentage, r.taken_at " +
                     "FROM results r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN categories c ON r.category_id = c.id " +
                     "ORDER BY r.score DESC, r.percentage DESC, r.taken_at ASC " +
                     "LIMIT 10";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractResultFromResultSet(rs));
            }
        }
        return list;
    }

    private Result extractResultFromResultSet(ResultSet rs) throws SQLException {
        return new Result(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getInt("category_id"),
            rs.getString("category_name"),
            rs.getInt("total_questions"),
            rs.getInt("correct_answers"),
            rs.getInt("wrong_answers"),
            rs.getInt("score"),
            rs.getDouble("percentage"),
            rs.getTimestamp("taken_at")
        );
    }
}
