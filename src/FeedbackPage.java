import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class FeedbackPage extends JFrame {
    private JTextArea feedbackArea;
    private JButton submitBtn, skipBtn, dashboardBtn;
    private String username;
    private String driverName;

    // Database Config
    private final String DB_URL = "jdbc:mysql://localhost:3306/rideflow_DB";
    private final String DB_USER = "root";
    private final String DB_PASS = "2D905Bdad";

    public FeedbackPage(String username, String driverName) {
        this.username = username;
        this.driverName = driverName;

        setTitle("Ride Feedback - RideFlow");
        setSize(550, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(32, 46, 55));

        // Header Section
        JLabel titleLabel = new JLabel("How was your ride?");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Share your experience with driver " + driverName);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(25));

        // Star Rating Section (Visual only)
        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        starPanel.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            JLabel star = new JLabel("⭐");
            star.setFont(new Font("Segoe UI", Font.PLAIN, 32));
            star.setCursor(new Cursor(Cursor.HAND_CURSOR));
            starPanel.add(star);
        }
        mainPanel.add(starPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Feedback Text Area
        JLabel feedbackLabel = new JLabel("Tell us more (optional):");
        feedbackLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(feedbackLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        feedbackArea = new JTextArea(8, 40);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        feedbackArea.setBackground(new Color(50, 60, 70));
        feedbackArea.setForeground(Color.WHITE);
        feedbackArea.setCaretColor(Color.WHITE);
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 120, 140), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(25));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        submitBtn = new JButton("Submit Feedback");
        submitBtn.setBackground(new Color(26, 188, 156));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setFocusPainted(false);
        submitBtn.setPreferredSize(new Dimension(160, 45));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> submitFeedback());

        skipBtn = new JButton("Skip");
        skipBtn.setBackground(new Color(149, 165, 166));
        skipBtn.setForeground(Color.WHITE);
        skipBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        skipBtn.setFocusPainted(false);
        skipBtn.setPreferredSize(new Dimension(100, 45));
        skipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> skipFeedback());

        dashboardBtn = new JButton("Go to Dashboard");
        dashboardBtn.setBackground(new Color(52, 152, 219));
        dashboardBtn.setForeground(Color.WHITE);
        dashboardBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dashboardBtn.setFocusPainted(false);
        dashboardBtn.setPreferredSize(new Dimension(160, 45));
        dashboardBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dashboardBtn.addActionListener(e -> goToDashboard());

        buttonPanel.add(submitBtn);
        buttonPanel.add(skipBtn);
        mainPanel.add(buttonPanel);

        mainPanel.add(Box.createVerticalStrut(15));

        // Dashboard button in separate panel for better layout
        JPanel dashboardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dashboardPanel.setOpaque(false);
        dashboardPanel.add(dashboardBtn);
        mainPanel.add(dashboardPanel);

        add(mainPanel);
        setVisible(true);
    }

    private void submitFeedback() {
        String feedback = feedbackArea.getText().trim();

        if (feedback.isEmpty()) {
            int response = JOptionPane.showConfirmDialog(this,
                    "No feedback entered. Submit anyway?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
            feedback = "No written feedback provided";
        }

        // Save feedback to database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO feedbacks (username, feedback, driver_name, feedback_time) VALUES (?, ?, ?, NOW())")) {

            pstmt.setString(1, username);
            pstmt.setString(2, feedback);
            pstmt.setString(3, driverName);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Thank you for your feedback!\nYour input helps us improve.",
                    "Feedback Submitted",
                    JOptionPane.INFORMATION_MESSAGE);

            // Go to dashboard after submitting
            goToDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback to file if database fails
            saveFeedbackToFile(feedback);
            JOptionPane.showMessageDialog(this,
                    "Thank you for your feedback!",
                    "Feedback Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
            goToDashboard();
        }
    }

    private void saveFeedbackToFile(String feedback) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("feedbacks.txt", true))) {
            bw.write("Username: " + username + " | Driver: " + driverName);
            bw.newLine();
            bw.write(feedback);
            bw.newLine();
            bw.write("---");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void skipFeedback() {
        int response = JOptionPane.showConfirmDialog(this,
                "Skip feedback? Your input helps us improve service.",
                "Skip Feedback",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            goToDashboard();
        }
    }

    private void goToDashboard() {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            new PassengerDashboard(username);
        });
    }
}