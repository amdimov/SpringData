import com.mysql.cj.jdbc.ConnectionWrapper;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class DB_Connection{
    private static Properties properties;
    private static Connection connection;
    private static String database;

    private DB_Connection() throws SQLException {
        properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "bulgariavarna");
        connection =  DriverManager
                .getConnection("jdbc:mysql://localhost:3306/"+ database, properties);
    }
    public static Connection createConnection(String databaseName){
        try {
            database = databaseName;
            DB_Connection dbConnection = new DB_Connection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
