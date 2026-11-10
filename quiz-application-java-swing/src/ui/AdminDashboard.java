package ui;

import dao.QuestionDAO;
import dao.ResultDAO;
import model.Category;
import model.Question;
import model.Result;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * AdminDashboard.java
 *
 * Full administrative control panel:
 * 1. Question Management:
 *    - Add new question with 4 options and correct answer
 *    - Edit selected question
 *    - Delete selected question
 *    - Search and browse all questions in a JTable
 * 2. Category Management:
 *    - Add new categories (e.g. Aptitude, Python, Cloud)
 * 3. Student Scores Overview:
 *    - Real-time JTable showing all quiz attempts submitted by students
 */
public class AdminDashboard extends JFrame {

    private final User currentUser;
    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;

    // UI - Questions Table & Form
    private JTable tblQuestions;
    private DefaultTableModel questionsTableModel;
    private List<Question> currentQuestionList;

    private JComboBox<Category> comboQuestionCategory;
    private JTextArea txtQuestionPrompt;
    private JTextField txtOptionA;
    private JTextField txtOptionB;
    private JTextField txtOptionC;
    private JTextField txtOptionD;
    private JComboBox<String> comboCorrectOption;
    private JButton btnSaveQuestion;
    private JButton btnClearForm;
    private JButton btnDeleteQuestion;

    private int selectedQuestionId = -1; // -1 indicates "Add Mode", >0 indicates "Edit Mode"

    // UI - Categories
    private JTextField txtCategoryName;
    private JTextField txtCategoryDesc;
    private JButton btnAddCategory;

    // UI - Results Table
    private JTable tblAllResults;
    private DefaultTableModel resultsTableModel;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.questionDAO = new QuestionDAO();
        this.resultDAO = new ResultDAO();

