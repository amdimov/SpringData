import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class ConnectionAndConnectionProperties_01 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {

            Connection connection = DB_Connection.createConnection("soft_uni");
            PreparedStatement statement = connection.prepareStatement("SELECT first_name, last_name FROM employees WHERE salary > ?;");
            statement.setDouble(1, Double.parseDouble(scanner.nextLine()));
            ResultSet resultSet = statement.executeQuery();
            StringBuilder stringBuilder= new StringBuilder();
            Connection connection2 = DB_Connection.createConnection("soft_uni");
            while (resultSet.next()){
                stringBuilder.append(resultSet.getString("first_name")).append(" ")
                .append(resultSet.getString("last_name")).append(System.lineSeparator());
            }
            System.out.println(stringBuilder.toString().trim());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
