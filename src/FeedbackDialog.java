import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * FeedbackDialog — shown to the passenger after a ride is COMPLETED.
 * Inserts into the `feedback` table (ride_id, passenger_username,
 * driver_username, rating, comments).
 */
public class FeedbackDialog extends JDialog {

    // ── Design tokens (same as RideAdminApp / PassengerDashboard) ─────
    private static final Color BG_DARK        = new Color(10,  14,  26);
    private static final Color BG_CARD        = new Color(18,  24,  42);
    private static final Color BG_CARD2       = new Color(24,  32,  56);
    private static final Color ACCENT         = new Color(0,   210, 180);
    private static final Color ACCENT_RED     = new Color(255, 80,  80);
    private static final Color ACCENT_YELLOW  = new Color(255, 200, 50);
    private static final Color ACCENT_GREEN   = new Color(50,  220, 120);
    private static final Color TEXT_PRIMARY   = new Color(230, 235, 255);
    private static final Color TEXT_SECONDARY = new Color(130, 145, 180);
    private static final Color BORDER_COLOR   = new Color(40,  55,  90);

    // ── State ──────────────────────────────────────────────────────────
    private final int    rideId;
    private final String passengerUsername;
    private final String driverUsername;

    private int selectedRating = 0;           // 1–5 stars
    private final JButton[] starBtns = new JButton[5];
    private JTextArea commentsArea;
    private JLabel  ratingStatusLabel;

    // ══════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════
    public FeedbackDialog(Frame parent,
                          int    rideId,
                          String passengerUsername,
                          String driverUsername) {

        super(parent, "Rate Your Ride", true);

        this.rideId            = rideId;
        this.passengerUsername = passengerUsername;
        this.driverUsername    = driverUsername;

        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildBody(),    BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }

    // ── Header ─────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(18, 24, 18, 24)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Rate Your Ride");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Ride #" + rideId + "  ·  Driver: " + driverUsername);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        bar.add(left, BorderLayout.WEST);

        JLabel icon = new JLabel("⭐");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        bar.add(icon, BorderLayout.EAST);

