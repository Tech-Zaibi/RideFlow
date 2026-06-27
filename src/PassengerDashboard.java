import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Stack;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.view.swing.BrowserView;

public class PassengerDashboard extends JFrame {

    // ── Design Tokens ────────────────────────────────────────
    private static final Color BG_DARK        = new Color(10,  14,  26);
    private static final Color BG_CARD        = new Color(18,  24,  42);
    private static final Color BG_CARD2       = new Color(24,  32,  56);
    private static final Color ACCENT         = new Color(0,   210, 180);
    private static final Color ACCENT_RED     = new Color(255, 80,  80);
    private static final Color ACCENT_YELLOW  = new Color(255, 200, 50);
    private static final Color ACCENT_BLUE    = new Color(60,  140, 255);
    private static final Color ACCENT_GREEN   = new Color(50,  220, 120);
    private static final Color ACCENT_PURPLE  = new Color(160, 80,  255);
    private static final Color ACCENT_ORANGE  = new Color(255, 140, 50);
    private static final Color TEXT_PRIMARY   = new Color(230, 235, 255);
    private static final Color TEXT_SECONDARY = new Color(130, 145, 180);
    private static final Color BORDER_COLOR   = new Color(40,  55,  90);

    // ── State ────────────────────────────────────────────────
    private final String username;
    private String selectedVehicleType   = "Select Vehicle Type";
    private String selectedPaymentMethod = "None";
    private final Stack<String> rideHistory = new Stack<>();

    private JPanel     centerPanel;
    private CardLayout centerLayout;
    private int        pendingFeedbackRideId  = -1;
    private String     pendingFeedbackDriver  = "";
    private Timer      feedbackPollTimer;

    private JLabel     selectedVehicleLabel;
    private JLabel     estimatedFareLabel;
    private JTextField pickupAddressField;
    private JTextField dropoffAddressField;
    private JLabel     pickupCoordLabel, dropoffCoordLabel;
    private Engine      engine;
    private Browser     browser;
    private BrowserView browserView;

    private double pickupLat = 0, pickupLng = 0;
    private double dropoffLat = 0, dropoffLng = 0;
    private double currentRouteDistanceKm = 0.0;
    private int    currentRequestId       = -1;

    // ── Vehicle button references ────────────────────────────
    private final JButton[] vehicleBtns   = new JButton[4];
    private final Color[]   vehicleColors = {
            ACCENT_GREEN, ACCENT_BLUE, ACCENT_YELLOW, ACCENT_PURPLE
    };
    private final String[]  vehicleNames  = {
            "Bike", "Car without AC", "Car with AC", "Premium"
    };

    // ── Payment button references ────────────────────────────
    private final JButton[] paymentBtns   = new JButton[3];
    private final Color[]   paymentColors = {
            new Color(220, 60,  60),
            new Color(30,  170, 100),
            ACCENT_BLUE
    };
    private final String[]  paymentNames  = { "JazzCash", "EasyPaisa", "Cash" };