        setTitle("Admin Dashboard - " + user.getUsername());
        setSize(1000, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadCategories();
        loadQuestions();
        loadAllResults();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Quiz Master - Administrator Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));

        JButton btnLogout = new JButton("Logout (" + currentUser.getUsername() + ")");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Center Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Questions Management
        tabbedPane.addTab("Manage Questions", createQuestionsTab());

        // TAB 2: Categories Management
        tabbedPane.addTab("Manage Categories", createCategoriesTab());

        // TAB 3: Student Results
        tabbedPane.addTab("All Student Scores", createResultsTab());

        root.add(tabbedPane, BorderLayout.CENTER);
        add(root);
    }

    private JPanel createQuestionsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top / Center: Questions Table
        questionsTableModel = new DefaultTableModel(
            new String[]{"ID", "Category", "Question Text", "Option A", "Option B", "Option C", "Option D", "Correct"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblQuestions = new JTable(questionsTableModel);
        tblQuestions.setRowHeight(24);
        tblQuestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblQuestions.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblQuestions);
        scrollTable.setPreferredSize(new Dimension(900, 240));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Bottom Form for Add/Edit
        JPanel formContainer = new JPanel(new BorderLayout(5, 5));
        formContainer.setBorder(BorderFactory.createTitledBorder("Question Details (Add / Edit)"));

        JPanel formGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Category & Correct Option
        gbc.gridx = 0; gbc.gridy = 0;
        formGrid.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        comboQuestionCategory = new JComboBox<>();
        comboQuestionCategory.setPreferredSize(new Dimension(200, 26));
        formGrid.add(comboQuestionCategory, gbc);

        gbc.gridx = 2;
        formGrid.add(new JLabel("Correct Option:"), gbc);
        gbc.gridx = 3;
        comboCorrectOption = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        formGrid.add(comboCorrectOption, gbc);

        // Row 1: Question Text
        gbc.gridx = 0; gbc.gridy = 1;
        formGrid.add(new JLabel("Question:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtQuestionPrompt = new JTextArea(3, 40);
        txtQuestionPrompt.setLineWrap(true);
        txtQuestionPrompt.setWrapStyleWord(true);
        formGrid.add(new JScrollPane(txtQuestionPrompt), gbc);

        // Row 2: Option A & Option B
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2;
        formGrid.add(new JLabel("Option A:"), gbc);
        gbc.gridx = 1;
        txtOptionA = new JTextField(20);
        formGrid.add(txtOptionA, gbc);

        gbc.gridx = 2;
        formGrid.add(new JLabel("Option B:"), gbc);
        gbc.gridx = 3;
        txtOptionB = new JTextField(20);
        formGrid.add(txtOptionB, gbc);

        // Row 3: Option C & Option D
        gbc.gridx = 0; gbc.gridy = 3;
        formGrid.add(new JLabel("Option C:"), gbc);
        gbc.gridx = 1;
        txtOptionC = new JTextField(20);
        formGrid.add(txtOptionC, gbc);

        gbc.gridx = 2;
        formGrid.add(new JLabel("Option D:"), gbc);
        gbc.gridx = 3;
        txtOptionD = new JTextField(20);
        formGrid.add(txtOptionD, gbc);

        formContainer.add(formGrid, BorderLayout.CENTER);

        // Form action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        btnSaveQuestion = new JButton("Add Question");
        btnSaveQuestion.setBackground(new Color(37, 99, 235));
        btnSaveQuestion.setForeground(Color.WHITE);
        btnSaveQuestion.addActionListener(this::handleSaveQuestion);

        btnClearForm = new JButton("Clear / New");
        btnClearForm.addActionListener(e -> clearQuestionForm());

        btnDeleteQuestion = new JButton("Delete Selected");
        btnDeleteQuestion.setBackground(new Color(239, 68, 68));
        btnDeleteQuestion.setForeground(Color.WHITE);
        btnDeleteQuestion.setEnabled(false);
        btnDeleteQuestion.addActionListener(this::handleDeleteQuestion);

        actionPanel.add(btnDeleteQuestion);
        actionPanel.add(btnClearForm);
        actionPanel.add(btnSaveQuestion);

        formContainer.add(actionPanel, BorderLayout.SOUTH);
        panel.add(formContainer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCategoriesTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel addCatCard = new JPanel(new GridBagLayout());
        addCatCard.setBorder(BorderFactory.createTitledBorder("Add New Category"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        addCatCard.add(new JLabel("Category Name:"), gbc);
        gbc.gridx = 1;
        txtCategoryName = new JTextField(25);
        addCatCard.add(txtCategoryName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addCatCard.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        txtCategoryDesc = new JTextField(25);
        addCatCard.add(txtCategoryDesc, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        btnAddCategory = new JButton("Save Category");
        btnAddCategory.setBackground(new Color(16, 185, 129));
        btnAddCategory.setForeground(Color.WHITE);
        btnAddCategory.addActionListener(this::handleAddCategory);
        addCatCard.add(btnAddCategory, gbc);

        panel.add(addCatCard, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createResultsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultsTableModel = new DefaultTableModel(
            new String[]{"Result ID", "Student Username", "Category", "Total Qs", "Correct", "Wrong", "Score", "Percentage", "Date & Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblAllResults = new JTable(resultsTableModel);
        tblAllResults.setRowHeight(24);
        panel.add(new JScrollPane(tblAllResults), BorderLayout.CENTER);

        JButton btnRefreshResults = new JButton("Refresh Scores");
        btnRefreshResults.addActionListener(e -> loadAllResults());
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.add(btnRefreshResults);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
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
                    List<Category> list = get();
                    comboQuestionCategory.removeAllItems();
                    for (Category c : list) {
                        comboQuestionCategory.addItem(c);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Failed to load categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadQuestions() {
        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() throws Exception {
                return questionDAO.getAllQuestionsWithCategory();
            }

            @Override
            protected void done() {
                try {
                    currentQuestionList = get();
                    questionsTableModel.setRowCount(0);
                    for (Question q : currentQuestionList) {
                        questionsTableModel.addRow(new Object[]{
                            q.getId(),
                            q.getCategoryName(),
                            q.getQuestionText(),
                            q.getOptionA(),
                            q.getOptionB(),
                            q.getOptionC(),
                            q.getOptionD(),
                            q.getCorrectOption()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Failed to load questions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadAllResults() {
        SwingWorker<List<Result>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Result> doInBackground() throws Exception {
                return resultDAO.getAllResults();
            }

            @Override
            protected void done() {
                try {
                    List<Result> results = get();
                    resultsTableModel.setRowCount(0);
                    for (Result r : results) {
                        resultsTableModel.addRow(new Object[]{
                            r.getId(),
                            r.getUsername(),
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
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Failed to load student scores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = tblQuestions.getSelectedRow();
        if (selectedRow >= 0 && currentQuestionList != null && selectedRow < currentQuestionList.size()) {
            Question q = currentQuestionList.get(selectedRow);
            selectedQuestionId = q.getId();

            txtQuestionPrompt.setText(q.getQuestionText());
            txtOptionA.setText(q.getOptionA());
            txtOptionB.setText(q.getOptionB());
            txtOptionC.setText(q.getOptionC());
            txtOptionD.setText(q.getOptionD());
            comboCorrectOption.setSelectedItem(String.valueOf(q.getCorrectOption()));

            // Match Category in ComboBox
            for (int i = 0; i < comboQuestionCategory.getItemCount(); i++) {
                Category c = comboQuestionCategory.getItemAt(i);
                if (c.getId() == q.getCategoryId()) {
                    comboQuestionCategory.setSelectedIndex(i);
                    break;
                }
            }

            btnSaveQuestion.setText("Update Question");
            btnDeleteQuestion.setEnabled(true);
        }
    }

    private void clearQuestionForm() {
        selectedQuestionId = -1;
        txtQuestionPrompt.setText("");
        txtOptionA.setText("");
        txtOptionB.setText("");
        txtOptionC.setText("");
        txtOptionD.setText("");
        comboCorrectOption.setSelectedIndex(0);
        btnSaveQuestion.setText("Add Question");
        btnDeleteQuestion.setEnabled(false);
        tblQuestions.clearSelection();
    }

    private void handleSaveQuestion(ActionEvent e) {
        Category cat = (Category) comboQuestionCategory.getSelectedItem();
        String prompt = txtQuestionPrompt.getText().trim();
        String a = txtOptionA.getText().trim();
        String b = txtOptionB.getText().trim();
        String c = txtOptionC.getText().trim();
        String d = txtOptionD.getText().trim();
        String correctStr = (String) comboCorrectOption.getSelectedItem();

        if (cat == null || prompt.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty() || correctStr == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all question fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Question q = new Question(
            selectedQuestionId,
            cat.getId(),
            prompt,
            a, b, c, d,
            correctStr.charAt(0)
        );

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (selectedQuestionId > 0) {
                    return questionDAO.updateQuestion(q);
                } else {
                    return questionDAO.addQuestion(q);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            (selectedQuestionId > 0 ? "Question updated!" : "Question added!"),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearQuestionForm();
                        loadQuestions();
                    } else {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to save question.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void handleDeleteQuestion(ActionEvent e) {
        if (selectedQuestionId <= 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this question?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return questionDAO.deleteQuestion(selectedQuestionId);
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(AdminDashboard.this, "Question deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                            clearQuestionForm();
                            loadQuestions();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void handleAddCategory(ActionEvent e) {
        String name = txtCategoryName.getText().trim();
        String desc = txtCategoryDesc.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return questionDAO.addCategory(name, desc);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Category '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        txtCategoryName.setText("");
                        txtCategoryDesc.setText("");
                        loadCategories();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to add category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
