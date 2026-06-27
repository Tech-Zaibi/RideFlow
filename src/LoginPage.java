import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginPage extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JButton btnSignup;

    public LoginPage() {
        setTitle("RideFlow - Login");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(25, 45, 85));
        add(bg);

        JPanel card = new JPanel(null);
        card.setPreferredSize(new Dimension(330, 380));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("RideFlow");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setHorizontalAlignment(JLabel.CENTER);
        lblTitle.setForeground(new Color(25, 45, 85));
        lblTitle.setBounds(30, 10, 260, 50);

        // Title fade-in animation
        fadeInComponent(lblTitle);

        JLabel lblSubtitle = new JLabel("Welcome Back!");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setHorizontalAlignment(JLabel.CENTER);
        lblSubtitle.setForeground(new Color(100, 100, 100));
        lblSubtitle.setBounds(30, 55, 260, 25);

        // Subtitle fade-in animation
        fadeInComponent(lblSubtitle);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(new Color(60, 60, 60));
        lblUser.setBounds(30, 100, 200, 20);

        txtUser = new JTextField();
        txtUser.setBounds(30, 125, 260, 35);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Text field focus animation
        addTextFieldAnimation(txtUser);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setForeground(new Color(60, 60, 60));
        lblPass.setBounds(30, 175, 200, 20);

        txtPass = new JPasswordField();
        txtPass.setBounds(30, 200, 260, 35);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Text field focus animation
        addTextFieldAnimation(txtPass);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(30, 260, 260, 40);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setBackground(new Color(66, 103, 178));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorder(BorderFactory.createEmptyBorder());
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFocusPainted(false);

        // Button hover animation
        addButtonAnimation(btnLogin, new Color(66, 103, 178), new Color(50, 85, 160));

        btnLogin.addActionListener(e -> handleLogin());

        btnSignup = new JButton("Create Account");
        btnSignup.setBounds(30, 310, 260, 35);
        btnSignup.setBackground(new Color(35, 60, 110));
        btnSignup.setForeground(Color.WHITE);
        btnSignup.setBorder(BorderFactory.createEmptyBorder());
        btnSignup.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSignup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignup.setFocusPainted(false);

        // Button hover animation
        addButtonAnimation(btnSignup, new Color(35, 60, 110), new Color(20, 40, 90));

        btnSignup.addActionListener(e -> {
            dispose();
            new SignupPage();
        });

        card.add(lblTitle);
        card.add(lblSubtitle);
        card.add(lblUser);
        card.add(txtUser);
        card.add(lblPass);
        card.add(txtPass);
        card.add(btnLogin);
        card.add(btnSignup);

        bg.add(card);
        setVisible(true);
    }

    // Fade-in animation for components
    private void fadeInComponent(JComponent component) {
        Timer timer = new Timer(20, null);
        timer.setInitialDelay(100);
        float[] alpha = {0f};

        timer.addActionListener(e -> {
            alpha[0] += 0.05f;
            if (alpha[0] >= 1f) {
                alpha[0] = 1f;
                ((Timer) e.getSource()).stop();
            }
            component.setOpaque(false);
            component.repaint();
        });
        timer.start();
    }

    // Text field focus animation
    private void addTextFieldAnimation(JTextField textField) {
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                animateTextFieldBorder(textField, true);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                animateTextFieldBorder(textField, false);
            }
        });
    }

    // Animate text field border on focus
    private void animateTextFieldBorder(JTextField textField, boolean focused) {
        Color targetColor = focused ? new Color(66, 103, 178) : new Color(200, 200, 200);
        int[] thickness = {1};

        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            thickness[0] += focused ? 1 : -1;
            thickness[0] = Math.max(1, Math.min(3, thickness[0]));

            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(targetColor, thickness[0]),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            if (thickness[0] == (focused ? 3 : 1)) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    // Button hover animation
    private void addButtonAnimation(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateButtonColor(button, normalColor, hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateButtonColor(button, hoverColor, normalColor);
            }
        });
    }

    // Animate button color transition
    private void animateButtonColor(JButton button, Color fromColor, Color toColor) {
        Timer timer = new Timer(20, null);
        int[] step = {0};
        int steps = 10;

        timer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / steps;

            int r = (int) (fromColor.getRed() + (toColor.getRed() - fromColor.getRed()) * progress);
            int g = (int) (fromColor.getGreen() + (toColor.getGreen() - fromColor.getGreen()) * progress);
            int b = (int) (fromColor.getBlue() + (toColor.getBlue() - fromColor.getBlue()) * progress);

            button.setBackground(new Color(r, g, b));

            if (step[0] >= steps) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    private void handleLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            return;
        }

        if (UserDatabase.loginDriver(username, password)) {
            animateButtonSuccess(btnLogin);
            String vType = UserDatabase.getDriverVehicleType(username);
            dispose();
            new DriverDashboard(username, vType);
        } else if (UserDatabase.loginPassenger(username, password)) {
            animateButtonSuccess(btnLogin);
            dispose();
            // PassengerDashboard will handle payment redirect if needed
            PassengerDashboard dashboard = new PassengerDashboard(username);
            // If constructor returned early due to payment redirect, dashboard won't be visible
            if (!dashboard.isVisible()) {
                dashboard.dispose(); // Clean up if not used
            }
        } else {
            animateButtonError(btnLogin);
            JOptionPane.showMessageDialog(this, "Invalid credentials!");
        }
    }

    // Button success animation (green flash)
    private void animateButtonSuccess(JButton button) {
        Color originalColor = button.getBackground();
        Timer timer = new Timer(50, null);
        int[] count = {0};

        timer.addActionListener(e -> {
            count[0]++;
            if (count[0] % 2 == 0) {
                button.setBackground(new Color(76, 175, 80));
            } else {
                button.setBackground(originalColor);
            }

            if (count[0] >= 4) {
                button.setBackground(originalColor);
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    // Button error animation (red flash)
    private void animateButtonError(JButton button) {
        Color originalColor = button.getBackground();
        Timer timer = new Timer(50, null);
        int[] count = {0};

        timer.addActionListener(e -> {
            count[0]++;
            if (count[0] % 2 == 0) {
                button.setBackground(new Color(244, 67, 54));
            } else {
                button.setBackground(originalColor);
            }

            if (count[0] >= 4) {
                button.setBackground(originalColor);
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}