import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class Solutions {

    public static void Problem_2_Get_Villains(Connection connection){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT v.`name`, COUNT(DISTINCT mv.minion_id) AS `num_of_minions`\n" +
                    "FROM minions_villains AS mv\n" +
                    "JOIN minions AS m ON mv.minion_id = m.id\n" +
                    "JOIN villains AS v ON mv.villain_id = v.id\n" +
                    "GROUP BY v.`name`\n" +
                    "HAVING `num_of_minions` > ?\n" +
                    "ORDER BY `num_of_minions` DESC;");
            preparedStatement.setInt(1, 15);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                System.out.printf("%s %d%n", resultSet.getString("v.name"), resultSet.getInt("num_of_minions"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void Problem_3_Get_Minion_Names(Connection connection, BufferedReader reader){
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT DISTINCT v.`name`, m.`name`, m.`age`\n" +
                            "FROM minions_villains AS mv\n" +
                            "JOIN minions AS m ON mv.minion_id = m.id\n" +
                            "JOIN villains AS v ON mv.villain_id = v.id\n" +
                            "WHERE v.`id` = ?;");
            System.out.print("Enter villain ID: ");
            int villainID = Integer.parseInt(reader.readLine());
            preparedStatement.setInt(1, villainID);
            ResultSet resultSet = preparedStatement.executeQuery();


            int counter = 0;

            while (resultSet.next()){
                counter++;
                String minionName = resultSet.getString("m.name");
                int minionAge = resultSet.getInt("m.age");
                if (resultSet.isFirst()){
                    String villainName = resultSet.getString("v.name");
                    System.out.printf("Villain: %s%n%d. %s %d%n", villainName, counter, minionName, minionAge);
                    continue;
                }
                System.out.printf("%d. %s %d%n", counter, minionName, minionAge);
            }
            if (counter == 0){
                System.out.printf("No villain with ID %d exists in the database.%n", villainID);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    public static void Problem_4_Add_Minion(Connection connection, BufferedReader reader){
        try {
            System.out.print("Minion: ");
            String[] minions = reader.readLine().split("\\s+");
            String minionName = minions[0];
            String minionAge = minions[1];
            String minionTown = minions[2];
            System.out.print("Villain: ");
            String villain = reader.readLine();
            Map<String, Integer> townsIdName = SolutionMethods.getValueKeyByName("towns", connection);
            Map<String, Integer> villainsIdName = SolutionMethods.getValueKeyByName("villains",  connection);

            Integer townId = townsIdName.get(minionTown);
            if (townId == null){
                Integer getLastIdOfTowns = SolutionMethods.getLastId(townsIdName);
                townId = ++getLastIdOfTowns;
                townsIdName.put(minionTown, ++getLastIdOfTowns);
                SolutionMethods.insertInDatabase("INSERT INTO towns(`name`) VALUES(?);", connection
                        , minionTown);
                System.out.printf("Town %s was added to the database.%n", minionTown);
            }
            Integer villainId = villainsIdName.get(villain);
            if (villainId == null){
                Integer villainsLastId = SolutionMethods.getLastId(villainsIdName);
                villainId = ++villainsLastId;
                townsIdName.put(villain, ++villainsLastId);
                SolutionMethods.insertInDatabase("INSERT INTO villains(`name`, `evilness_factor`) VALUES (?, 'evil');",
                        connection, villain);
                System.out.printf("Villain %s was added to the database.%n", villain);
            }

            SolutionMethods.insertInDatabase("INSERT INTO minions(`name`, `age`, `town_id`)VALUES(?, ?, ?)"
                    , connection, minionName, minionAge, townId.toString());
            int minionId = SolutionMethods.getMinionId(connection, minionName);
            SolutionMethods.insertInDatabase("INSERT INTO minions_villains(`minion_id`, `villain_id`)" +
                    "VALUES(?, ?)", connection, String.valueOf(minionId), String.valueOf(villainId));
            System.out.printf("Successfully added %s to be minion of %s%n", minionName, villain);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void Problem_5_Change_Town_Names_Casing(Connection connection, BufferedReader reader){
        System.out.print("Country: ");
        try {
            String country = reader.readLine();
            PreparedStatement preparedStatement = connection
                    .prepareStatement("UPDATE towns \n" +
                            "SET `name` = UPPER(`name`)\n" +
                            "WHERE country = ?\n" +
                            ";");
            preparedStatement.setString(1, country);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0){
                System.out.println(rowsAffected + " town names were affected.");
                preparedStatement = connection.prepareStatement("SELECT towns.`name` FROM towns WHERE `country` = ?");
                preparedStatement.setString(1, country);
                ResultSet resultSet = preparedStatement.executeQuery();
                StringBuilder builder = new StringBuilder();

                builder.append("[");
                String towns = "";
                while (resultSet.next()) {
                    if (!resultSet.isLast()) {
                        builder.append(resultSet.getString("towns.name")).append(", ");
                    } else {
                        builder.append(resultSet.getString("towns.name"));
                    }
                }
                builder.append("]");
                System.out.println(builder.toString());
            }else {
                System.out.println("No town names were affected.");
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void Problem_7_Print_All_Minion_Names(Connection connection){
        try {
            //TYPE_SCROLL_INSENSITIVE - allows moving cursor forwards and backwards
            //CONCUR_READ_ONLY - allows only reading, but not updating
            Statement preparedStatement = connection
                    .createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = preparedStatement.executeQuery("SELECT `name` FROM minions_db.minions;");
            int i = 1;
            resultSet.beforeFirst();
            while (resultSet.next()){
                //absolute() - sets the cursor to specific row
                resultSet.absolute(i);
                int currentRowForwards = resultSet.getRow();
                System.out.println(resultSet.getString(1));
                resultSet.absolute(i * -1);
                int currentRowBackwards = resultSet.getRow();
                System.out.println(resultSet.getString(1));
                resultSet.absolute(i);
                //break when cursor forward meets cursor backwards
                if (currentRowBackwards == currentRowForwards+1){
                    break;
                }
                i++;
            }


            //resultSet.first();
            //String nameFront = resultSet.getString("name");
            //System.out.println(nameFront);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void Problem_8_Increase_Minions_Age(Connection connection, BufferedReader reader){
        try {
            System.out.print("Select id separated by space: ");
            String[] ids = reader.readLine().split("\\s+");
            PreparedStatement preparedStatement = connection
                    .prepareStatement(SolutionMethods.insertWildcardInMinionsAgeUpdateQuery(ids));
            for (int i = 1; i <= ids.length; i++) {
                preparedStatement.setInt(i, Integer.parseInt(ids[i-1]));
            }
            preparedStatement.execute();
            preparedStatement = connection
                    .prepareStatement("SELECT `name`, `age` FROM minions_db.minions;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                System.out.println(resultSet.getString("minions.name") + " "
                        + resultSet.getString("minions.age"));
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    public static void Problem_9_Increase_Age_Stored_Procedure(Connection connection, BufferedReader reader){
        try {
            System.out.print("Select Minion's ID: ");
            int minionID = Integer.parseInt(reader.readLine());
            CallableStatement callableStatement = connection.prepareCall("CALL usp_get_older(?);");
            callableStatement.setInt(1, minionID);
            int rows = callableStatement.executeUpdate();
            System.out.println(rows + " affected");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