        return bar;
    }

    // ── Body ───────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_DARK);
        body.setBorder(new EmptyBorder(24, 28, 10, 28));

        // ── Star rating section ────────────────────────────────────────
        RoundedPanel ratingCard = new RoundedPanel(14, BG_CARD);
        ratingCard.setLayout(new BoxLayout(ratingCard, BoxLayout.Y_AXIS));
        ratingCard.setBorder(new EmptyBorder(20, 24, 20, 24));
        ratingCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel ratingTitle = new JLabel("How was your experience?");
        ratingTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratingTitle.setForeground(TEXT_SECONDARY);
        ratingTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingCard.add(ratingTitle);
        ratingCard.add(Box.createVerticalStrut(14));

        // Star buttons row
        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        starRow.setOpaque(false);
        starRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < 5; i++) {
            final int starValue = i + 1;
            JButton star = new JButton("☆");
            star.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            star.setForeground(TEXT_SECONDARY);
            star.setBackground(BG_CARD);
            star.setOpaque(true);
            star.setBorderPainted(false);
            star.setFocusPainted(false);
            star.setCursor(new Cursor(Cursor.HAND_CURSOR));
            star.setBorder(new EmptyBorder(2, 2, 2, 2));
            star.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    // Highlight stars up to hovered position
                    for (int j = 0; j < 5; j++) {
                        starBtns[j].setText(j < starValue ? "★" : "☆");
                        starBtns[j].setForeground(j < starValue ? ACCENT_YELLOW : TEXT_SECONDARY);
                    }
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // Restore to selected rating
                    updateStarDisplay(selectedRating);
                }
            });
            star.addActionListener(e -> {
                selectedRating = starValue;
                updateStarDisplay(selectedRating);
                String[] labels = {"", "Poor", "Fair", "Good", "Very Good", "Excellent"};
                ratingStatusLabel.setText(starValue + " / 5  —  " + labels[starValue]);
                ratingStatusLabel.setForeground(starValue >= 4 ? ACCENT_GREEN
                        : starValue == 3 ? ACCENT_YELLOW : ACCENT_RED);
            });
            starBtns[i] = star;
            starRow.add(star);
        }

        ratingCard.add(starRow);
        ratingCard.add(Box.createVerticalStrut(10));

        ratingStatusLabel = new JLabel("Tap a star to rate");
        ratingStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        ratingStatusLabel.setForeground(TEXT_SECONDARY);
        ratingStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingCard.add(ratingStatusLabel);

        body.add(ratingCard);
        body.add(Box.createVerticalStrut(14));

        // ── Comments section ───────────────────────────────────────────
        RoundedPanel commentsCard = new RoundedPanel(14, BG_CARD);
        commentsCard.setLayout(new BoxLayout(commentsCard, BoxLayout.Y_AXIS));
        commentsCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        commentsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel commentsTitle = new JLabel("Comments  (optional)");
        commentsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        commentsTitle.setForeground(TEXT_SECONDARY);
        commentsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentsCard.add(commentsTitle);
        commentsCard.add(Box.createVerticalStrut(10));

        commentsArea = new JTextArea(4, 30);
        commentsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        commentsArea.setBackground(BG_CARD2);
        commentsArea.setForeground(TEXT_PRIMARY);
        commentsArea.setCaretColor(ACCENT);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        JScrollPane commentScroll = new JScrollPane(commentsArea);
        commentScroll.setBorder(null);
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        commentsCard.add(commentScroll);

        body.add(commentsCard);
        return body;
    }

    // ── Footer (action buttons) ────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(10, 28, 22, 28));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        // Skip button
        JButton skipBtn = new JButton("Skip for Now");
        skipBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        skipBtn.setForeground(TEXT_SECONDARY);
        skipBtn.setBackground(BG_CARD2);
        skipBtn.setOpaque(true);
        skipBtn.setBorderPainted(true);
        skipBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 16, 10, 16)));
        skipBtn.setFocusPainted(false);
        skipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                skipBtn.setForeground(TEXT_PRIMARY);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                skipBtn.setForeground(TEXT_SECONDARY);
            }
        });
        skipBtn.addActionListener(e -> dispose());

        // Submit button
        JButton submitBtn = new JButton("⭐  Submit Feedback");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.setForeground(BG_DARK);
        submitBtn.setBackground(ACCENT);
        submitBtn.setOpaque(true);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        Color accentDark = ACCENT.darker();
        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { submitBtn.setBackground(accentDark); }
            public void mouseExited (java.awt.event.MouseEvent e) { submitBtn.setBackground(ACCENT);     }
        });
        submitBtn.addActionListener(e -> handleSubmit());

        btnRow.add(skipBtn);
        btnRow.add(submitBtn);

        footer.add(btnRow, BorderLayout.CENTER);
        return footer;
    }

    // ── Star display helper ────────────────────────────────────────────
    private void updateStarDisplay(int rating) {
        for (int j = 0; j < 5; j++) {
            starBtns[j].setText(j < rating ? "★" : "☆");
            starBtns[j].setForeground(j < rating ? ACCENT_YELLOW : TEXT_SECONDARY);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  SUBMIT — inserts into `feedback` table
    // ══════════════════════════════════════════════════════════════════
    private void handleSubmit() {

        if (selectedRating == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a star rating before submitting.",
                    "Rating Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String comments = commentsArea.getText().trim();

        // ✅ DO NOT include feedback_id (auto-generated)
        String sql = "INSERT INTO RideFeedback "
                + "(ride_id, passenger_username, driver_username, rating, comments) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            // ✅ Correct parameter order
            pst.setInt(1, rideId);
            pst.setString(2, passengerUsername);
            pst.setString(3, driverUsername);
            pst.setInt(4, selectedRating);
            pst.setString(5, comments.isEmpty() ? null : comments);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Thank you for your feedback!",
                        "Feedback Submitted", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Feedback could not be saved. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Rounded Panel (shared design component) ───────────────────────
    static class RoundedPanel extends JPanel {
        private final int   radius;
        private       Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}