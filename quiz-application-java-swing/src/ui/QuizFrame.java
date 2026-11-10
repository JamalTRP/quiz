package ui;

import model.Category;
import model.Question;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuizFrame.java
 *
 * Manages the live quiz session:
 * - One question at a time
 * - 4 options with JRadioButton grouped inside a ButtonGroup
 * - Next & Previous buttons
 * - 30-second countdown timer per question (auto-moves to next question on timeout)
 * - Remembers user's selected answers during navigation
 * - Automatically transitions to ResultFrame upon completion
 */
public class QuizFrame extends JFrame {

    private static final int TIME_PER_QUESTION_SEC = 30;

    private final User user;
    private final Category category;
    private final List<Question> questions;
    private final StudentDashboard parentDashboard;

    // Track user's chosen option for each question index (0 -> 'A', 1 -> 'C', etc.)
    private final Map<Integer, Character> userAnswers;

    private int currentIndex = 0;
    private int remainingSeconds = TIME_PER_QUESTION_SEC;
    private Timer countdownTimer;

    // UI components
    private JLabel lblQuestionNum;
    private JLabel lblTimer;
    private JProgressBar timerProgress;
    private JTextArea txtQuestion;
    private JRadioButton rbOptionA;
    private JRadioButton rbOptionB;
    private JRadioButton rbOptionC;
    private JRadioButton rbOptionD;
    private ButtonGroup optionGroup;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnSubmit;

