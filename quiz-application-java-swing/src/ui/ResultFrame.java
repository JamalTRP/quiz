package ui;

import dao.ResultDAO;
import model.Category;
import model.Result;
import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * ResultFrame.java
 *
 * Displays the comprehensive score report for a completed quiz attempt.
 * Automatically saves the result to the MySQL database via ResultDAO.
 * Provides a button to return to the Student Dashboard.
 */
public class ResultFrame extends JFrame {

    private final User user;
    private final Category category;
    private final int totalQuestions;
    private final int correctAnswers;
    private final int wrongAnswers;
    private final int score;
    private final double percentage;
    private final StudentDashboard parentDashboard;

    private final ResultDAO resultDAO;

    public ResultFrame(User user, Category category, int totalQuestions,
                       int correctAnswers, int wrongAnswers, int score,
                       double percentage, StudentDashboard parentDashboard) {
        this.user = user;
        this.category = category;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.score = score;
        this.percentage = percentage;
        this.parentDashboard = parentDashboard;
        this.resultDAO = new ResultDAO();

        setTitle("Quiz Results - " + category.getName());
        setSize(480, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        saveResultToDatabase();

        // Return to dashboard when closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToDashboard();
            }
        });
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(new Color(248, 250, 252));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Quiz Completed!", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JLabel lblCategory = new JLabel("Category: " + category.getName(), SwingConstants.CENTER);
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCategory.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblTitle);
        headerPanel.add(lblCategory);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Stats Card
        JPanel cardPanel = new JPanel(new GridLayout(6, 2, 12, 10));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        addStatRow(cardPanel, "Candidate Name:", user.getUsername(), false);
        addStatRow(cardPanel, "Total Questions:", String.valueOf(totalQuestions), false);
        addStatRow(cardPanel, "Correct Answers:", String.valueOf(correctAnswers), true);
        addStatRow(cardPanel, "Wrong / Skipped:", String.valueOf(wrongAnswers), false);
        addStatRow(cardPanel, "Total Score:", score + " pts", true);
        addStatRow(cardPanel, "Percentage:", String.format("%.1f%%", percentage), true);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Bottom Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(248, 250, 252));

        JButton btnDone = new JButton("Back to Dashboard");
        btnDone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDone.setBackground(new Color(37, 99, 235));
        btnDone.setForeground(Color.WHITE);
        btnDone.setPreferredSize(new Dimension(170, 36));
        btnDone.addActionListener(e -> returnToDashboard());

        bottomPanel.add(btnDone);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addStatRow(JPanel panel, String label, String value, boolean highlight) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(71, 85, 105));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", highlight ? Font.BOLD : Font.PLAIN, 14));
        if (highlight) {
            val.setForeground(new Color(16, 185, 129));
        } else {
            val.setForeground(new Color(15, 23, 42));
        }

        panel.add(lbl);
        panel.add(val);
    }

    private void saveResultToDatabase() {
        Result r = new Result();
        r.setUserId(user.getId());
        r.setCategoryId(category.getId());
        r.setTotalQuestions(totalQuestions);
        r.setCorrectAnswers(correctAnswers);
        r.setWrongAnswers(wrongAnswers);
        r.setScore(score);
        r.setPercentage(percentage);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return resultDAO.saveResult(r);
            }

            @Override
            protected void done() {
                try {
                    boolean saved = get();
                    if (!saved) {
                        System.err.println("Warning: Result could not be inserted into the database.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ResultFrame.this,
                        "Warning: Failed to save result to database: " + ex.getMessage(),
                        "Database Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void returnToDashboard() {
        if (parentDashboard != null) {
            parentDashboard.loadHistory();
            parentDashboard.loadLeaderboard();
            parentDashboard.setVisible(true);
        }
        dispose();
    }
}
