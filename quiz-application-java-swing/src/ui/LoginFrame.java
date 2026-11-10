package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * LoginFrame.java
 *
 * Provides a clean, modern Swing login screen.
 * Follows MVC: contains no SQL logic. Uses SwingWorker to perform
 * database authentication in the background without freezing the UI.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;
    private final JButton btnRegister;
    private final JLabel lblStatus;

    private final UserDAO userDAO;

    public LoginFrame() {
        this.userDAO = new UserDAO();

        setTitle("Quiz Application - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // Main background panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(new Color(245, 247, 250));

        // Header
        JLabel lblHeader = new JLabel("Welcome to Quiz Master", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(new Color(24, 43, 73));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Form fields layout using GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(txtPassword, gbc);

        // Status Label (displays error / loading info)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(new Color(200, 30, 30));
        formPanel.add(lblStatus, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(245, 247, 250));

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(110, 35));

        btnRegister = new JButton("Create Account");
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(130, 35));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event Listeners
        btnLogin.addActionListener(this::handleLogin);
        btnRegister.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });

        // Pressing Enter in password field triggers login
        txtPassword.addActionListener(this::handleLogin);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // 1. Validation
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Disable buttons and show loading state
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        lblStatus.setText("Authenticating with MySQL database...");
        lblStatus.setForeground(new Color(50, 100, 180));

        // 3. Run database call on background thread (SwingWorker)
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.login(username, password);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
                lblStatus.setText(" ");

                try {
                    User user = get();
                    if (user != null) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Route to appropriate dashboard based on role
                        if (user.isAdmin()) {
                            new AdminDashboard(user).setVisible(true);
                        } else {
                            new StudentDashboard(user).setVisible(true);
                        }
                        dispose(); // Close login window
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Invalid username or password. Please try again.",
                            "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Database Connection Error: " + ex.getMessage() +
                        "\n\nPlease verify that MySQL is running and DBConnection credentials are correct.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
