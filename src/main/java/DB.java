import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/fipldb";
    private static final String USER = "root";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, "");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Impossibile connettersi al db", e);
        }
    }
}
