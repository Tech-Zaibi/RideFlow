import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class RidePaymentPage extends JFrame {

    private String username, driverName, vehicle, pickup, dropoff;
    private int rideId, totalFare;
    private double distance;
    private int durationMinutes;

    private JLabel totalFareLabel, statusLabel;
    private JTextField cardNumberField, cardHolderField;
    private JPasswordField cvvField;
    private JButton payButton;

    public RidePaymentPage(int rideId, String username, String driverName) {

        this.rideId = rideId;
        this.username = username;
        this.driverName = driverName;

        fetchRideData();

        setTitle("RideFlow Payment - " + username);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Pickup: " + pickup));
        panel.add(new JLabel("Dropoff: " + dropoff));
        panel.add(new JLabel("Driver: " + driverName));

        totalFare = calculateFare();

        totalFareLabel = new JLabel("Total Fare: Rs. " + totalFare);
        panel.add(totalFareLabel);

        panel.add(Box.createVerticalStrut(20));

        payButton = new JButton("Pay Now");
        payButton.addActionListener(e -> handlePaymentProcess());
        panel.add(payButton);

        add(panel);
        setVisible(true);
    }

    // ✅ FIXED DB CONNECTION
    private void fetchRideData() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM ride_requests WHERE id=?")) {

            ps.setInt(1, rideId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                pickup = rs.getString("pickup_location");
                dropoff = rs.getString("dropoff_location");
                vehicle = rs.getString("vehicle_type");

                distance = 5 + Math.random() * 10;
                durationMinutes = (int) (distance * 2.5);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateFare() {
        int baseFare = switch (vehicle) {
            case "Bike" -> 150;
            case "Car without AC" -> 250;
            case "Car with AC" -> 350;
            case "Premium" -> 600;
            default -> 200;
        };

        return baseFare + (int)(distance * 50) + (durationMinutes * 10);
    }

    // ✅ PAYMENT (NO STATUS CHANGE HERE)
    private void handlePaymentProcess() {

        payButton.setEnabled(false);

        try (Connection conn = DatabaseConnection.getConnection()) {

            JOptionPane.showMessageDialog(this,
                    "✅ Payment Successful!\nRs. " + totalFare);

            dispose();

            new FeedbackPage(username, driverName);

        } catch (Exception e) {
            e.printStackTrace();
            payButton.setEnabled(true);
        }
    }

    // ✅ OPTIONAL UI HELPERS (INSIDE CLASS NOW)

    private JPanel createStyledSection(String title, Color borderColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(32, 46, 55));
        panel.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panel.add(t);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private void addInfo(JPanel p, String lbl, String val) {
        p.add(new JLabel("<html><font color='#AAAAAA'>" + lbl + "</font></html>"));
        p.add(new JLabel("<html><font color='white'>" + val + "</font></html>"));
    }

    private void addFareRow(JPanel p, String desc, String amt) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel d = new JLabel(desc);
        d.setForeground(Color.WHITE);

        JLabel a = new JLabel(amt);
        a.setForeground(new Color(255, 235, 59));

        row.add(d, BorderLayout.WEST);
        row.add(a, BorderLayout.EAST);

        p.add(row);
    }
}
