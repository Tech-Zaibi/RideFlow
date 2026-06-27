import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.view.swing.BrowserView;

public class DriverDashboard extends JFrame {

    private String username;
    private String driverVehicleType;
    private LinkedList<String> rideHistory;
    private JPanel availableRidesPanel;
    private JLabel statusLabel;
    private JLabel statusDot;
    private JLabel rideCountLabel;
    private boolean hasActiveRide = false;
    private int currentRideId = -1;
    private Timer refreshTimer;
    private int totalRidesCompleted = 0;

    // =========================================================
    // JxBrowser Fields
    // =========================================================
    private Engine      engine;
    private Browser     browser;
    private BrowserView browserView;

    // =========================================================
    // COLORS
    // =========================================================
    private static final Color BG_DARK        = new Color(10,  14,  26);
    private static final Color BG_CARD        = new Color(18,  24,  42);
    private static final Color BG_CARD2       = new Color(24,  32,  56);
    private static final Color ACCENT         = new Color(0,   210, 180);
    private static final Color ACCENT_DIM     = new Color(0,   150, 130);
    private static final Color ACCENT_RED     = new Color(255, 80,  80);
    private static final Color ACCENT_YELLOW  = new Color(255, 200, 50);
    private static final Color ACCENT_GREEN   = new Color(50,  220, 120);
    private static final Color TEXT_PRIMARY   = new Color(230, 235, 255);
    private static final Color TEXT_SECONDARY = new Color(130, 145, 180);
    private static final Color BORDER_COLOR   = new Color(40,  55,  90);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public DriverDashboard(String username, String vehicleType) {

        this.username          = username;
        this.driverVehicleType = vehicleType;
        this.rideHistory       = new LinkedList<>();
        this.hasActiveRide     = RideManager.hasActiveRide(username);

        if (hasActiveRide) {
            this.currentRideId = RideManager.getActiveRideId(username);
        }

        setTitle("RideFlow — Driver Dashboard");
        setSize(1440, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(BG_DARK);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(createSidebar(),     BorderLayout.WEST);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.add(createMapPanel(),    BorderLayout.EAST);
        add(root);

        startAutoRefresh();
        setVisible(true);
    }

    // =========================================================
    // SIDEBAR
    // =========================================================
    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_CARD);
        sidebar.setPreferredSize(new Dimension(260, 720));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(createLogoPanel());
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(createDriverInfoCard());
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createStatsCard());
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createLogoutBtn());
        sidebar.add(Box.createVerticalStrut(25));

        return sidebar;
    }

    private JPanel createLogoPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel logo = new JLabel("RIDEFLOW");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Driver Console");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(logo);
        panel.add(Box.createVerticalStrut(3));
        panel.add(sub);
        return panel;
    }

    private JPanel createDriverInfoCard() {

        RoundedPanel card = new RoundedPanel(14, BG_CARD2);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 40, 18, 38));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(320, 160));

        JLabel avatar = new JLabel(
                String.valueOf(username.charAt(0)).toUpperCase(),
                SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        avatar.setForeground(BG_DARK);
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(48, 48));
        avatar.setMaximumSize(new Dimension(48, 48));
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel vehicleLabel = new JLabel("🚗  " + driverVehicleType);
        vehicleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vehicleLabel.setForeground(TEXT_SECONDARY);
        vehicleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusDot = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusDot.setForeground(hasActiveRide ? ACCENT_YELLOW : ACCENT_GREEN);

        statusLabel = new JLabel(hasActiveRide ? "On Trip" : "Online");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(hasActiveRide ? ACCENT_YELLOW : ACCENT_GREEN);

        statusRow.add(statusDot);
        statusRow.add(statusLabel);

        card.add(avatar);
        card.add(Box.createVerticalStrut(12));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(vehicleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(statusRow);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card);
        return wrapper;
    }

    private JPanel createStatsCard() {

        RoundedPanel card = new RoundedPanel(14, BG_CARD2);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 25));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(220, 120));

        JLabel title = new JLabel("STATS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setForeground(TEXT_SECONDARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        rideCountLabel = createStatRow(card, "Rides Today ", "0");
        createStatRowLabel(card, "Vehicle ", driverVehicleType);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card);
        return wrapper;
    }

    private JLabel createStatRow(JPanel parent, String label, String value) {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(ACCENT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
        parent.add(Box.createVerticalStrut(6));
        return val;
    }

    private void createStatRowLabel(JPanel parent, String label, String value) {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
    }

    private JPanel createLogoutBtn() {

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        wrapper.setOpaque(false);

        JButton btn = new JButton("⏻   Logout");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(ACCENT_RED);
        btn.setBackground(new Color(255, 80, 80, 30));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 80, 80, 80), 1, true),
                new EmptyBorder(8, 18, 8, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addActionListener(e -> {
            if (refreshTimer != null) refreshTimer.stop();
            if (engine != null)       engine.close();
            dispose();
            new LoginPage().setVisible(true);
        });

        wrapper.add(btn);
        return wrapper;
    }

    // =========================================================
    // MAIN CONTENT
    // =========================================================
    private JPanel createMainContent() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.add(createTopBar(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(createRidesArea());
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopBar() {

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(28, 30, 16, 30));

        JLabel heading = new JLabel("Available Rides");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(TEXT_PRIMARY);

        JLabel refresh = new JLabel("● Auto-refresh 5s");
        refresh.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        refresh.setForeground(ACCENT_DIM);

        bar.add(heading, BorderLayout.WEST);
        bar.add(refresh, BorderLayout.EAST);
        return bar;
    }

    private JPanel createRidesArea() {

        availableRidesPanel = new JPanel();
        availableRidesPanel.setLayout(
                new BoxLayout(availableRidesPanel, BoxLayout.Y_AXIS));
        availableRidesPanel.setBackground(BG_DARK);
        availableRidesPanel.setBorder(new EmptyBorder(0, 30, 30, 30));
        refreshRidesPanel();
        return availableRidesPanel;
    }

    // =========================================================
    // MAP PANEL — JxBrowser embedded
    // =========================================================
    private JPanel createMapPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setPreferredSize(new Dimension(480, 0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_CARD);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel mapTitle = new JLabel("🗺  Live Route Preview");
        mapTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mapTitle.setForeground(TEXT_PRIMARY);

        JLabel mapHint = new JLabel("Powered by Chromium");
        mapHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

    // =========================================================
    // DEFAULT MAP HTML
    // =========================================================
    private String buildDefaultMapHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset='utf-8'/>
<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
<style>
  html, body, #map { width:100%; height:100%; margin:0; padding:0; overflow:hidden; }
  .info-box {
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
<div class='info-box'>🗺️<br><br>Press the Map button<br>on a ride card to preview the route.</div>
<script>
  var map = L.map('map');
  L.tileLayer('https://mt1.google.com/vt/lyrs=r&x={x}&y={y}&z={z}',
              { maxZoom:19, attribution:'© OpenStreetMap' }).addTo(map);
  map.setView([30.3753, 69.3451], 6);
  setTimeout(function(){ map.invalidateSize(); }, 500);
</script>
</body>
</html>
""";
    }

    // =========================================================
    // ROUTE MAP HTML
    // =========================================================
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
                + "  background:rgba(10,14,26,0.9);color:#00d2b4;padding:8px 20px;"
                + "  border-radius:20px;font-family:'Segoe UI',sans-serif;font-size:13px;"
                + "  font-weight:bold;z-index:999;border:1px solid rgba(0,210,180,0.4);pointer-events:none; }"
                + "</style></head><body>"
                + "<div id='map'></div>"
                + "<div class='fare-badge' id='badge'>Calculating route…</div>"
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
                + "  document.getElementById('badge').innerHTML='📏 '+dist+' km &nbsp;|&nbsp; ⏱ ~'+time+' min';"
                + "});"
                + "map.fitBounds([[" + pLat + "," + pLng + "],[" + dLat + "," + dLng + "]],{padding:[40,40]});"
                + "setTimeout(function(){map.invalidateSize();},500);"
                + "</script></body></html>";
    }

    // =========================================================
    // UPDATE EMBEDDED MAP
    // =========================================================
    private void updateEmbeddedMap(String pickup, String dropoff) {

        if (browser != null) {
            browser.navigation().loadHtml(
                    "<html><body style='background:#0a0e1a;color:#00d2b4;"
                            + "font-family:Segoe UI;display:flex;align-items:center;"
                            + "justify-content:center;height:100%;margin:0;'>"
                            + "<div style='text-align:center'>"
                            + "<div style='font-size:32px'>🗺️</div>"
                            + "<div style='margin-top:12px;font-size:14px'>Geocoding addresses…</div>"
                            + "</div></body></html>"
            );
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            double[] pickupCoords;
            double[] dropoffCoords;

            @Override
            protected Void doInBackground() {
                pickupCoords  = parseCoords(pickup);
                dropoffCoords = parseCoords(dropoff);
                return null;
            }

            @Override
            protected void done() {
                if (pickupCoords == null || dropoffCoords == null) {
                    JOptionPane.showMessageDialog(DriverDashboard.this,
                            "Could not locate one of the addresses on the map.");
                    return;
                }
                if (browser != null) {
                    browser.navigation().loadHtml(
                            buildRouteMapHTML(
                                    pickupCoords[0], pickupCoords[1],
                                    dropoffCoords[0], dropoffCoords[1]));
                }
            }
        };

        worker.execute();
    }

    // =========================================================
    // AUTO REFRESH
    // =========================================================
    private void startAutoRefresh() {

        refreshTimer = new Timer(5000, e -> {
            hasActiveRide = RideManager.hasActiveRide(username);
            updateStatus();
            refreshRidesPanel();
        });
        refreshTimer.start();
    }

    private void updateStatus() {

        if (statusLabel == null || statusDot == null) return;
        if (hasActiveRide) {
            statusLabel.setText("On Trip");
            statusLabel.setForeground(ACCENT_YELLOW);
            statusDot.setForeground(ACCENT_YELLOW);
        } else {
            statusLabel.setText("Online");
            statusLabel.setForeground(ACCENT_GREEN);
            statusDot.setForeground(ACCENT_GREEN);
        }
    }

    // =========================================================
    // REFRESH RIDES
    // =========================================================
    private void refreshRidesPanel() {

        if (availableRidesPanel == null) return;
        availableRidesPanel.removeAll();

        if (hasActiveRide && currentRideId != -1) {
            RideManager.RideRequest activeRide =
                    RideManager.getActiveRideDetails(currentRideId);

            if (activeRide != null) {
                availableRidesPanel.add(createActiveRideCard(activeRide));
            } else {
                // DB not ready yet (race condition after accept) — show loading state.
                // The 5s timer will retry and load the real card shortly.
                JPanel loadingPanel = new JPanel();
                loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
                loadingPanel.setOpaque(false);
                loadingPanel.setBorder(new EmptyBorder(60, 0, 0, 0));

                JLabel icon = new JLabel("⏳");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
                icon.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel msg = new JLabel("Loading active ride...");
                msg.setFont(new Font("Segoe UI", Font.BOLD, 16));
                msg.setForeground(TEXT_PRIMARY);
                msg.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel sub = new JLabel("This will refresh automatically");
                sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                sub.setForeground(TEXT_SECONDARY);
                sub.setAlignmentX(Component.CENTER_ALIGNMENT);

                loadingPanel.add(icon);
                loadingPanel.add(Box.createVerticalStrut(14));
                loadingPanel.add(msg);
                loadingPanel.add(Box.createVerticalStrut(6));
                loadingPanel.add(sub);

                availableRidesPanel.add(loadingPanel);
            }
        } else {
            List<RideManager.RideRequest> requests =
                    RideManager.getPendingRequestsByVehicleType(driverVehicleType);

            if (requests.isEmpty()) {
                availableRidesPanel.add(createEmptyState());
            } else {
                for (RideManager.RideRequest r : requests) {
                    availableRidesPanel.add(Box.createVerticalStrut(12));
                    availableRidesPanel.add(createRideCard(r));
                }
            }
        }

        availableRidesPanel.revalidate();
        availableRidesPanel.repaint();
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================
    private JPanel createEmptyState() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(60, 0, 0, 0));

        JLabel icon = new JLabel("🛣️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("No ride requests yet");
        msg.setFont(new Font("Segoe UI", Font.BOLD, 18));
        msg.setForeground(TEXT_PRIMARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("New requests will appear here automatically");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(16));
        panel.add(msg);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sub);
        return panel;
    }

    // =========================================================
    // RIDE CARD
    // =========================================================
    private JPanel createRideCard(RideManager.RideRequest request) {

        RoundedPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel info = new JPanel(new GridLayout(4, 1, 0, 4));
        info.setOpaque(false);

        JLabel passenger = new JLabel("👤   " + request.passengerUsername);
        passenger.setFont(new Font("Segoe UI", Font.BOLD, 15));
        passenger.setForeground(TEXT_PRIMARY);

        JLabel pickup = new JLabel("📍   " + request.pickupLocation);
        pickup.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pickup.setForeground(TEXT_SECONDARY);

        JLabel dropoff = new JLabel("🏁   " + request.destinationLocation);
        dropoff.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dropoff.setForeground(TEXT_SECONDARY);

        JLabel fare = new JLabel("💰   " + request.estimatedFare);
        fare.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fare.setForeground(ACCENT);

        info.add(passenger);
        info.add(pickup);
        info.add(dropoff);
        info.add(fare);

        JPanel btns = new JPanel(new GridLayout(2, 1, 0, 8));
        btns.setOpaque(false);
        btns.setPreferredSize(new Dimension(120, 90));

        JButton acceptBtn = makeBtn("✓  Accept", ACCENT_GREEN, BG_DARK);
        acceptBtn.addActionListener(e -> {
            if (RideManager.acceptRide(request.requestId, username)) {
                hasActiveRide = true;
                currentRideId = request.requestId;

                updateStatus();

                // Immediately render active ride card using the already-loaded
                // request object — avoids DB round-trip race condition
                availableRidesPanel.removeAll();
                availableRidesPanel.add(createActiveRideCard(request));
                availableRidesPanel.revalidate();
                availableRidesPanel.repaint();

                JOptionPane.showMessageDialog(
                        DriverDashboard.this, "✅ Ride Accepted!");
                // Do NOT call refreshRidesPanel() here.
                // The auto-refresh timer will sync state within 5 seconds.
            } else {
                JOptionPane.showMessageDialog(
                        DriverDashboard.this, "Could not accept ride. It may have been taken.");
            }
        });

        JButton mapBtn = makeBtn("🗺  Map", ACCENT, BG_DARK);
        mapBtn.addActionListener(e ->
                updateEmbeddedMap(request.pickupLocation, request.destinationLocation));

        btns.add(acceptBtn);
        btns.add(mapBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // =========================================================
    // ACTIVE RIDE CARD
    // =========================================================
    private JPanel createActiveRideCard(RideManager.RideRequest ride) {

        RoundedPanel card = new RoundedPanel(16, new Color(0, 60, 50));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel badge = new JLabel("● ACTIVE RIDE");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT_GREEN);

        JLabel title = new JLabel("Trip in Progress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JLabel passenger = new JLabel("👤 Passenger: " + ride.passengerUsername);
        passenger.setForeground(TEXT_PRIMARY);

        JLabel pickup = new JLabel("📍 From: " + ride.pickupLocation);
        pickup.setForeground(TEXT_SECONDARY);

        JLabel dropoff = new JLabel("🏁 To: " + ride.destinationLocation);
        dropoff.setForeground(TEXT_SECONDARY);

        JLabel fare = new JLabel("💰 Fare: " + ride.estimatedFare);
        fare.setForeground(ACCENT_YELLOW);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        JButton mapBtn = makeBtn("🗺 Preview Route", ACCENT, BG_DARK);
        mapBtn.addActionListener(e ->
                updateEmbeddedMap(ride.pickupLocation, ride.destinationLocation));

        JButton finishBtn = makeBtn("✓ Finish Ride", ACCENT_RED, Color.WHITE);
        finishBtn.addActionListener(e -> {

            if (RideManager.finishRide(currentRideId)) {

                hasActiveRide = false;
                currentRideId = -1;
                updateStatus();

                // Reset embedded map to idle state
                if (browser != null) {
                    browser.navigation().loadHtml(buildDefaultMapHTML());
                }

                // ─────────────────────────────────────────────────────────────
                // FeedbackDialog is intentionally NOT opened here.
                // PassengerDashboard's startFeedbackPolling() timer detects
                // the COMPLETED status every 5 s and opens FeedbackDialog
                // automatically on the passenger's own screen.
                // ─────────────────────────────────────────────────────────────

                JOptionPane.showMessageDialog(
                        DriverDashboard.this,
                        "✅  Ride finished!\nThe passenger will be prompted to rate the trip."
                );

                refreshRidesPanel();
            }
        });

        btnRow.add(mapBtn);
        btnRow.add(finishBtn);

        card.add(badge);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(passenger);
        card.add(Box.createVerticalStrut(6));
        card.add(pickup);
        card.add(Box.createVerticalStrut(4));
        card.add(dropoff);
        card.add(Box.createVerticalStrut(8));
        card.add(fare);
        card.add(Box.createVerticalStrut(16));
        card.add(btnRow);

        return card;
    }

    // =========================================================
    // GEOCODING
    // =========================================================
    private double[] parseCoords(String location) {
        try {
            if (location.matches("-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?")) {
                String[] parts = location.split(",");
                return new double[]{
                        Double.parseDouble(parts[0].trim()),
                        Double.parseDouble(parts[1].trim())
                };
            }
            return geocodeAddress(location);
        } catch (Exception e) {
            e.printStackTrace();
            return new double[]{ 33.6844, 73.0479 };
        }
    }

    private double[] geocodeAddress(String address) {
        try {
            String fullAddress = address + ", Pakistan";
            String encoded     = java.net.URLEncoder.encode(fullAddress, "UTF-8");
            String urlStr      = "https://nominatim.openstreetmap.org/search?q=" + encoded
                    + "&format=json&limit=1&countrycodes=pk";

            URL               url  = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent",      "RideFlowApp/1.0");
            conn.setRequestProperty("Accept-Language", "en");

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
            int latStart  = json.indexOf(latKey) + latKey.length();
            int latEnd    = json.indexOf("\"", latStart);
            int lonStart  = json.indexOf(lonKey) + lonKey.length();
            int lonEnd    = json.indexOf("\"", lonStart);

            return new double[]{
                    Double.parseDouble(json.substring(latStart, latEnd)),
                    Double.parseDouble(json.substring(lonStart, lonEnd))
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================================
    // BUTTON FACTORY
    // =========================================================
    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    // =========================================================
    // ROUNDED PANEL
    // =========================================================
    static class RoundedPanel extends JPanel {

        private final int   radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg     = bg;
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