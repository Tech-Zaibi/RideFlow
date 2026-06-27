import java.sql.*;

public class UserDatabase {

    public static boolean registerPassenger(String username, String password,
                                            String email, String phone) {
        String insertUser = "INSERT INTO Users (Username, PasswordHash, Email, Phone, Role) " +
                "VALUES (?, ?, ?, ?, 'Passenger')";
        // Removed Username column — no longer exists in Passengers table
        String insertPassenger = "INSERT INTO Passengers (UserID) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps1 = conn.prepareStatement(insertUser,
                    Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, username);
            ps1.setString(2, password);
            ps1.setString(3, email);
            ps1.setString(4, phone);
            ps1.executeUpdate();

            ResultSet rs = ps1.getGeneratedKeys();
            if (rs.next()) {
                int userID = rs.getInt(1);
                PreparedStatement ps2 = conn.prepareStatement(insertPassenger);
                ps2.setInt(1, userID);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerDriver(String username, String password,
                                         String email, String phone,
                                         String licenseNumber, String vehicleType) {
        String insertUser = "INSERT INTO Users (Username, PasswordHash, Email, Phone, Role) " +
                "VALUES (?, ?, ?, ?, 'Driver')";
        // Removed Username column — no longer exists in Drivers table
        String insertDriver = "INSERT INTO Drivers (UserID, LicenseNumber, VehicleType) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps1 = conn.prepareStatement(insertUser,
                    Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, username);
            ps1.setString(2, password);
            ps1.setString(3, email);
            ps1.setString(4, phone);
            ps1.executeUpdate();

            ResultSet rs = ps1.getGeneratedKeys();
            if (rs.next()) {
                int userID = rs.getInt(1);
                PreparedStatement ps2 = conn.prepareStatement(insertDriver);
                ps2.setInt(1, userID);
                ps2.setString(2, licenseNumber);
                ps2.setString(3, vehicleType);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginPassenger(String username, String password) {
        String query = "SELECT * FROM Users WHERE Username=? AND PasswordHash=? AND Role='Passenger'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginDriver(String username, String password) {
        String query = "SELECT * FROM Users WHERE Username=? AND PasswordHash=? AND Role='Driver'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDriverVehicleType(String username) {
        String query = "SELECT d.VehicleType FROM Drivers d " +
                "JOIN Users u ON d.UserID = u.UserID " +
                "WHERE u.Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("VehicleType");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}