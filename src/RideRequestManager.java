import java.sql.*;

public class RideRequestManager {

    public static class PendingRideInfo {
        public int rideId;
        public String status;
        public String driverUsername;

        public PendingRideInfo(int rideId, String status, String driverUsername) {
            this.rideId = rideId;
            this.status = status;
            this.driverUsername = driverUsername;
        }
    }

    // Save ride and return generated ID
    public static int saveRideRequestWithId(String passengerUsername, String pickupLocation,
                                            String dropoffLocation, String vehicleType,
                                            String fare) {
        String query = "INSERT INTO ride_requests " +
                "(passenger_username, pickup_location, dropoff_location, " +
                "vehicle_type, fare, status) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, passengerUsername);
            ps.setString(2, pickupLocation);
            ps.setString(3, dropoffLocation);
            ps.setString(4, vehicleType);
            ps.setString(5, fare);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Check if passenger has an accepted (unpaid) ride
    public static PendingRideInfo getPassengerPendingRide(String passengerUsername) {
        String query = "SELECT TOP 1 id, status, driver_username FROM ride_requests " +
                "WHERE passenger_username = ? AND status = 'ACCEPTED' " +
                "ORDER BY request_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, passengerUsername);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PendingRideInfo(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getString("driver_username")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get ride status by ID
    public static String getRideStatus(int requestId) {
        String query = "SELECT status FROM ride_requests WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }

    // Get driver username for a ride
    public static String getDriverUsername(int requestId) {
        String query = "SELECT driver_username FROM ride_requests WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("driver_username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get full ride history for a passenger
    public static String getUserRideHistory(String username) {
        String query = "SELECT pickup_location, dropoff_location, vehicle_type, " +
                "fare, status, driver_username, request_time " +
                "FROM ride_requests WHERE passenger_username = ? " +
                "ORDER BY request_time DESC";

        StringBuilder history = new StringBuilder();
        history.append("=== Ride History for ").append(username).append(" ===\n\n");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                history.append("Ride #").append(count).append("\n");
                history.append("  From    : ").append(rs.getString("pickup_location")).append("\n");
                history.append("  To      : ").append(rs.getString("dropoff_location")).append("\n");
                history.append("  Vehicle : ").append(rs.getString("vehicle_type")).append("\n");
                history.append("  Fare    : Rs. ").append(rs.getString("fare")).append("\n");
                history.append("  Driver  : ").append(
                        rs.getString("driver_username") != null ?
                                rs.getString("driver_username") : "Not assigned yet").append("\n");
                history.append("  Status  : ").append(rs.getString("status")).append("\n");
                history.append("  Date    : ").append(rs.getString("request_time")).append("\n");
                history.append("─────────────────────────────\n");
            }

            if (count == 0) history.append("No rides found.");

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error loading history: " + e.getMessage();
        }

        return history.toString();
    }
}