    public QuizFrame(User user, Category category, List<Question> questions, StudentDashboard parentDashboard) {
        this.user = user;
        this.category = category;
        this.questions = questions;
        this.parentDashboard = parentDashboard;
        this.userAnswers = new HashMap<>();

        setTitle("Quiz - " + category.getName());
        setSize(780, 520);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Confirm before quitting
        setLocationRelativeTo(null);

        initUI();
        displayQuestion(0);
        startTimer();

        // Prompt before closing midway
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(QuizFrame.this,
                    "Are you sure you want to quit? Your current quiz progress will be lost.",
                    "Confirm Quit", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    if (countdownTimer != null) countdownTimer.stop();
                    parentDashboard.setVisible(true);
                    dispose();
                }
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        root.setBackground(new Color(248, 250, 252));

        // Top Bar: Question Index & Countdown Timer
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 250, 252));

        lblQuestionNum = new JLabel("Question 1 of " + questions.size());
        lblQuestionNum.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblQuestionNum.setForeground(new Color(30, 41, 59));
        topPanel.add(lblQuestionNum, BorderLayout.WEST);

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        timerPanel.setBackground(new Color(248, 250, 252));

        lblTimer = new JLabel("Time: 30s");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTimer.setForeground(new Color(220, 38, 38)); // Red color for urgency

        timerProgress = new JProgressBar(0, TIME_PER_QUESTION_SEC);
        timerProgress.setValue(TIME_PER_QUESTION_SEC);
        timerProgress.setPreferredSize(new Dimension(100, 18));
        timerProgress.setForeground(new Color(220, 38, 38));

        timerPanel.add(lblTimer);
        timerPanel.add(timerProgress);
        topPanel.add(timerPanel, BorderLayout.EAST);

        root.add(topPanel, BorderLayout.NORTH);

        // Center Panel: Question text & 4 options
        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setBackground(new Color(248, 250, 252));

        txtQuestion = new JTextArea();
        txtQuestion.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setEditable(false);
        txtQuestion.setBackground(Color.WHITE);
        txtQuestion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        centerPanel.add(txtQuestion, BorderLayout.NORTH);

        // Options Radio Buttons Panel
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBackground(new Color(248, 250, 252));

        rbOptionA = createOptionRadio("A");
        rbOptionB = createOptionRadio("B");
        rbOptionC = createOptionRadio("C");
        rbOptionD = createOptionRadio("D");

        optionGroup = new ButtonGroup();
        optionGroup.add(rbOptionA);
        optionGroup.add(rbOptionB);
        optionGroup.add(rbOptionC);
        optionGroup.add(rbOptionD);

        optionsPanel.add(rbOptionA);
        optionsPanel.add(rbOptionB);
        optionsPanel.add(rbOptionC);
        optionsPanel.add(rbOptionD);

        centerPanel.add(optionsPanel, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        // Bottom Navigation Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 250, 252));

        btnPrev = new JButton("<< Previous");
        btnPrev.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnPrev.addActionListener(e -> navigatePrevious());
        bottomPanel.add(btnPrev, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setBackground(new Color(248, 250, 252));

        btnNext = new JButton("Next >>");
        btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnNext.addActionListener(e -> navigateNext());
        rightButtons.add(btnNext);

        btnSubmit = new JButton("Submit Quiz");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(new Color(16, 185, 129));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.addActionListener(e -> confirmAndSubmitQuiz());
        rightButtons.add(btnSubmit);

        bottomPanel.add(rightButtons, BorderLayout.EAST);
        root.add(bottomPanel, BorderLayout.SOUTH);

        add(root);
    }

    private JRadioButton createOptionRadio(String letter) {
        JRadioButton rb = new JRadioButton();
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rb.setBackground(Color.WHITE);
        rb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        rb.setBorderPainted(true);
        rb.setFocusPainted(false);
        rb.setActionCommand(letter);
        return rb;
    }

    private void displayQuestion(int index) {
        saveCurrentSelection();

        currentIndex = index;
        Question q = questions.get(index);

        lblQuestionNum.setText("Question " + (index + 1) + " of " + questions.size());
        txtQuestion.setText(q.getQuestionText());

        rbOptionA.setText("A)  " + q.getOptionA());
        rbOptionB.setText("B)  " + q.getOptionB());
        rbOptionC.setText("C)  " + q.getOptionC());
        rbOptionD.setText("D)  " + q.getOptionD());

        // Restore previously selected option if any
        optionGroup.clearSelection();
        if (userAnswers.containsKey(index)) {
            char chosen = userAnswers.get(index);
            if (chosen == 'A') rbOptionA.setSelected(true);
            else if (chosen == 'B') rbOptionB.setSelected(true);
            else if (chosen == 'C') rbOptionC.setSelected(true);
            else if (chosen == 'D') rbOptionD.setSelected(true);
        }

        // Configure button enabled states
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < questions.size() - 1);

        // Reset question timer
        remainingSeconds = TIME_PER_QUESTION_SEC;
        updateTimerDisplay();
    }

    private void saveCurrentSelection() {
        ButtonModel selected = optionGroup.getSelection();
        if (selected != null) {
            userAnswers.put(currentIndex, selected.getActionCommand().charAt(0));
        }
    }

    private void navigateNext() {
        saveCurrentSelection();
        if (currentIndex < questions.size() - 1) {
            displayQuestion(currentIndex + 1);
        }
    }

    private void navigatePrevious() {
        saveCurrentSelection();
        if (currentIndex > 0) {
            displayQuestion(currentIndex - 1);
        }
    }

    private void startTimer() {
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingSeconds--;
                updateTimerDisplay();

                if (remainingSeconds <= 0) {
                    // Time is up for current question: auto move to next
                    saveCurrentSelection();
                    if (currentIndex < questions.size() - 1) {
                        displayQuestion(currentIndex + 1);
                    } else {
                        // Reached last question timeout: auto-submit quiz
                        countdownTimer.stop();
                        JOptionPane.showMessageDialog(QuizFrame.this,
                            "Time is completely up! Submitting your answers now.",
                            "Time Expired", JOptionPane.INFORMATION_MESSAGE);
                        finishAndOpenResult();
                    }
                }
            }
        });
        countdownTimer.start();
    }

    private void updateTimerDisplay() {
        lblTimer.setText("Time: " + remainingSeconds + "s");
        timerProgress.setValue(remainingSeconds);

        if (remainingSeconds <= 5) {
            lblTimer.setForeground(new Color(239, 68, 68)); // bright red warning
        } else {
            lblTimer.setForeground(new Color(185, 28, 28));
        }
    }

    private void confirmAndSubmitQuiz() {
        saveCurrentSelection();
        int unanswered = questions.size() - userAnswers.size();
        String message = "Are you ready to submit your quiz?";
        if (unanswered > 0) {
            message += "\nYou have " + unanswered + " unanswered question(s).";
        }

        int choice = JOptionPane.showConfirmDialog(this, message, "Confirm Submission", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (countdownTimer != null) countdownTimer.stop();
            finishAndOpenResult();
        }
    }

    private void finishAndOpenResult() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        saveCurrentSelection();

        // Calculate score
        int correctCount = 0;
        int wrongCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            Character chosen = userAnswers.get(i);
            if (chosen != null && chosen == q.getCorrectOption()) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }

        int total = questions.size();
        // Assuming 10 points per question
        int score = correctCount * 10;
        double percentage = ((double) correctCount / total) * 100.0;

        // Open ResultFrame
        new ResultFrame(user, category, total, correctCount, wrongCount, score, percentage, parentDashboard).setVisible(true);
        dispose();
    }
}