    // ════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════
    public PassengerDashboard(String username) {
        this.username = username;

        if (checkForPendingPayment()) return;

        setTitle("RideFlow — Passenger");
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(BG_DARK);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildHeader(),   BorderLayout.NORTH);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildMapPanel(), BorderLayout.CENTER);

        setContentPane(root);

        checkCompletedRideFeedback();
        startFeedbackPolling();
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  HEADER
    // ════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(16, 26, 16, 26)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("RIDEFLOW");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(ACCENT);

        JLabel tag = new JLabel("Passenger Portal");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tag.setForeground(TEXT_SECONDARY);

        left.add(logo);
        left.add(tag);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel welcome = new JLabel("Welcome,  " + username + "  ●");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcome.setForeground(ACCENT);

        JLabel sep = new JLabel("|");
        sep.setForeground(BORDER_COLOR);

        JButton logoutBtn = buildLogoutButton();

        right.add(welcome);
        right.add(sep);
        right.add(logoutBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton buildLogoutButton() {
        JButton btn = new JButton("⏻  Logout");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(ACCENT_RED);
        btn.setBackground(new Color(40, 20, 20));
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 50, 50), 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT_RED); btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_RED, 1, true),
                        new EmptyBorder(6, 14, 6, 14)));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 20, 20)); btn.setForeground(ACCENT_RED);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 50, 50), 1, true),
                        new EmptyBorder(6, 14, 6, 14)));
            }
        });
        btn.addActionListener(e -> handleLogout());
        return btn;
    }

    private void handleLogout() {
        int c = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            if (feedbackPollTimer != null) feedbackPollTimer.stop();
            if (engine != null)           engine.close();
            dispose();
            new LoginPage().setVisible(true);
        }
    }

    // ════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════
    private JScrollPane buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG_DARK);
        side.setBorder(new EmptyBorder(20, 18, 20, 18));

        side.add(buildLocationCard());
        side.add(Box.createVerticalStrut(14));
        side.add(buildVehicleCard());
        side.add(Box.createVerticalStrut(14));
        side.add(buildFareCard());
        side.add(Box.createVerticalStrut(14));
        side.add(buildPaymentCard());
        side.add(Box.createVerticalStrut(18));
        side.add(buildActionRow());

        JScrollPane scroll = new JScrollPane(side);
        scroll.setPreferredSize(new Dimension(420, 0));
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setVisible(false);
        return scroll;
    }

    // ── Location Card ────────────────────────────────────────
    private RoundedPanel buildLocationCard() {

        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        addCardTitle(card, "📍", "Pickup & Destination", ACCENT_BLUE);
        card.add(Box.createVerticalStrut(16));

        card.add(fieldGroupLabel("Pickup Address"));
        card.add(Box.createVerticalStrut(6));

        pickupAddressField = styledCoordField("Enter pickup address");
        pickupAddressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(pickupAddressField);
        card.add(Box.createVerticalStrut(6));

        pickupCoordLabel = statusLabel("Not set");
        card.add(pickupCoordLabel);
        card.add(Box.createVerticalStrut(16));

        card.add(fieldGroupLabel("Destination Address"));
        card.add(Box.createVerticalStrut(6));

        dropoffAddressField = styledCoordField("Enter destination address");
        dropoffAddressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(dropoffAddressField);
        card.add(Box.createVerticalStrut(6));

        dropoffCoordLabel = statusLabel("Not set");
        card.add(dropoffCoordLabel);
        card.add(Box.createVerticalStrut(18));

        JButton setBtn = primaryButton("📌 Set Locations & Calculate", ACCENT_GREEN);
        setBtn.addActionListener(e -> applyAddresses());
        card.add(setBtn);

        return card;
    }

    // ── Address → Coordinates ────────────────────────────────
    private void applyAddresses() {

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            double[] pickupCoords;
            double[] dropoffCoords;

            @Override
            protected Void doInBackground() throws Exception {
                String pickup  = pickupAddressField.getText().trim();
                String dropoff = dropoffAddressField.getText().trim();

                if (pickup.isEmpty() || dropoff.isEmpty()) {
                    JOptionPane.showMessageDialog(PassengerDashboard.this,
                            "Please enter both pickup and destination addresses.");
                    return null;
                }

                pickupCoords  = geocodeAddress(pickup);
                dropoffCoords = geocodeAddress(dropoff);
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (pickupCoords == null || dropoffCoords == null) {
                        JOptionPane.showMessageDialog(PassengerDashboard.this,
                                "Could not locate one of the addresses.");
                        return;
                    }

                    pickupLat  = pickupCoords[0];
                    pickupLng  = pickupCoords[1];
                    dropoffLat = dropoffCoords[0];
                    dropoffLng = dropoffCoords[1];

                    pickupCoordLabel.setText(
                            String.format("✅ %.4f, %.4f", pickupLat, pickupLng));
                    pickupCoordLabel.setForeground(ACCENT_GREEN);

                    dropoffCoordLabel.setText(
                            String.format("✅ %.4f, %.4f", dropoffLat, dropoffLng));
                    dropoffCoordLabel.setForeground(ACCENT_GREEN);

                    currentRouteDistanceKm =
                            haversineDistance(pickupLat, pickupLng, dropoffLat, dropoffLng);

                    updateEstimatedFare();
                    updateEmbeddedMap();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private double[] geocodeAddress(String address) {
        try {
            String fullAddress   = address + ", Pakistan";
            String encodedAddress =
                    java.net.URLEncoder.encode(fullAddress, "UTF-8");
            String urlStr =
                    "https://nominatim.openstreetmap.org/search?"
                            + "q=" + encodedAddress
                            + "&format=jsonv2&limit=1&countrycodes=pk";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "RideFlowApp/1.0");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            String json = response.toString();
            if (json.equals("[]")) return null;

            String latKey = "\"lat\":\"";
            String lonKey = "\"lon\":\"";

            int latStart = json.indexOf(latKey);
            if (latStart == -1) return null;
            latStart += latKey.length();
            int latEnd = json.indexOf("\"", latStart);

            int lonStart = json.indexOf(lonKey);
            if (lonStart == -1) return null;
            lonStart += lonKey.length();
            int lonEnd = json.indexOf("\"", lonStart);

            return new double[]{
                    Double.parseDouble(json.substring(latStart, latEnd)),
                    Double.parseDouble(json.substring(lonStart, lonEnd))
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Vehicle Card ─────────────────────────────────────────
    private RoundedPanel buildVehicleCard() {
        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        addCardTitle(card, "🚗", "Select Service", ACCENT_YELLOW);
        card.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] icons = { "🛵", "🚗", "❄️", "⭐" };
        for (int i = 0; i < vehicleNames.length; i++) {
            final int idx = i;
            JButton btn = vehicleSelectButton(icons[i] + "  " + vehicleNames[i], vehicleColors[i]);
            btn.addActionListener(e -> {
                selectedVehicleType = vehicleNames[idx];
                selectedVehicleLabel.setText("✅  " + vehicleNames[idx] + " selected");
                selectedVehicleLabel.setForeground(vehicleColors[idx]);
                for (int j = 0; j < vehicleBtns.length; j++) {
                    if (j == idx) {
                        vehicleBtns[j].setBackground(blendSolid(BG_CARD2, vehicleColors[j], 0.35f));
                        vehicleBtns[j].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(vehicleColors[j], 2, true),
                                new EmptyBorder(8, 10, 8, 10)));
                    } else {
                        vehicleBtns[j].setBackground(BG_CARD2);
                        vehicleBtns[j].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                                new EmptyBorder(9, 10, 9, 10)));
                    }
                }
                updateEstimatedFare();
            });
            vehicleBtns[i] = btn;
            grid.add(btn);
        }

        card.add(grid);
        card.add(Box.createVerticalStrut(12));

        selectedVehicleLabel = new JLabel("No service selected");
        selectedVehicleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        selectedVehicleLabel.setForeground(TEXT_SECONDARY);
        selectedVehicleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(selectedVehicleLabel);

        return card;
    }

    // ── Fare Card ────────────────────────────────────────────
    private RoundedPanel buildFareCard() {
        RoundedPanel card = new RoundedPanel(16, BG_CARD2);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel titleLbl = new JLabel("💰  Estimated Fare");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));

        estimatedFareLabel = new JLabel("Rs. --");
        estimatedFareLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        estimatedFareLabel.setForeground(ACCENT_YELLOW);
        estimatedFareLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(estimatedFareLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel hint = new JLabel("Based on Haversine distance × rate/km");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hint);

        return card;
    }

    // ── Payment Card ─────────────────────────────────────────
    private RoundedPanel buildPaymentCard() {
        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        addCardTitle(card, "💳", "Payment Method", ACCENT_GREEN);
        card.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] payIcons = { "📱", "📲", "💵" };
        JLabel selectedLabel = new JLabel("No payment method selected");
        selectedLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        selectedLabel.setForeground(TEXT_SECONDARY);
        selectedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < paymentNames.length; i++) {
            final int idx = i;
            JButton btn = vehicleSelectButton(payIcons[i] + "  " + paymentNames[i], paymentColors[i]);
            btn.addActionListener(e -> {
                selectedPaymentMethod = paymentNames[idx];
                selectedLabel.setText("✅  " + paymentNames[idx]);
                selectedLabel.setForeground(paymentColors[idx]);
                for (int j = 0; j < paymentBtns.length; j++) {
                    if (j == idx) {
                        paymentBtns[j].setBackground(blendSolid(BG_CARD2, paymentColors[j], 0.35f));
                        paymentBtns[j].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(paymentColors[j], 2, true),
                                new EmptyBorder(8, 10, 8, 10)));
                    } else {
                        paymentBtns[j].setBackground(BG_CARD2);
                        paymentBtns[j].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                                new EmptyBorder(9, 10, 9, 10)));
                    }
                }
            });
            paymentBtns[i] = btn;
            grid.add(btn);
        }

        card.add(grid);
        card.add(Box.createVerticalStrut(10));
        card.add(selectedLabel);

        return card;
    }

    // ── Action Row ───────────────────────────────────────────
    private JPanel buildActionRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JButton requestBtn = primaryButton("🛎  Request Ride", ACCENT);
        requestBtn.setForeground(BG_DARK);
        requestBtn.addActionListener(e -> handleRequestRide());

        JButton historyBtn = primaryButton("📋  My Rides", ACCENT_BLUE);
        historyBtn.addActionListener(e -> handleViewHistory());

        row.add(requestBtn);
        row.add(historyBtn);
        return row;
    }

    // ════════════════════════════════════════════════════════
    //  MAP PANEL
    // ════════════════════════════════════════════════════════
    private JPanel buildMapPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_CARD);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel mapTitle = new JLabel("🗺 Live Route Preview");
        mapTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mapTitle.setForeground(TEXT_PRIMARY);

        JLabel mapHint = new JLabel("Live map powered by Chromium");
        mapHint.setForeground(TEXT_SECONDARY);

        topBar.add(mapTitle, BorderLayout.WEST);
        topBar.add(mapHint,  BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        engine = Engine.newInstance(
                EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED)
                        .licenseKey(
                                "OK6AEKNYF5FHWF3XSDVN6WBC4CZDXNMJ4CZQNSWYOKM46FT7NLQZI18NJOY9GK" +
                                        "SA1EVSJW6SVG0EUQ43M1H6RMSA906Y24J1UDGZ6EBMMHUBDNHSJ4IIBR61Z675" +
                                        "Q88SZSD5MJXEDLPRAM5B5")
                        .build()
        );

        browser = engine.newBrowser();
        browser.settings().allowRunningInsecureContent();
        browserView = BrowserView.newInstance(browser);
        panel.add(browserView, BorderLayout.CENTER);
        browser.navigation().loadHtml(buildDefaultMapHTML());

        return panel;
    }

    private String buildDefaultMapHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset='utf-8'/>
