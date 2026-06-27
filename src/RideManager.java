import java.sql.*;
import java.util.*;

public class RideManager {

    // =========================================
    // RIDE REQUEST MODEL
    // =========================================
    public static class RideRequest {

        public int requestId;
        public String passengerUsername;
        public String pickupLocation;
        public String destinationLocation;
        public String estimatedFare;
        public String driverUsername;
        public String status;

        public RideRequest(int id,
                           String passengerUsername,
                           String pickupLocation,
                           String destinationLocation,
                           String estimatedFare) {

            this.requestId = id;
            this.passengerUsername = passengerUsername;
            this.pickupLocation = pickupLocation;
            this.destinationLocation = destinationLocation;
            this.estimatedFare = "Rs. " + estimatedFare;
        }
    }

    // =========================================
    // GET PENDING RIDES BY VEHICLE TYPE
    // =========================================
    public static List<RideRequest> getPendingRequestsByVehicleType(String vehicleType) {

        List<RideRequest> rides = new ArrayList<>();

        String query =
                "SELECT * FROM ride_requests " +
                        "WHERE vehicle_type=? AND status='PENDING'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, vehicleType);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                RideRequest ride = new RideRequest(
                        rs.getInt("id"),
                        rs.getString("passenger_username"),
                        rs.getString("pickup_location"),
                        rs.getString("dropoff_location"),
                        rs.getString("fare")
                );

                ride.driverUsername = rs.getString("driver_username");
                ride.status = rs.getString("status");

                rides.add(ride);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rides;
    }

    // =========================================
    // ACCEPT RIDE
    // =========================================
    public static boolean acceptRide(int rideId, String driverUsername) {

        String query =
                "UPDATE ride_requests " +
                        "SET status='ACCEPTED', driver_username=? " +
                        "WHERE id=? AND status='PENDING'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, driverUsername);
            pst.setInt(2, rideId);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================================
    // FINISH RIDE
    // =========================================
    public static boolean finishRide(int rideId) {

        String query =
                "UPDATE ride_requests SET status='COMPLETED' WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, rideId);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // =========================================
    // CHECK ACTIVE RIDE
    // =========================================
    public static boolean hasActiveRide(String driverUsername) {

        String query =
                "SELECT COUNT(*) " +
                        "FROM ride_requests " +
                        "WHERE driver_username=? " +
                        "AND status='ACCEPTED'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, driverUsername);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================================
    // GET ACTIVE RIDE ID
    // =========================================
    public static int getActiveRideId(String driverUsername) {

        String query =
                "SELECT TOP 1 id " +
                        "FROM ride_requests " +
                        "WHERE driver_username=? " +
                        "AND status='ACCEPTED' " +
                        "ORDER BY id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, driverUsername);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // =========================================
    // GET ACTIVE RIDE DETAILS
    // =========================================
    public static RideRequest getActiveRideDetails(int rideId) {

        String query =
                "SELECT * FROM ride_requests WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, rideId);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                RideRequest ride = new RideRequest(
                        rs.getInt("id"),
                        rs.getString("passenger_username"),
                        rs.getString("pickup_location"),
                        rs.getString("dropoff_location"),
                        rs.getString("fare")
                );

                ride.driverUsername = rs.getString("driver_username");
                ride.status = rs.getString("status");

                return ride;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================================
    // CHECK PAYMENT STATUS
    // =========================================
    public static boolean isPaymentConfirmed(int rideId) {

        String query =
                "SELECT status FROM ride_requests WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, rideId);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                String status = rs.getString("status");

                return status.equalsIgnoreCase("COMPLETED");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================================
    // SAVE FEEDBACK
    // =========================================
    public static boolean saveFeedback(int rideId,
                                       String passengerUsername,
                                       String driverUsername,
                                       int rating,
                                       String comments) {

        String query =
                "INSERT INTO RideFeedback " +
                        "(ride_id, passenger_username, driver_username, rating, comments) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, rideId);
            pst.setString(2, passengerUsername);
            pst.setString(3, driverUsername);
            pst.setInt(4, rating);
            pst.setString(5, comments);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================================
    // GET DRIVER USERNAME BY RIDE ID
    // =========================================
    public static String getDriverUsernameByRide(int rideId) {

        String query =
                "SELECT driver_username " +
                        "FROM ride_requests " +
                        "WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, rideId);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("driver_username");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================================
    // GET PASSENGER PENDING RIDE
    // =========================================
    public static RideRequestManager.PendingRideInfo getPassengerPendingRide(String username) {

        String query =
                "SELECT TOP 1 id, driver_username, status " +
                        "FROM ride_requests " +
                        "WHERE passenger_username=? " +
                        "AND status='ACCEPTED' " +
                        "ORDER BY id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                return new RideRequestManager.PendingRideInfo(
                        rs.getInt("id"),
                        rs.getString("driver_username"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================================
    // GET USER RIDE HISTORY
    // =========================================
    public static String getUserRideHistory(String username) {

        StringBuilder history = new StringBuilder();

        String query =
                "SELECT * FROM ride_requests " +
                        "WHERE passenger_username=? " +
                        "ORDER BY id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                history.append("Ride ID: ")
                        .append(rs.getInt("id"))
                        .append("\n");

                history.append("Pickup: ")
                        .append(rs.getString("pickup_location"))
                        .append("\n");

                history.append("Dropoff: ")
                        .append(rs.getString("dropoff_location"))
                        .append("\n");

                history.append("Vehicle: ")
                        .append(rs.getString("vehicle_type"))
                        .append("\n");

                history.append("Fare: Rs. ")
                        .append(rs.getString("fare"))
                        .append("\n");

                history.append("Status: ")
                        .append(rs.getString("status"))
                        .append("\n");

                history.append("-----------------------------------\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (history.length() == 0) {
            return "No rides found.";
        }

        return history.toString();
    }

    // =========================================
    // SAVE RIDE REQUEST
    // =========================================
    public static int saveRideRequestWithId(String passengerUsername,
                                            String pickupLocation,
                                            String dropoffLocation,
                                            String vehicleType,
                                            String fare) {

        String query =
                "INSERT INTO ride_requests " +
                        "(passenger_username, pickup_location, dropoff_location, vehicle_type, fare, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst =
                     con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, passengerUsername);
            pst.setString(2, pickupLocation);
            pst.setString(3, dropoffLocation);
            pst.setString(4, vehicleType);
            pst.setString(5, fare);

            int rows = pst.executeUpdate();

            if (rows > 0) {

                ResultSet rs = pst.getGeneratedKeys();

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}