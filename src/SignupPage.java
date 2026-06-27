import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupPage extends JFrame {
    private JComboBox<String> roleComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField licenseNumberField;
    private JComboBox<String> vehicleTypeComboBox;
    private JPanel formPanel;

    public SignupPage() {
        setTitle("RideFlow - Sign Up");
        setSize(550, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Main background panel
        JPanel mainBg = new JPanel(new BorderLayout());
        mainBg.setBackground(new Color(15, 25, 50));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(15, 25, 50));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        mainBg.add(headerPanel, BorderLayout.NORTH);

        // Scrollable form panel
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(25, 45, 85));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Role selection
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(roleLabel);
        formPanel.add(Box.createVerticalStrut(8));

        roleComboBox = new JComboBox<>(new String[]{"Passenger", "Driver"});
        roleComboBox.setBackground(new Color(30, 50, 100));
        roleComboBox.setForeground(Color.WHITE);
        roleComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        roleComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleComboBox.addActionListener(e -> updateFormFields());
        formPanel.add(roleComboBox);
        formPanel.add(Box.createVerticalStrut(15));

        // Username
        formPanel.add(createLabeledField("Username:", usernameField = new JTextField()));

        // Email
        formPanel.add(createLabeledField("Email:", emailField = new JTextField()));

        // Phone
        formPanel.add(createLabeledField("Phone:", phoneField = new JTextField()));

        // Password
        formPanel.add(createLabeledField("Password:", passwordField = new JPasswordField()));

        // Confirm Password
        formPanel.add(createLabeledField("Confirm Password:", confirmPasswordField = new JPasswordField()));

        // Driver-specific fields
        JLabel licenseLabel = new JLabel("License Number:");
        licenseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        licenseLabel.setForeground(Color.WHITE);
        licenseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        licenseNumberField = new JTextField();
        licenseNumberField.setBackground(new Color(30, 50, 100));
        licenseNumberField.setForeground(Color.WHITE);
        licenseNumberField.setCaretColor(Color.WHITE);
        licenseNumberField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        licenseNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);
        licenseNumberField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel vehicleLabel = new JLabel("Vehicle Type:");
        vehicleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vehicleLabel.setForeground(Color.WHITE);
        vehicleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        vehicleTypeComboBox = new JComboBox<>(new String[]{"Car without AC", "Car with AC", "Premium", "Bike"});
        vehicleTypeComboBox.setBackground(new Color(30, 50, 100));
        vehicleTypeComboBox.setForeground(Color.WHITE);
        vehicleTypeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        vehicleTypeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add driver fields (initially hidden)
        formPanel.add(licenseLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(licenseNumberField);
        formPanel.add(Box.createVerticalStrut(15));

        formPanel.add(vehicleLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(vehicleTypeComboBox);
        formPanel.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(new Color(25, 45, 85));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(new Color(76, 175, 80));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupButton.setPreferredSize(new Dimension(140, 40));
        signupButton.setMaximumSize(new Dimension(140, 40));
        signupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupButton.setFocusPainted(false);
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });
        buttonPanel.add(signupButton);
        buttonPanel.add(Box.createHorizontalStrut(15));

        JButton backButton = new JButton("Back to Login");
        backButton.setBackground(new Color(100, 180, 220));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setPreferredSize(new Dimension(140, 40));
        backButton.setMaximumSize(new Dimension(140, 40));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setFocusPainted(false);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginPage();
                dispose();
            }
        });
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalGlue());

        formPanel.add(buttonPanel);
        formPanel.add(Box.createVerticalGlue());

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainBg.add(scrollPane, BorderLayout.CENTER);
        add(mainBg);

        updateFormFields();
        setVisible(true);
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 45, 85));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jLabel.setForeground(Color.WHITE);
        jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(jLabel);
        panel.add(Box.createVerticalStrut(8));

        field.setBackground(new Color(30, 50, 100));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panel.add(field);

        return panel;
    }

    private void updateFormFields() {
        String selectedRole = (String) roleComboBox.getSelectedItem();
        Component[] components = formPanel.getComponents();

        // Find and toggle visibility of driver specific fields
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];

            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();

                if (text.equals("License Number:")) {
                    label.setVisible(selectedRole.equals("Driver"));
                    components[i + 1].setVisible(selectedRole.equals("Driver"));
                    components[i + 2].setVisible(selectedRole.equals("Driver"));
                } else if (text.equals("Vehicle Type:")) {
                    label.setVisible(selectedRole.equals("Driver"));
                    components[i + 1].setVisible(selectedRole.equals("Driver"));
                    components[i + 2].setVisible(selectedRole.equals("Driver"));
                }
            }
        }

        formPanel.revalidate();
        formPanel.repaint();
    }

    // Keep UI code same...
// REPLACE handleSignup with this:
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        boolean success = false;
        if (role.equals("Passenger")) {
            success = UserDatabase.registerPassenger(username, password, email, phoneField.getText());
        } else {
            success = UserDatabase.registerDriver(username, password, email, phoneField.getText(),
                    licenseNumberField.getText(), (String) vehicleTypeComboBox.getSelectedItem());
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Account Created!");
            dispose();
            new LoginPage();
        } else {
            JOptionPane.showMessageDialog(this, "Error: Username might be taken.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignupPage());
    }
}