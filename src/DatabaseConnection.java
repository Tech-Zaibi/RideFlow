import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;" +
                    "databaseName=RideFlowDB;" +
                    "integratedSecurity=true;" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}