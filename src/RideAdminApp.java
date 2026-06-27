import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.Vector;

public class RideAdminApp extends JFrame {

    // ── Colors ──────────────────────────────────
    private static final Color BG_DARK        = new Color(10, 14, 26);
    private static final Color BG_CARD        = new Color(18, 24, 42);
    private static final Color BG_CARD2       = new Color(24, 32, 56);
    private static final Color ACCENT         = new Color(0, 210, 180);
    private static final Color ACCENT_RED     = new Color(255, 80, 80);
    private static final Color ACCENT_YELLOW  = new Color(255, 200, 50);
    private static final Color ACCENT_BLUE    = new Color(60, 140, 255);
    private static final Color ACCENT_PURPLE  = new Color(160, 80, 255);
    private static final Color ACCENT_GREEN   = new Color(50, 220, 120);
    private static final Color ACCENT_ORANGE  = new Color(255, 140, 50);
    private static final Color TEXT_PRIMARY   = new Color(230, 235, 255);
    private static final Color TEXT_SECONDARY = new Color(130, 145, 180);
    private static final Color BORDER_COLOR   = new Color(40, 55, 90);
    private static final Color TABLE_HEADER   = new Color(20, 28, 50);
    private static final Color TABLE_ROW1     = new Color(16, 22, 40);
    private static final Color TABLE_ROW2     = new Color(20, 28, 50);
    private static final Color TABLE_SELECT   = new Color(0, 100, 90);

    private JPanel contentArea;
    private JLabel pageTitle;

