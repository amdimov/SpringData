import java.sql.*;
import java.util.Scanner;

public class TestConnection {
    public static void main(String[] args) {
        ResultSet getUpdates = null;
        PreparedStatement getNameAndUpdatedSalary = null;
        try {


            Scanner scanner = new Scanner(System.in);
            System.out.print("Check for name: ");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "bulgariavarna");
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.execute("USE soft_uni");

            String checkName = scanner.nextLine();
            ResultSet getNames = statement.executeQuery("SELECT * FROM employees WHERE first_name = '" + checkName + "'");

            boolean result = false;

            while (getNames.next()) {
                System.out.println(getNames.getString("first_name") + " " + getNames.getString("last_name") + " "
                        + getNames.getString("salary")
                );
                result = true;
            }
            if (!result) {
                System.out.println("Nothing Found");
            }
            System.out.print("Whose salary you would like to increase with 10% (Last Name): ");
            String lastName = scanner.nextLine();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE employees " +
                    "SET salary = salary * 1.10 WHERE last_name = ?");
            preparedStatement.setString(1, lastName);
            int returning = preparedStatement.executeUpdate();



            if (returning > 0){
                System.out.println(returning +" rows affected");
            }

            getNameAndUpdatedSalary = connection.prepareStatement("SELECT * FROM employees WHERE last_name = ? AND " +
                    "first_name = ?");
            getNameAndUpdatedSalary.setString(1, lastName);
            getNameAndUpdatedSalary.setString(2, checkName);
            getUpdates = getNameAndUpdatedSalary.executeQuery();
            int id = -1;
            String status = "";
            while (getUpdates.next()) {
                id = getUpdates.getInt("employee_id");
                status = getUpdates.getString("first_name") + " " + getUpdates.getString("last_name") + " "
                        + getUpdates.getString("salary");
                System.out.println(status);
            }
            System.out.println("His ID number is: " + id);
            System.out.println("Save changes?[yes/no]");
            String answer = scanner.nextLine();
            if (answer.equals("yes")){
                connection.commit();
            }else {
                connection.rollback();
                getUpdates = getNameAndUpdatedSalary.executeQuery();
                status = getUpdates.getString("first_name") + " " + getUpdates.getString("last_name") + " "
                        + getUpdates.getString("salary");
                System.out.println(status);
                System.out.println("Changes not saved");

            }
            System.out.println(connection.isClosed());


        }catch (SQLException e){

        }
    }
}
