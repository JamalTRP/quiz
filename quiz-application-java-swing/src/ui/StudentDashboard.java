package ui;

import dao.QuestionDAO;
import dao.ResultDAO;
import model.Category;
import model.Result;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * StudentDashboard.java
 *
 * The main portal for students.
 * - Choose quiz category and start a timed test
 * - View history of past attempts in a JTable
 * - View Top 10 system-wide leaderboard
 * - Log out
 */
public class StudentDashboard extends JFrame {

    private final User currentUser;
    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;

    private JComboBox<Category> comboCategory;
    private JTable tblHistory;
    private DefaultTableModel historyTableModel;
    private JTable tblLeaderboard;
    private DefaultTableModel leaderboardTableModel;

    public StudentDashboard(User user) {
        this.currentUser = user;
        this.questionDAO = new QuestionDAO();
        this.resultDAO = new ResultDAO();

        setTitle("Student Dashboard - " + user.getUsername());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadCategories();
        loadHistory();
        loadLeaderboard();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Top Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblWelcome = new JLabel("Welcome back, " + currentUser.getUsername() + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(30, 41, 59));

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        headerPanel.add(lblWelcome, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center: Tabbed Pane (1. Start Quiz / History, 2. Leaderboard)
        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Start Quiz & History
        JPanel tabQuiz = new JPanel(new BorderLayout(15, 15));
        tabQuiz.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        // Start Quiz Section
        JPanel startQuizPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        startQuizPanel.setBorder(BorderFactory.createTitledBorder("Take a New Quiz"));

        startQuizPanel.add(new JLabel("Select Category:"));
        comboCategory = new JComboBox<>();
        comboCategory.setPreferredSize(new Dimension(250, 30));
        startQuizPanel.add(comboCategory);

        JButton btnStart = new JButton("Start Quiz");
        btnStart.setBackground(new Color(37, 99, 235));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStart.setPreferredSize(new Dimension(120, 32));
        btnStart.addActionListener(e -> startQuiz());
        startQuizPanel.add(btnStart);

        tabQuiz.add(startQuizPanel, BorderLayout.NORTH);

        // Past Attempts Table Section
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("My Past Quiz Attempts"));

        historyTableModel = new DefaultTableModel(
            new String[]{"ID", "Category", "Total Questions", "Correct", "Wrong", "Score", "Percentage", "Date & Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        tblHistory = new JTable(historyTableModel);
        tblHistory.setRowHeight(24);
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyPanel.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

        JButton btnRefreshHistory = new JButton("Refresh History");
        btnRefreshHistory.addActionListener(e -> loadHistory());
        JPanel histBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        histBtnPanel.add(btnRefreshHistory);
        historyPanel.add(histBtnPanel, BorderLayout.SOUTH);

        tabQuiz.add(historyPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Quiz & My History", tabQuiz);

        // TAB 2: Leaderboard
        JPanel tabLeaderboard = new JPanel(new BorderLayout(10, 10));
        tabLeaderboard.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        leaderboardTableModel = new DefaultTableModel(
            new String[]{"Rank", "Student Username", "Category", "Score", "Percentage", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLeaderboard = new JTable(leaderboardTableModel);
        tblLeaderboard.setRowHeight(26);
        tblLeaderboard.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabLeaderboard.add(new JScrollPane(tblLeaderboard), BorderLayout.CENTER);

        JButton btnRefreshLeaderboard = new JButton("Refresh Leaderboard");
        btnRefreshLeaderboard.addActionListener(e -> loadLeaderboard());
        JPanel leadBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leadBtn.add(btnRefreshLeaderboard);
        tabLeaderboard.add(leadBtn, BorderLayout.SOUTH);

        tabbedPane.addTab("Top 10 Leaderboard", tabLeaderboard);

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    private void loadCategories() {
        SwingWorker<List<Category>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                return questionDAO.getAllCategories();
            }

            @Override
            protected void done() {
                try {
                    List<Category> categories = get();
                    comboCategory.removeAllItems();
                    for (Category cat : categories) {
                        comboCategory.addItem(cat);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Failed to load categories: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void loadHistory() {
        SwingWorker<List<Result>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Result> doInBackground() throws Exception {
                return resultDAO.getResultsByUser(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Result> results = get();
                    historyTableModel.setRowCount(0); // clear table
                    for (Result r : results) {
                        historyTableModel.addRow(new Object[]{
                            r.getId(),
                            r.getCategoryName(),
                            r.getTotalQuestions(),
                            r.getCorrectAnswers(),
                            r.getWrongAnswers(),
                            r.getScore(),
                            String.format("%.1f%%", r.getPercentage()),
                            r.getTakenAt()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Failed to load past attempts: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void loadLeaderboard() {
        SwingWorker<List<Result>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Result> doInBackground() throws Exception {
                return resultDAO.getTop10Leaderboard();
            }

            @Override
            protected void done() {
                try {
                    List<Result> topResults = get();
                    leaderboardTableModel.setRowCount(0);
                    int rank = 1;
                    for (Result r : topResults) {
                        leaderboardTableModel.addRow(new Object[]{
                            "#" + rank++,
                            r.getUsername(),
                            r.getCategoryName(),
                            r.getScore(),
                            String.format("%.1f%%", r.getPercentage()),
                            r.getTakenAt()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Failed to load leaderboard: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void startQuiz() {
        Category selectedCategory = (Category) comboCategory.getSelectedItem();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Please choose a category first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Fetch questions for this category
        SwingWorker<java.util.List<model.Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<model.Question> doInBackground() throws Exception {
                return questionDAO.getQuestionsByCategory(selectedCategory.getId());
            }

            @Override
            protected void done() {
                try {
                    java.util.List<model.Question> questions = get();
                    if (questions.isEmpty()) {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "No questions available in '" + selectedCategory.getName() + "'. Please choose another category.",
                            "Category Empty", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Launch the QuizFrame
                    new QuizFrame(currentUser, selectedCategory, questions, StudentDashboard.this).setVisible(true);
                    setVisible(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Error loading quiz: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