    public RideAdminApp() {
        setTitle("RideFlow — Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setLocationRelativeTo(null);
        setResizable(true);
        setBackground(BG_DARK);
        showLoginPage();
        setVisible(true);
    }

    // ── DB Connection ────────────────────────────
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // ════════════════════════════════════════════
    //  LOGIN PAGE
    // ════════════════════════════════════════════
    private void showLoginPage() {
        setSize(480, 580);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(20, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 44, 40, 44));
        card.setPreferredSize(new Dimension(400, 500));
        card.setMaximumSize(new Dimension(400, 500));

        JLabel logo = new JLabel("RIDEFLOW");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Admin Console");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLbl = styledLabel("Username");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField userField = styledField("admin");
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLbl = styledLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField passField = new JPasswordField("admin123");
        styleTextField(passField);
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLbl.setForeground(ACCENT_RED);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = accentButton("Login →", ACCENT, BG_DARK);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            if (u.equals("admin") && p.equals("admin123")) {
                showDashboard();
            } else {
                errLbl.setText("Invalid credentials. Try again.");
            }
        });
        passField.addActionListener(e -> loginBtn.doClick());

        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(sep);
        card.add(Box.createVerticalStrut(28));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(userField);
        card.add(Box.createVerticalStrut(20));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));
        card.add(errLbl);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);

        JPanel leftWrap = new JPanel();
        leftWrap.setOpaque(false);
        leftWrap.setLayout(new BoxLayout(leftWrap, BoxLayout.Y_AXIS));
        leftWrap.setBorder(new EmptyBorder(40, 40, 40, 40));
        leftWrap.add(card);
        leftWrap.add(Box.createVerticalGlue());

        root.add(leftWrap, BorderLayout.WEST);
        JPanel rightPane = new JPanel();
        rightPane.setBackground(BG_DARK);
        root.add(rightPane, BorderLayout.CENTER);

        setContentPane(root);
        revalidate(); repaint();
    }

    // ════════════════════════════════════════════
    //  MAIN DASHBOARD SHELL
    // ════════════════════════════════════════════
    private void showDashboard() {
        setSize(1200, 740);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        root.add(createSidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG_DARK);
        right.add(createTopBar(), BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BG_DARK);
        contentArea.setBorder(new EmptyBorder(0, 24, 24, 24));

        showHomeCards();

        right.add(contentArea, BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);

        setContentPane(root);
        revalidate(); repaint();
    }

    // ── Sidebar ──────────────────────────────────
    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG_CARD);
        side.setPreferredSize(new Dimension(220, 740));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        side.add(Box.createVerticalStrut(28));

        JLabel logo = new JLabel("RIDEFLOW");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(ACCENT);
        logo.setBorder(new EmptyBorder(0, 22, 0, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);

        JLabel adminTag = new JLabel("Admin Panel");
        adminTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        adminTag.setForeground(TEXT_SECONDARY);
        adminTag.setBorder(new EmptyBorder(2, 22, 0, 0));
        adminTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(adminTag);

        side.add(Box.createVerticalStrut(30));

        // ── MANAGE section ───────────────────────
        side.add(sidebarDivider("MANAGE"));
        side.add(Box.createVerticalStrut(6));
        side.add(sidebarBtn("Passengers",     ACCENT_BLUE,   () -> showUserTable("Passenger")));
        side.add(Box.createVerticalStrut(4));
        side.add(sidebarBtn("Drivers",        ACCENT_GREEN,  () -> showUserTable("Driver")));
        side.add(Box.createVerticalStrut(4));
        side.add(sidebarBtn("Ride Requests",  ACCENT_YELLOW, () -> showRideRequests()));
        side.add(Box.createVerticalStrut(20));

        // ── STATS section ────────────────────────
        side.add(sidebarDivider("STATS"));
        side.add(Box.createVerticalStrut(6));
        side.add(sidebarBtn("Driver Stats",    ACCENT_ORANGE, () -> showDriverStats()));
        side.add(Box.createVerticalStrut(4));
        side.add(sidebarBtn("Passenger Stats", ACCENT_PURPLE, () -> showPassengerStats()));
        side.add(Box.createVerticalStrut(4));
        side.add(sidebarBtn("Driver Feedback", ACCENT_GREEN,  () -> showFeedbackPage()));
        side.add(Box.createVerticalStrut(20));

        // ── TOOLS section ────────────────────────
        side.add(sidebarDivider("TOOLS"));
        side.add(Box.createVerticalStrut(6));
        side.add(sidebarBtn("Search User",    ACCENT_PURPLE, () -> showSearchPanel()));
        side.add(Box.createVerticalStrut(4));
        side.add(sidebarBtn("Home",           ACCENT,        () -> showHomeCards()));

        side.add(Box.createVerticalGlue());

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logout.setForeground(ACCENT_RED);
        logout.setBackground(new Color(40, 20, 20));
        logout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 80, 80, 120), 1, true),
                new EmptyBorder(8, 18, 8, 18)));
        logout.setFocusPainted(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.setOpaque(true);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { logout.setBackground(new Color(80, 20, 20)); }
            public void mouseExited (java.awt.event.MouseEvent e) { logout.setBackground(new Color(40, 20, 20)); }
        });
        logout.addActionListener(e -> showLoginPage());

        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(logout);
        logoutWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logoutWrapper);
        side.add(Box.createVerticalStrut(24));

        return side;
    }

    private JPanel sidebarDivider(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_SECONDARY);
        p.add(lbl);
        return p;
    }

    private JPanel sidebarBtn(String text, Color accent, Runnable action) {
        Color hoverBg = new Color(
                Math.min(255, BG_CARD.getRed()   + (accent.getRed()   - BG_CARD.getRed())   / 4),
                Math.min(255, BG_CARD.getGreen() + (accent.getGreen() - BG_CARD.getGreen()) / 4),
                Math.min(255, BG_CARD.getBlue()  + (accent.getBlue()  - BG_CARD.getBlue())  / 4)
        );

        JPanel wrapper = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {}
        };
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setPreferredSize(new Dimension(220, 38));
        wrapper.setMaximumSize(new Dimension(220, 38));
        wrapper.setMinimumSize(new Dimension(220, 38));
        wrapper.setLayout(new BorderLayout());

        JPanel accentBar = new JPanel();
        accentBar.setBackground(BG_CARD);
        accentBar.setPreferredSize(new Dimension(3, 38));
        accentBar.setOpaque(true);
        wrapper.add(accentBar, BorderLayout.WEST);

        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_CARD);
        btn.setBorder(new EmptyBorder(6, 16, 6, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.setForeground(accent);
                accentBar.setBackground(accent);
                wrapper.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(BG_CARD);
                btn.setForeground(TEXT_PRIMARY);
                accentBar.setBackground(BG_CARD);
                wrapper.repaint();
            }
        });
        btn.addActionListener(e -> {
            setPageTitle(text.replaceAll("[^a-zA-Z ]", "").trim());
            action.run();
        });

        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Top Bar ──────────────────────────────────
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pageTitle.setForeground(TEXT_PRIMARY);
        bar.add(pageTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel admin = new JLabel("Admin  ●");
        admin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        admin.setForeground(ACCENT);

        JLabel divider = new JLabel("|");
        divider.setForeground(BORDER_COLOR);
        divider.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton topLogout = new JButton("⏻  Logout");
        topLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topLogout.setForeground(ACCENT_RED);
        topLogout.setBackground(new Color(40, 20, 20));
        topLogout.setOpaque(true);
        topLogout.setBorderPainted(true);
        topLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 50, 50), 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        topLogout.setFocusPainted(false);
        topLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        topLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                topLogout.setBackground(ACCENT_RED);
                topLogout.setForeground(Color.WHITE);
                topLogout.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_RED, 1, true),
                        new EmptyBorder(6, 14, 6, 14)));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                topLogout.setBackground(new Color(40, 20, 20));
                topLogout.setForeground(ACCENT_RED);
                topLogout.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 50, 50), 1, true),
                        new EmptyBorder(6, 14, 6, 14)));
            }
        });
        topLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    RideAdminApp.this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) showLoginPage();
        });

        rightPanel.add(admin);
        rightPanel.add(divider);
        rightPanel.add(topLogout);
        bar.add(rightPanel, BorderLayout.EAST);

        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(18, 24, 14, 24)));

        return bar;
    }

    private void setPageTitle(String title) {
        if (pageTitle != null) pageTitle.setText(title);
    }

    // ── Home Cards ───────────────────────────────
    private void showHomeCards() {
        setPageTitle("Dashboard");
        contentArea.removeAll();

        int passengerCount = getCount("SELECT COUNT(*) FROM Passengers");
        int driverCount    = getCount("SELECT COUNT(*) FROM Drivers");
        int rideCount      = getCount("SELECT COUNT(*) FROM ride_requests");
        int pendingCount   = getCount("SELECT COUNT(*) FROM ride_requests WHERE status='PENDING'");

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        statsRow.add(statCard("Passengers",    String.valueOf(passengerCount), "👥", ACCENT_BLUE));
        statsRow.add(statCard("Drivers",       String.valueOf(driverCount),    "🚗", ACCENT_GREEN));
        statsRow.add(statCard("Total Rides",   String.valueOf(rideCount),      "🛣️",  ACCENT_YELLOW));
        statsRow.add(statCard("Pending Rides", String.valueOf(pendingCount),   "⏳", ACCENT_RED));

        // 7 cards → 3 rows × 3 cols (last cell empty, looks clean)
        JPanel cards = new JPanel(new GridLayout(3, 3, 16, 16));
        cards.setOpaque(false);

        cards.add(dashCard("👥 Passengers",      "View, edit or delete passenger accounts",   ACCENT_BLUE,   () -> showUserTable("Passenger")));
        cards.add(dashCard("🚗 Drivers",          "View, edit or delete driver accounts",      ACCENT_GREEN,  () -> showUserTable("Driver")));
        cards.add(dashCard("🛣️ Ride Requests",    "View all ride history and statuses",         ACCENT_YELLOW, () -> showRideRequests()));
        cards.add(dashCard("📊 Driver Stats",     "Total finished rides per driver",           ACCENT_ORANGE, () -> showDriverStats()));
        cards.add(dashCard("📈 Passenger Stats",  "Total rides per passenger",                 ACCENT_PURPLE, () -> showPassengerStats()));
        cards.add(dashCard("⭐ Driver Feedback",  "View passenger ratings & comments per driver", ACCENT_GREEN, () -> showFeedbackPage()));
        cards.add(dashCard("🔍 Search User",      "Search passenger or driver by username",    ACCENT_PURPLE, () -> showSearchPanel()));
        // two empty filler panels to complete the 3×3 grid neatly
        cards.add(emptyCard());
        cards.add(emptyCard());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(statsRow, BorderLayout.NORTH);
        wrapper.add(cards,    BorderLayout.CENTER);

        contentArea.add(wrapper, BorderLayout.NORTH);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // Transparent placeholder so the GridLayout stays aligned
    private JPanel emptyCard() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private int getCount(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private JPanel statCard(String label, String value, String icon, Color accent) {
        RoundedPanel card = new RoundedPanel(14, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        ico.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 30));
        val.setForeground(accent);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(ico);
        card.add(Box.createVerticalStrut(8));
        card.add(val);
        card.add(Box.createVerticalStrut(4));
        card.add(lbl);
        return card;
    }

    private JPanel dashCard(String title, String desc, Color accent, Runnable action) {
        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hoverBg = new Color(
                Math.min(255, BG_CARD.getRed()   + (accent.getRed()   - BG_CARD.getRed())   / 6),
                Math.min(255, BG_CARD.getGreen() + (accent.getGreen() - BG_CARD.getGreen()) / 6),
                Math.min(255, BG_CARD.getBlue()  + (accent.getBlue()  - BG_CARD.getBlue())  / 6)
        );

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(accent);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><p style='width:180px'>" + desc + "</p></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(TEXT_SECONDARY);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel arrow = new JLabel("→  Open");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        arrow.setForeground(accent);
        arrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(descLbl);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(16));
        card.add(arrow);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { action.run(); }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBgColor(hoverBg); card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBgColor(BG_CARD); card.repaint();
            }
        });
        return card;
    }

    // ════════════════════════════════════════════
    //  PASSENGERS / DRIVERS TABLE
    // ════════════════════════════════════════════
    private void showUserTable(String role) {
        setPageTitle(role + "s");
        contentArea.removeAll();

        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        String query = role.equals("Passenger")
                ? "SELECT u.UserID, u.Username, u.Email, u.Phone, u.CreatedAt " +
                "FROM Users u JOIN Passengers p ON u.UserID = p.UserID"
                : "SELECT u.UserID, u.Username, u.Email, u.Phone, d.LicenseNumber, d.VehicleType, d.IsAvailable, d.Rating " +
                "FROM Users u JOIN Drivers d ON u.UserID = d.UserID";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++)
                model.addColumn(meta.getColumnName(i));

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.add(rs.getObject(i));
                model.addRow(row);
            }
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
            return;
        }

        JTable table = buildStyledTable(model);
        JScrollPane scroll = buildScrollPane(table);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton deleteBtn = accentButton("🗑  Delete Selected", ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showError("Select a row first."); return; }
            int userId = (int) model.getValueAt(row, 0);
            String uname = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete user '" + uname + "'? This cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "DELETE FROM Users WHERE UserID = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    model.removeRow(row);
                    showSuccess("User deleted successfully.");
                } catch (SQLException ex) { showError(ex.getMessage()); }
            }
        });

        JButton refreshBtn = accentButton("↻  Refresh", ACCENT, BG_DARK);
        refreshBtn.addActionListener(e -> showUserTable(role));

        btnBar.add(deleteBtn);
        btnBar.add(refreshBtn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildBackBar(), BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        wrapper.add(btnBar, BorderLayout.SOUTH);

        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ════════════════════════════════════════════
    //  DRIVER STATS PAGE
    // ════════════════════════════════════════════
    private void showDriverStats() {
        setPageTitle("Driver Stats");
        contentArea.removeAll();

        int totalFinished = getCount("SELECT SUM(TotalRidesCompleted) FROM DriverDetails");
        int totalDrivers  = getCount("SELECT COUNT(*) FROM Drivers");

        JPanel summaryRow = new JPanel(new GridLayout(1, 2, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(new EmptyBorder(0, 0, 18, 0));
        summaryRow.add(statCard("Total Drivers",         String.valueOf(totalDrivers),  "🚗", ACCENT_GREEN));
        summaryRow.add(statCard("Total Finished Rides",  String.valueOf(totalFinished), "✅", ACCENT_ORANGE));

        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.setColumnIdentifiers(new String[]{
                "Driver ID", "Username", "Email", "Phone",
                "License No", "Vehicle Type", "Available", "Rating", "Finished Rides"
        });

        String query =
                "SELECT DriverID, Username, Email, Phone, " +
                        "LicenseNumber, VehicleType, IsAvailable, Rating, TotalRidesCompleted " +
                        "FROM DriverDetails " +
                        "ORDER BY TotalRidesCompleted DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("DriverID"),
                        rs.getString("Username"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("LicenseNumber"),
                        rs.getString("VehicleType"),
                        rs.getBoolean("IsAvailable") ? "Yes" : "No",
                        rs.getString("Rating"),
                        rs.getInt("TotalRidesCompleted")
                });
            }
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
            return;
        }

        JTable table = buildStyledTable(model);

        // Highlight the "Finished Rides" column in orange
        table.getColumnModel().getColumn(8).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                        super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                        setForeground(sel ? Color.WHITE : ACCENT_ORANGE);
                        setBackground(sel ? TABLE_SELECT : (r % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setBorder(new EmptyBorder(0, 12, 0, 12));
                        return this;
                    }
                });

        JScrollPane scroll = buildScrollPane(table);

        JButton refreshBtn = accentButton("↻  Refresh", ACCENT, BG_DARK);
        refreshBtn.addActionListener(e -> showDriverStats());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(14, 0, 0, 0));
        btnBar.add(refreshBtn);

        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setOpaque(false);
        tableSection.add(summaryRow, BorderLayout.NORTH);
        tableSection.add(scroll,     BorderLayout.CENTER);
        tableSection.add(btnBar,     BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildBackBar(),  BorderLayout.NORTH);
        wrapper.add(tableSection,    BorderLayout.CENTER);

        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ════════════════════════════════════════════
    //  PASSENGER STATS PAGE
    // ════════════════════════════════════════════
    private void showPassengerStats() {
        setPageTitle("Passenger Stats");
        contentArea.removeAll();

        int totalPassengers = getCount("SELECT COUNT(*) FROM Passengers");
        int totalRides      = getCount("SELECT SUM(TotalRides) FROM PassengerDetails");

        JPanel summaryRow = new JPanel(new GridLayout(1, 2, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(new EmptyBorder(0, 0, 18, 0));
        summaryRow.add(statCard("Total Passengers", String.valueOf(totalPassengers), "👥", ACCENT_BLUE));
        summaryRow.add(statCard("Total Rides Made", String.valueOf(totalRides),      "🛣️",  ACCENT_PURPLE));

        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.setColumnIdentifiers(new String[]{
                "Passenger ID", "Username", "Email", "Phone", "Joined On", "Total Rides"
        });

        String query =
                "SELECT PassengerID, Username, Email, Phone, CreatedAt, TotalRides " +
                        "FROM PassengerDetails " +
                        "ORDER BY TotalRides DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("PassengerID"),
                        rs.getString("Username"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("CreatedAt"),
                        rs.getInt("TotalRides")
                });
            }
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
            return;
        }

        JTable table = buildStyledTable(model);

        // Highlight the "Total Rides" column in purple
        table.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                        super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                        setForeground(sel ? Color.WHITE : ACCENT_PURPLE);
                        setBackground(sel ? TABLE_SELECT : (r % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setBorder(new EmptyBorder(0, 12, 0, 12));
                        return this;
                    }
                });

        JScrollPane scroll = buildScrollPane(table);

        JButton refreshBtn = accentButton("↻  Refresh", ACCENT, BG_DARK);
        refreshBtn.addActionListener(e -> showPassengerStats());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(14, 0, 0, 0));
        btnBar.add(refreshBtn);

        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setOpaque(false);
        tableSection.add(summaryRow, BorderLayout.NORTH);
        tableSection.add(scroll,     BorderLayout.CENTER);
        tableSection.add(btnBar,     BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildBackBar(), BorderLayout.NORTH);
        wrapper.add(tableSection,   BorderLayout.CENTER);

        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ════════════════════════════════════════════
    //  DRIVER FEEDBACK PAGE
    //  Shows all passenger feedback against drivers.
    //  Filterable by driver username.
    //  Rating column: colour-coded stars (green → red).
    //  Comments: truncated in cell, full text on hover.
    // ════════════════════════════════════════════
    private void showFeedbackPage() {
        setPageTitle("Driver Feedback");
        contentArea.removeAll();

        // ── Summary stat cards ───────────────────
        int totalFeedbacks = getCount("SELECT COUNT(*) FROM RideFeedback");

        double avgRating = 0.0;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT AVG(CAST(rating AS DECIMAL(4,2))) FROM RideFeedback")) {
            if (rs.next()) avgRating = rs.getDouble(1);
        } catch (SQLException ignored) {}

        int totalDriversWithFeedback = getCount(
                "SELECT COUNT(DISTINCT driver_username) FROM RideFeedback");

        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(new EmptyBorder(0, 0, 18, 0));
        summaryRow.add(statCard("Total Feedbacks",        String.valueOf(totalFeedbacks),  "💬", ACCENT_BLUE));
        summaryRow.add(statCard("Avg Driver Rating",      String.format("%.2f / 5.00", avgRating), "⭐", ACCENT_YELLOW));
        summaryRow.add(statCard("Drivers with Feedback",  String.valueOf(totalDriversWithFeedback), "🚗", ACCENT_GREEN));

        // ── Driver filter bar ────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel filterLbl = styledLabel("Filter by Driver:");
        filterLbl.setForeground(TEXT_SECONDARY);

        JComboBox<String> driverFilter = new JComboBox<>();
        driverFilter.setBackground(BG_CARD2);
        driverFilter.setForeground(TEXT_PRIMARY);
        driverFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        driverFilter.addItem("ALL DRIVERS");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DISTINCT driver_username FROM RideFeedback ORDER BY driver_username")) {
            while (rs.next()) {
                String dname = rs.getString("driver_username");
                if (dname != null && !dname.isBlank()) driverFilter.addItem(dname);
            }
        } catch (SQLException e) {
            showError("Could not load driver list: " + e.getMessage());
        }

        // ── Rating filter ────────────────────────
        JLabel ratingLbl = styledLabel("  Min Rating:");
        ratingLbl.setForeground(TEXT_SECONDARY);

        String[] ratingOpts = {"Any", "1+", "2+", "3+", "4+", "5"};
        JComboBox<String> ratingFilter = new JComboBox<>(ratingOpts);
        ratingFilter.setBackground(BG_CARD2);
        ratingFilter.setForeground(TEXT_PRIMARY);
        ratingFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        filterBar.add(filterLbl);
        filterBar.add(driverFilter);
        filterBar.add(ratingLbl);
        filterBar.add(ratingFilter);

        // ── Table ────────────────────────────────
        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.setColumnIdentifiers(new String[]{
                "ID", "Driver", "Passenger", "Ride ID", "Rating", "Comments", "Date"
        });

        JTable table = buildStyledTable(model);
        table.setRowHeight(40);

        // ── Rating column (col 4) — colour-coded stars ──
        table.getColumnModel().getColumn(4).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                        super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                        int rating = 0;
                        try { rating = Integer.parseInt(val != null ? val.toString().trim() : "0"); }
                        catch (NumberFormatException ignored) {}

                        Color ratingColor;
                        if      (rating == 5) ratingColor = ACCENT_GREEN;
                        else if (rating == 4) ratingColor = new Color(140, 220, 80);
                        else if (rating == 3) ratingColor = ACCENT_YELLOW;
                        else if (rating == 2) ratingColor = ACCENT_ORANGE;
                        else                  ratingColor = ACCENT_RED;

                        String stars = "★".repeat(Math.max(0, rating))
                                + "☆".repeat(Math.max(0, 5 - rating));
                        setText(stars + "  (" + rating + ")");
                        setForeground(sel ? Color.WHITE : ratingColor);
                        setBackground(sel ? TABLE_SELECT : (r % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setBorder(new EmptyBorder(0, 12, 0, 12));
                        return this;
                    }
                });

        // ── Comments column (col 5) — truncated, full text on hover ──
        table.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                        super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                        String comment = (val != null && !val.toString().isBlank())
                                ? val.toString() : "— no comment —";
                        setText(comment.length() > 55
                                ? comment.substring(0, 52) + "..." : comment);
                        setToolTipText("<html><body style='width:300px'>" + comment + "</body></html>");
                        setForeground(sel ? Color.WHITE : TEXT_SECONDARY);
                        setBackground(sel ? TABLE_SELECT : (r % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2));
                        setFont(new Font("Segoe UI", Font.ITALIC, 12));
                        setBorder(new EmptyBorder(0, 12, 0, 12));
                        return this;
                    }
                });

        // ── Set preferred column widths ──────────
        int[] colWidths = {50, 110, 110, 70, 160, 300, 150};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Initial load
        loadFeedback(model, "ALL DRIVERS", 0);

        // React to filter changes
        driverFilter.addActionListener(e -> {
            int minRating = ratingFilter.getSelectedIndex(); // index 0=Any, 1=1+, ...
            loadFeedback(model,
                    (String) driverFilter.getSelectedItem(),
                    minRating);
        });
        ratingFilter.addActionListener(e -> {
            int minRating = ratingFilter.getSelectedIndex();
            loadFeedback(model,
                    (String) driverFilter.getSelectedItem(),
                    minRating);
        });

        JScrollPane scroll = buildScrollPane(table);

        // ── Button bar ───────────────────────────
        JButton refreshBtn = accentButton("↻  Refresh", ACCENT, BG_DARK);
        refreshBtn.addActionListener(e ->
                loadFeedback(model,
                        (String) driverFilter.getSelectedItem(),
                        ratingFilter.getSelectedIndex()));

        JButton deleteBtn = accentButton("🗑  Delete Selected", ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showError("Select a feedback row first."); return; }
            int fid = (int) model.getValueAt(row, 0);
            String driver = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete Feedback #" + fid + " for driver '" + driver + "'?\nThis cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "DELETE FROM RideFeedback WHERE feedback_id = ?")) {
                    ps.setInt(1, fid);
                    ps.executeUpdate();
                    model.removeRow(row);
                    showSuccess("Feedback #" + fid + " deleted.");
                } catch (SQLException ex) { showError(ex.getMessage()); }
            }
        });

        JButton viewFullBtn = accentButton("💬  View Full Comment", ACCENT_BLUE, BG_DARK);
        viewFullBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showError("Select a row to view its comment."); return; }
            String comment = model.getValueAt(row, 5).toString();
            String driver  = model.getValueAt(row, 1).toString();
            String pass    = model.getValueAt(row, 2).toString();
            String rating  = model.getValueAt(row, 4).toString();

            JTextArea ta = new JTextArea(comment);
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setEditable(false);
            ta.setBackground(BG_CARD2);
            ta.setForeground(TEXT_PRIMARY);
            ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            ta.setBorder(new EmptyBorder(10, 10, 10, 10));
            ta.setPreferredSize(new Dimension(400, 120));

            JPanel dialogPanel = new JPanel(new BorderLayout(0, 8));
            dialogPanel.setBackground(BG_CARD);
            dialogPanel.add(new JLabel(
                            "<html><b style='color:#00D2B4'>Driver:</b> " + driver +
                                    "  &nbsp;  <b style='color:#00D2B4'>Passenger:</b> " + pass +
                                    "  &nbsp;  <b style='color:#FFD700'>Rating:</b> " + rating + "/5</html>"),
                    BorderLayout.NORTH);
            dialogPanel.add(new JScrollPane(ta), BorderLayout.CENTER);

            JOptionPane.showMessageDialog(this, dialogPanel,
                    "Full Feedback Comment", JOptionPane.PLAIN_MESSAGE);
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(14, 0, 0, 0));
        btnBar.add(deleteBtn);
        btnBar.add(viewFullBtn);
        btnBar.add(refreshBtn);

        // ── Layout assembly ──────────────────────
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(summaryRow, BorderLayout.NORTH);
        topSection.add(filterBar,  BorderLayout.SOUTH);

        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setOpaque(false);
        tableSection.add(topSection, BorderLayout.NORTH);
        tableSection.add(scroll,     BorderLayout.CENTER);
        tableSection.add(btnBar,     BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildBackBar(),  BorderLayout.NORTH);
        wrapper.add(tableSection,    BorderLayout.CENTER);

        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    /**
     * Loads feedback rows into the given model, filtered by driver and minimum rating.
     *
     * @param model        the table model to populate
     * @param driverFilter "ALL DRIVERS" or a specific driver username
     * @param minRating    0 = any, 1 = rating >= 1, ..., 5 = rating = 5
     */
    private void loadFeedback(DefaultTableModel model, String driverFilter, int minRating) {
        model.setRowCount(0);

        boolean allDrivers = "ALL DRIVERS".equals(driverFilter);
        boolean anyRating  = (minRating == 0);

        // Build query dynamically based on active filters
        StringBuilder sb = new StringBuilder(
                "SELECT feedback_id, driver_username, passenger_username, ride_id, " +
                        "rating, comments, feedback_date FROM RideFeedback WHERE 1=1 ");

        if (!allDrivers) sb.append("AND driver_username = ? ");
        if (!anyRating)  sb.append("AND rating >= ? ");
        sb.append("ORDER BY feedback_date DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            int idx = 1;
            if (!allDrivers) ps.setString(idx++, driverFilter);
            if (!anyRating)  ps.setInt(idx,      minRating);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String comment = rs.getString("comments");
                model.addRow(new Object[]{
                        rs.getInt("feedback_id"),
                        rs.getString("driver_username"),
                        rs.getString("passenger_username"),
                        rs.getInt("ride_id"),
                        rs.getInt("rating"),
                        (comment != null && !comment.isBlank()) ? comment : "— no comment —",
                        rs.getString("feedback_date")
                });
            }

            if (model.getRowCount() == 0 && !allDrivers) {
                showError("No feedback found for driver: " + driverFilter);
            }

        } catch (SQLException e) {
            showError("DB Error loading feedback: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════
    //  RIDE REQUESTS TABLE
    // ════════════════════════════════════════════
    private void showRideRequests() {
        setPageTitle("Ride Requests");
        contentArea.removeAll();

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel filterLbl = styledLabel("Filter by status:");
        filterLbl.setForeground(TEXT_SECONDARY);

        String[] statuses = {"ALL", "PENDING", "ACCEPTED", "FINISHED", "CANCELLED"};
        JComboBox<String> statusFilter = new JComboBox<>(statuses);
        statusFilter.setBackground(BG_CARD2);
        statusFilter.setForeground(TEXT_PRIMARY);
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        filterBar.add(filterLbl);
        filterBar.add(statusFilter);

        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.setColumnIdentifiers(new String[]{
                "ID", "Passenger", "Pickup", "Dropoff", "Vehicle", "Fare", "Status", "Driver", "Time"
        });

        JTable table = buildStyledTable(model);

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String status = val != null ? val.toString() : "";
                setForeground(switch (status) {
                    case "PENDING"   -> ACCENT_YELLOW;
                    case "ACCEPTED"  -> ACCENT_BLUE;
                    case "FINISHED"  -> ACCENT_GREEN;
                    case "CANCELLED" -> ACCENT_RED;
                    default          -> TEXT_PRIMARY;
                });
                setBackground(sel ? TABLE_SELECT : (r % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return this;
            }
        });

        loadRideRequests(model, "ALL");
        statusFilter.addActionListener(e ->
                loadRideRequests(model, (String) statusFilter.getSelectedItem()));

        JScrollPane scroll = buildScrollPane(table);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton deleteBtn = accentButton("🗑  Delete Selected", ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showError("Select a row first."); return; }
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete ride #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "DELETE FROM ride_requests WHERE id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    model.removeRow(row);
                    showSuccess("Ride deleted.");
                } catch (SQLException ex) { showError(ex.getMessage()); }
            }
        });

        JButton updateBtn = accentButton("✏  Update Status", ACCENT_YELLOW, BG_DARK);
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showError("Select a row first."); return; }
            int id = (int) model.getValueAt(row, 0);
            String[] opts = {"PENDING", "ACCEPTED", "FINISHED", "CANCELLED"};
            String chosen = (String) JOptionPane.showInputDialog(this,
                    "Select new status for Ride #" + id,
                    "Update Status", JOptionPane.PLAIN_MESSAGE,
                    null, opts, model.getValueAt(row, 6));
            if (chosen != null) {
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE ride_requests SET status=? WHERE id=?")) {
                    ps.setString(1, chosen);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    model.setValueAt(chosen, row, 6);
                    showSuccess("Status updated to " + chosen);
                } catch (SQLException ex) { showError(ex.getMessage()); }
            }
        });

        JButton refreshBtn = accentButton("↻  Refresh", ACCENT, BG_DARK);
        refreshBtn.addActionListener(e ->
                loadRideRequests(model, (String) statusFilter.getSelectedItem()));

        btnBar.add(deleteBtn);
        btnBar.add(updateBtn);
        btnBar.add(refreshBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(filterBar, BorderLayout.NORTH);
        top.add(scroll,    BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildBackBar(), BorderLayout.NORTH);
        wrapper.add(top,            BorderLayout.CENTER);
        wrapper.add(btnBar,         BorderLayout.SOUTH);

        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void loadRideRequests(DefaultTableModel model, String status) {
        model.setRowCount(0);
        String query = status.equals("ALL")
                ? "SELECT id, passenger_username, pickup_location, dropoff_location, " +
                "vehicle_type, fare, status, driver_username, request_time FROM ride_requests ORDER BY request_time DESC"
                : "SELECT id, passenger_username, pickup_location, dropoff_location, " +
                "vehicle_type, fare, status, driver_username, request_time FROM ride_requests " +
                "WHERE status=? ORDER BY request_time DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (!status.equals("ALL")) ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("passenger_username"),
                        rs.getString("pickup_location"),
                        rs.getString("dropoff_location"),
                        rs.getString("vehicle_type"),
                        "Rs. " + rs.getString("fare"),
                        rs.getString("status"),
                        rs.getString("driver_username"),
                        rs.getString("request_time")
                });
            }
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    // ════════════════════════════════════════════
    //  SEARCH
    // ════════════════════════════════════════════
    private void showSearchPanel() {
        setPageTitle("Search User");
        contentArea.removeAll();

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JTextField field = styledField("Enter username...");
        field.setPreferredSize(new Dimension(280, 38));

        JButton btn = accentButton("Search", ACCENT, BG_DARK);

        searchBar.add(field);
        searchBar.add(btn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Role","UserID","Username","Email","Phone","Extra"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);

        btn.addActionListener(e -> {
            model.setRowCount(0);
            String term = "%" + field.getText().trim() + "%";
            String query =
                    "SELECT 'Passenger' AS Role, u.UserID, u.Username, u.Email, u.Phone, " +
                            "CONVERT(VARCHAR, u.CreatedAt, 120) AS Extra " +
                            "FROM Users u " +
                            "JOIN Passengers p ON u.UserID = p.UserID " +
                            "WHERE u.Username LIKE ? " +
                            "UNION ALL " +
                            "SELECT 'Driver' AS Role, u.UserID, u.Username, u.Email, u.Phone, " +
                            "d.VehicleType AS Extra " +
                            "FROM Users u " +
                            "JOIN Drivers d ON u.UserID = d.UserID " +
                            "WHERE u.Username LIKE ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, term);
                ps.setString(2, term);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("Role"),
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("Email"),
                            rs.getString("Phone"),
                            rs.getString("Extra")
                    });
                }
                if (model.getRowCount() == 0)
                    showError("No users found for: " + field.getText());
            } catch (SQLException ex) { showError(ex.getMessage()); }
        });

        field.addActionListener(e -> btn.doClick());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(buildBackBar(), BorderLayout.NORTH);
        topBar.add(searchBar,      BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(topBar,                 BorderLayout.NORTH);
        wrapper.add(buildScrollPane(table), BorderLayout.CENTER);

        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ════════════════════════════════════════════
    //  SHARED TABLE BUILDER
    // ════════════════════════════════════════════
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TABLE_ROW1);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setGridColor(BORDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(TABLE_SELECT);
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(ACCENT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? TABLE_SELECT : (row % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2));
                setForeground(sel ? Color.WHITE : TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }
        });

        return table;
    }

    private JScrollPane buildScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(TABLE_ROW1);
        return scroll;
    }

    // ════════════════════════════════════════════
    //  BACK BUTTON HELPER
    // ════════════════════════════════════════════
    private JPanel buildBackBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JButton back = new JButton("← Back to Dashboard");
        back.setFont(new Font("Segoe UI", Font.BOLD, 13));
        back.setForeground(TEXT_SECONDARY);
        back.setBackground(BG_CARD2);
        back.setOpaque(true);
        back.setBorderPainted(true);
        back.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 16, 7, 16)));
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));

        back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                back.setBackground(new Color(34, 45, 75));
                back.setForeground(ACCENT);
                back.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1, true),
                        new EmptyBorder(7, 16, 7, 16)));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                back.setBackground(BG_CARD2);
                back.setForeground(TEXT_SECONDARY);
                back.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(7, 16, 7, 16)));
            }
        });
        back.addActionListener(e -> showHomeCards());

        bar.add(back);
        return bar;
    }

    // ════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════
    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledField(String text) {
        JTextField f = new JTextField(text);
        styleTextField(f);
        return f;
    }

    private void styleTextField(JTextField f) {
        f.setBackground(BG_CARD2);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton accentButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // ════════════════════════════════════════════
    //  ROUNDED PANEL
    // ════════════════════════════════════════════
    static class RoundedPanel extends JPanel {
        private int   radius;
        private Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg     = bg;
            setOpaque(false);
        }

        void setBgColor(Color c) { this.bg = c; }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RideAdminApp::new);
    }
}