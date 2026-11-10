package ui;

import dao.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * RegisterFrame.java
 *
 * Registration window allowing new users to sign up as a Student or Admin.
 * Has strict validation: passwords must match, length checks, and checks
 * duplicate usernames against the MySQL database via UserDAO.
 */
public class RegisterFrame extends JFrame {

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JPasswordField txtConfirmPassword;
    private final JComboBox<String> comboRole;
    private final JButton btnRegister;
    private final JButton btnBackToLogin;

    private final UserDAO userDAO;
    private final JFrame parentLoginFrame;

    public RegisterFrame(JFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;
        this.userDAO = new UserDAO();

        setTitle("Quiz Application - Register");
        setSize(460, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel lblTitle = new JLabel("Create New Account", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(24, 43, 73));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        formPanel.add(txtUsername, gbc);

        // Role Selection
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        comboRole = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        formPanel.add(comboRole, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        txtConfirmPassword = new JPasswordField(15);
        formPanel.add(txtConfirmPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(245, 247, 250));

        btnRegister = new JButton("Register");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(new Color(16, 185, 129));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(110, 35));

        btnBackToLogin = new JButton("Back to Login");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBackToLogin.setFocusPainted(false);
        btnBackToLogin.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBackToLogin);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Handlers
        btnRegister.addActionListener(this::handleRegister);
        btnBackToLogin.addActionListener(e -> returnToLogin());

        // Handle window close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                returnToLogin();
            }
        });
    }

    private void returnToLogin() {
        if (parentLoginFrame != null) {
            parentLoginFrame.setVisible(true);
        } else {
            new LoginFrame().setVisible(true);
        }
        dispose();
    }

    private void handleRegister(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String role = (String) comboRole.getSelectedItem();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();

        // 1. Validations
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnRegister.setEnabled(false);

        // 2. Perform duplicate check & registration in SwingWorker
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (userDAO.usernameExists(username)) {
                    return false;
                }
                return userDAO.register(username, password, role);
            }

            @Override
            protected void done() {
                btnRegister.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Account successfully created! You can now log in.",
                            "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                        returnToLogin();
                    } else {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Username '" + username + "' is already taken. Please choose another.",
                            "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        "Error during registration: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
