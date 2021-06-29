package Connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB_Config {
    private static final String DB_LINK = "jdbc:mysql://localhost/";
    private Connection connection;
    private String selectedDatabase;

    private void setConnection() throws IOException, SQLException {
        InputStream db_user_password = this.getClass().getClassLoader().getResourceAsStream("Connection/db_user_password.properties");
        Properties properties = new Properties();
        if (db_user_password != null) {
            properties.load(db_user_password);
        }else {
            throw new IllegalStateException("Property file not found");
        }
        this.connection = DriverManager.getConnection(DB_LINK+this.selectedDatabase, properties);
    }

    public Connection getConnection(String selectDatabase){
        this.selectedDatabase = selectDatabase;
        try {
            setConnection();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return this.connection;
    }

}