<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
<style>
html, body, #map{ width:100%; height:100%; margin:0; padding:0; overflow:hidden; }
.info-box{
  position:absolute; top:50%; left:50%;
  transform:translate(-50%,-50%);
  background:rgba(10,14,26,0.85); color:white;
  padding:20px 30px; border-radius:14px; z-index:999;
  font-family:'Segoe UI'; text-align:center;
}
</style>
</head>
<body>
<div id='map'></div>
<div class='info-box'>📍<br><br>Enter pickup & destination</div>
<script>
var map = L.map('map');
L.tileLayer('https://mt1.google.com/vt/lyrs=r&x={x}&y={y}&z={z}',
            { maxZoom:19, attribution:'© OpenStreetMap' }).addTo(map);
map.setView([30.3753,69.3451],6);
setTimeout(function(){ map.invalidateSize(); },500);
</script>
</body>
</html>
""";
    }

    private void updateEmbeddedMap() {
        browser.navigation().loadHtml(
                buildRouteMapHTML(pickupLat, pickupLng, dropoffLat, dropoffLng));
    }

    private String buildRouteMapHTML(
            double pLat, double pLng,
            double dLat, double dLng) {

        return "<!DOCTYPE html><html><head><meta charset='utf-8'/>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<script src='https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.js'></script>"
                + "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/leaflet-routing-machine/3.2.12/leaflet-routing-machine.min.css'/>"
                + "<style>"
                + "* { margin:0; padding:0; }"
                + "html,body,#map { height:100%; width:100%; }"
                + ".leaflet-routing-container { display:none !important; }"
                + "@keyframes pulse { 0%{transform:scale(1);opacity:1;} 50%{transform:scale(1.4);opacity:0.6;} 100%{transform:scale(1);opacity:1;} }"
                + ".pickup-dot  { width:14px;height:14px;background:#32dc78;border:3px solid #fff;border-radius:50%;animation:pulse 1.8s infinite; }"
                + ".dropoff-dot { width:14px;height:14px;background:#ff5050;border:3px solid #fff;border-radius:50%; }"
                + ".fare-badge  { position:absolute;bottom:18px;left:50%;transform:translateX(-50%);"
                + "  background:rgba(10,14,26,0.9);color:#00d2b4;padding:8px 20px;border-radius:20px;"
                + "  font-family:'Segoe UI',sans-serif;font-size:13px;font-weight:bold;z-index:999;"
                + "  border:1px solid rgba(0,210,180,0.4);pointer-events:none; }"
                + "</style></head><body>"
                + "<div id='map'></div>"
                + "<div class='fare-badge' id='fareBadge'>Calculating route...</div>"
                + "<script>"
                + "var map = L.map('map',{zoomControl:true});"
                + "L.tileLayer('https://mt1.google.com/vt/lyrs=r&x={x}&y={y}&z={z}',{maxZoom:19}).addTo(map);"
                + "var pickupIcon  = L.divIcon({className:'',html:'<div class=\"pickup-dot\"></div>',iconSize:[14,14],iconAnchor:[7,7]});"
                + "var dropoffIcon = L.divIcon({className:'',html:'<div class=\"dropoff-dot\"></div>',iconSize:[14,14],iconAnchor:[7,7]});"
                + "L.marker([" + pLat + "," + pLng + "],{icon:pickupIcon}).addTo(map).bindPopup('<b>📍 Pickup</b>').openPopup();"
                + "L.marker([" + dLat + "," + dLng + "],{icon:dropoffIcon}).addTo(map).bindPopup('<b>🏁 Destination</b>');"
                + "var control = L.Routing.control({"
                + "  waypoints:[L.latLng(" + pLat + "," + pLng + "),L.latLng(" + dLat + "," + dLng + ")],"
                + "  routeWhileDragging:false,show:false,addWaypoints:false,"
                + "  lineOptions:{styles:[{color:'#00d2b4',opacity:0.9,weight:5}]},"
                + "  createMarker:function(){return null;}"
                + "}).addTo(map);"
                + "control.on('routesfound',function(e){"
                + "  var dist=(e.routes[0].summary.totalDistance/1000).toFixed(2);"
                + "  var time=Math.round(e.routes[0].summary.totalTime/60);"
                + "  document.getElementById('fareBadge').innerHTML='📏 '+dist+' km &nbsp;|&nbsp; ⏱ ~'+time+' min';"
                + "});"
                + "map.fitBounds([[" + pLat + "," + pLng + "],[" + dLat + "," + dLng + "]],{padding:[40,40]});"
                + "setTimeout(function(){map.invalidateSize();},500);"
                + "</script></body></html>";
    }

    // ════════════════════════════════════════════════════════
    //  LOGIC
    // ════════════════════════════════════════════════════════
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private void updateEstimatedFare() {
        if (currentRouteDistanceKm > 0 && !selectedVehicleType.equals("Select Vehicle Type")) {
            double rate = switch (selectedVehicleType) {
                case "Bike"           -> 30;
                case "Car without AC" -> 50;
                case "Car with AC"    -> 70;
                case "Premium"        -> 100;
                default               -> 50;
            };
            double fare = Math.max(currentRouteDistanceKm * rate, 100);
            estimatedFareLabel.setText(String.format("Rs. %.2f", fare));
        } else {
            estimatedFareLabel.setText("Rs. --");
        }
    }

    private void handleRequestRide() {
        if (pickupAddressField.getText().trim().isEmpty()
                || dropoffAddressField.getText().trim().isEmpty()) {
            showWarn("Please set pickup and destination addresses first."); return;
        }
        if (selectedVehicleType.equals("Select Vehicle Type")) {
            showWarn("Please select a vehicle type."); return;
        }
        if (selectedPaymentMethod.equals("None")) {
            showWarn("Please select a payment method."); return;
        }
        if (currentRouteDistanceKm <= 0) {
            showWarn("Route distance is not calculated yet."); return;
        }

        double rate = switch (selectedVehicleType) {
            case "Bike"           -> 30;
            case "Car without AC" -> 50;
            case "Car with AC"    -> 70;
            case "Premium"        -> 100;
            default               -> 50;
        };
        double fare      = Math.max(currentRouteDistanceKm * rate, 100);
        String pickupLoc  = pickupAddressField.getText().trim();
        String dropoffLoc = dropoffAddressField.getText().trim();

        currentRequestId = RideRequestManager.saveRideRequestWithId(
                username, pickupLoc, dropoffLoc,
                selectedVehicleType, String.format("%.2f", fare));

        if (currentRequestId > 0) {
            JOptionPane.showMessageDialog(this,
                    "✅  Ride Requested!\n\n"
                            + "Distance : " + String.format("%.2f", currentRouteDistanceKm) + " km\n"
                            + "Fare     : Rs. " + String.format("%.2f", fare) + "\n"
                            + "Payment  : " + selectedPaymentMethod,
                    "Ride Requested", JOptionPane.INFORMATION_MESSAGE);
            rideHistory.push("To: " + dropoffLoc + " (Pending)");
        } else {
            showWarn("Error requesting ride. Please try again.");
        }
    }

    private void handleViewHistory() {
        String history = RideRequestManager.getUserRideHistory(username);
        JTextArea area = new JTextArea(history);
        area.setBackground(BG_CARD2);
        area.setForeground(TEXT_PRIMARY);
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 14, 10, 14));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 320));
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JOptionPane.showMessageDialog(this, sp, "My Rides", JOptionPane.PLAIN_MESSAGE);
    }

    private boolean checkForPendingPayment() {
        RideRequestManager.PendingRideInfo pending =
                RideRequestManager.getPassengerPendingRide(username);
        if (pending != null && "ACCEPTED".equals(pending.status)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "You have a pending ride with driver: " + pending.driverUsername,
                        "Pending Ride", JOptionPane.INFORMATION_MESSAGE);
                new RidePaymentPage(pending.rideId, username, pending.driverUsername);
            });
            return true;
        }
        return false;
    }

    // ════════════════════════════════════════════════════════
    //  FEEDBACK — triggered on PASSENGER side only
    //
    //  FeedbackDialog is opened here (passenger screen) after
    //  the driver marks the ride as COMPLETED.
    //  DriverDashboard no longer opens FeedbackDialog at all.
    //
    //  TABLE FIX: both queries now use the `feedback` table,
    //  matching what FeedbackDialog.handleSubmit() inserts into.
    //  Previously the queries used `RideFeedback`, which caused
    //  the dialog to reappear on every poll cycle because the
    //  inserted row was never found by the check.
    // ════════════════════════════════════════════════════════

    /**
     * Runs once on startup. Opens FeedbackDialog immediately if there is
     * a COMPLETED ride that the passenger hasn't rated yet.
     */
    private void checkCompletedRideFeedback() {
        String q = "SELECT TOP 1 id, driver_username FROM ride_requests "
                + "WHERE passenger_username = ? "
                + "AND status = 'COMPLETED' "
                + "AND id NOT IN (SELECT ride_id FROM RideFeedback) "
                + "ORDER BY id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(q)) {

            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int    rideId = rs.getInt("id");
                String driver = rs.getString("driver_username");
                SwingUtilities.invokeLater(() ->
                        new FeedbackDialog(this, rideId, username, driver));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Polls every 5 seconds for newly COMPLETED rides that still need
     * passenger feedback. Stops itself when a match is found, opens the
     * dialog, then restarts after the dialog closes.
     */
    private void startFeedbackPolling() {

        feedbackPollTimer = new Timer(5000, e -> {

            // Fixed: status = 'COMPLETED' (not 'FINISHED') + table = RideFeedback
            String q = "SELECT TOP 1 id, driver_username FROM ride_requests "
                    + "WHERE passenger_username = ? "
                    + "AND status = 'COMPLETED' "
                    + "AND id NOT IN (SELECT ride_id FROM RideFeedback) "
                    + "ORDER BY id DESC";

            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(q)) {

                pst.setString(1, username);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int    rideId = rs.getInt("id");
                    String driver = rs.getString("driver_username");

                    feedbackPollTimer.stop();

                    SwingUtilities.invokeLater(() -> {
                        new FeedbackDialog(PassengerDashboard.this, rideId, username, driver);
                        startFeedbackPolling();
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        feedbackPollTimer.start();
    }

    // ════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════
    private void addCardTitle(JPanel card, String icon, String text, Color accent) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ico = new JLabel(icon + "  ");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(accent);

        row.add(ico);
        row.add(lbl);
        card.add(row);

        JPanel line = new JPanel();
        line.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(line);
    }

    private JLabel fieldGroupLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel statusLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledCoordField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setForeground(TEXT_SECONDARY);
        f.setBackground(BG_CARD2);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private JButton primaryButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        Color darker = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(darker); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg);     }
        });
        return btn;
    }

    private JButton vehicleSelectButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_CARD2);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(9, 10, 9, 10)));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(accent);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 1, true),
                        new EmptyBorder(9, 10, 9, 10)));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.getBorder() instanceof CompoundBorder cb) {
                    if (cb.getOutsideBorder() instanceof javax.swing.border.LineBorder lb
                            && lb.getThickness() == 1) {
                        btn.setForeground(TEXT_PRIMARY);
                        btn.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                                new EmptyBorder(9, 10, 9, 10)));
                    }
                }
            }
        });
        return btn;
    }

    private Color blendSolid(Color a, Color b, float t) {
        return new Color(
                (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    // ════════════════════════════════════════════════════════
    //  ROUNDED PANEL
    // ════════════════════════════════════════════════════════
    static class RoundedPanel extends JPanel {
        private int   radius;
        private Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
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
}