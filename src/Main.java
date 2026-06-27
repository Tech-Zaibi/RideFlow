import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            System.out.println("✅ Connected to SQL Server!");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }
}