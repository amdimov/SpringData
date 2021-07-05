import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));
    private static Connection CONNECTION;

    public static void main(String[] args) throws SQLException, IOException {
        //TODO: Change the username and password in method getConnection();
        CONNECTION = getConnection();
        System.out.println("Enter the number of the check task");
        int number = Integer.parseInt(READER.readLine());
        switch (number) {
            case 2:
                exerciseOne();
                break;
            case 3:
                exerciseTwo();
                break;
            case 4:
                exerciseThree();
                break;
            case 5:
                exerciseFive();
                break;
            case 6:
                exerciseSix();
                break;
            case 7:
                exerciseSeven();
                break;
            case 8:
                exerciseEight();
                break;
            case 9:
                exerciseNine();
                break;
        }

    }

    private static void exerciseNine() throws IOException, SQLException {
        System.out.println("Enter Minion Id:");
        int minionId = Integer.parseInt(READER.readLine());
        String sql = "call usp_get_older(?)";
        CallableStatement callableStatement = CONNECTION.prepareCall(sql);
        callableStatement.setInt(1, minionId);
        callableStatement.execute();
        printMinionsNameAndAge();
    }

    private static void exerciseEight() throws IOException, SQLException {
        System.out.println("Enter Minion`s Ids:");
        String ids = READER.readLine();
        int[] ints = Arrays.stream(ids.split("\\s+")).mapToInt(Integer::parseInt).toArray();


        String updateSql = String.format("update minions\n" +
                "set age = (age + 1 ),\n" +
                "name = LOWER(name)\n" +
                "where id IN (%s);", Arrays.toString(ints).replaceAll("[\\[\\]]", ""));
        Statement statement = CONNECTION.createStatement();
        int i = statement.executeUpdate(updateSql);

        printMinionsNameAndAge();
    }

    private static void printMinionsNameAndAge() throws SQLException {
        String selectSql = "select name, age\n" +
                "FROM minions;";
        Statement statement1 = CONNECTION.createStatement();
        ResultSet resultSet = statement1.executeQuery(selectSql);
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1) + " " + resultSet.getInt(2));
        }
    }

    private static void exerciseSeven() throws SQLException {
        String selectSql = "select name\n" +
                "from minions";
        Statement statement = CONNECTION.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSql);
        List<String> minionsNames = new ArrayList<>();
        while (resultSet.next()) {
            minionsNames.add(resultSet.getString(1));
        }
        int count = minionsNames.size() / 2;
        for (int i = 0; i < count; i++) {
            System.out.println(minionsNames.get(i));
            System.out.println(minionsNames.get(minionsNames.size() - 1 - i));
        }
        if (minionsNames.size() % 2 == 1) {
            System.out.println(minionsNames.get(count + 1));
        }
    }

    private static void exerciseSix() throws IOException, SQLException {
        System.out.println("Enter Villain ID:");
        int villainId = Integer.parseInt(READER.readLine());

        String villainNameById = getVillainNameById(villainId);
        if (villainNameById == null) {
            System.out.println("No such villain was found");
        } else {
            String deleteSql = "DELETE from minions_villains\n" +
                    "where villain_id = ?;";
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(deleteSql);
            preparedStatement.setInt(1, villainId);
            int releasedMinions = preparedStatement.executeUpdate();

            String deleteVillainSql = "DELETE from villains\n" +
                    "where id = ?;";
            PreparedStatement preparedStatement1 = CONNECTION.prepareStatement(deleteVillainSql);
            preparedStatement.setInt(1, villainId);
            int deletedVillains = preparedStatement.executeUpdate();

            System.out.println(villainNameById + " was deleted");
            System.out.println(releasedMinions + " minions released");

        }
    }

    private static void exerciseFive() throws IOException, SQLException {
        System.out.println("Enter country:");
        String country = READER.readLine();

        String updateSql = "update towns\n" +
                "SET name = UPPER(name)\n" +
                "where country = ?;";
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(updateSql);
        preparedStatement.setString(1, country);
        int changes = preparedStatement.executeUpdate();

        if (changes == 0) {
            System.out.println("No town names were affected.");
        } else {
            String selectSql = "select name\n" +
                    "from towns\n" +
                    "where country = ?;";
            PreparedStatement preparedStatement1 = CONNECTION.prepareStatement(selectSql);
            preparedStatement1.setString(1, country);
            ResultSet resultSet = preparedStatement1.executeQuery();
            List<String> townsChanges = new ArrayList<>();
            while (resultSet.next()) {
                townsChanges.add(resultSet.getString(1));
            }
            System.out.println(String.format("%d town names were affected. \n" +
                    "[%s]\n", changes, String.join(", ", townsChanges)));
        }
    }

    private static void exerciseThree() throws SQLException, IOException {
        CONNECTION.setAutoCommit(false);
        System.out.println("Enter information about minion and its villain:");
        String minionData = READER.readLine();
        String villainData = READER.readLine();

        String[] mData = minionData.split("\\s+");
        String[] vData = villainData.split("\\s+");

        String town = mData[3];
        String name = mData[1];
        int age = Integer.parseInt(mData[2]);

        addEntityInDataBaseIfNotExist(town, "towns",
                "insert into towns(name)\n",
                "values ('%s');",
                "Town %s was added to the database.");

        addEntityInDataBaseIfNotExist(vData[1], "villains",
                "INSERT INTO villains(name, evilness_factor)\n",
                "VALUES ('%s', 'evil');",
                "Villain %s was added to the database.");

        addMinionInDataBaseIfNotExist(town, name, age);


        makeMinionServantOfVillain(vData[1], name);

        CONNECTION.setAutoCommit(true);
    }

    private static void makeMinionServantOfVillain(String vDatum, String name) throws SQLException {
        int villainId = getEntityIdByName(vDatum, "villains");
        int minionId = getEntityIdByName(name, "minions");

        PreparedStatement preparedStatement = CONNECTION.prepareStatement
                (String.format("insert into minions_villains\n" +
                        "VALUES (%d, %d);", minionId, villainId));
        preparedStatement.executeUpdate();
        System.out.println(String.format("Successfully added %s to be minion of %s.", name, vDatum));
    }

    private static void addMinionInDataBaseIfNotExist(String town, String name, int age) throws SQLException {
        if (checkIfEntityExist(name, "minions")) {
            int townID = getEntityIdByName(town, "towns");
            String insertSql = String.format("insert into minions(name, age, town_id)\n" +
                    "values ('%s', %d, %d);", name, age, townID);
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(insertSql);
            preparedStatement.executeUpdate();
            CONNECTION.commit();
        }
    }

    private static void addEntityInDataBaseIfNotExist(String entityName, String tableName,
                                                      String s, String s2, String s3) throws SQLException {
        if (checkIfEntityExist(entityName, tableName)) {
            String insertSQl = String.format(s +
                    s2, entityName);
            PreparedStatement preparedStatement1 = CONNECTION.prepareStatement(insertSQl);
            preparedStatement1.executeUpdate();
            CONNECTION.commit();
            System.out.println(String.format(s3, entityName));
        }
    }

    private static int getEntityIdByName(String name, String tableName) throws SQLException {
        String sql = String.format("select id\n" +
                "from %s\n" +
                "where name = ?;", tableName);

        PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        throw new NoSuchFieldError("No such entity exist");
    }


    private static boolean checkIfEntityExist(String name, String tableName) throws SQLException {
        String sql = String.format("select * FROM %s\n" +
                "where name = ?", tableName);
        PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return false;
        }
        return true;
    }

    private static void exerciseTwo() throws IOException, SQLException {
        System.out.println("Enter Villain ID:");
        int villainId = Integer.parseInt(READER.readLine());
        String villainName = getVillainNameById(villainId);
        if (villainName != null) {
            System.out.println("Villain: " + villainName);
            PreparedStatement preparedStatement = CONNECTION.prepareStatement
                    ("select name,age\n" +
                            "from minions\n" +
                            "join minions_villains mv on minions.id = mv.minion_id\n" +
                            "where mv.villain_id = ?");
            preparedStatement.setInt(1, villainId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(String.format("%d. %s %d",
                        resultSet.getRow(),
                        resultSet.getString("name"),
                        resultSet.getInt("age")));
            }
        } else {
            System.out.println(String.format("No villain with ID %d exists in the database.", villainId));
        }
    }

    private static String getVillainNameById(int villainId) throws SQLException {
        PreparedStatement preparedStatement = CONNECTION.prepareStatement("select name\n" +
                "from villains\n" +
                "where id = ?");
        preparedStatement.setInt(1, villainId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }

    private static void exerciseOne() throws SQLException {
        PreparedStatement preparedStatement = CONNECTION.prepareStatement
                ("select v.name , count(DISTINCT mv.minion_id) number_of_minions\n" +
                        "from villains v\n" +
                        "join minions_villains mv on v.id = mv.villain_id\n" +
                        "group by v.name\n" +
                        "having number_of_minions > 15\n" +
                        "order by number_of_minions DESC");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(String.format("%s %d", resultSet.getString("name"), resultSet.getLong(2)));
        }
    }

    private static Connection getConnection() throws SQLException, IOException {
//        System.out.println("Enter user:");
//        String user = READER.readLine();
//
//        System.out.println("Enter password:");
//        String password = READER.readLine();
        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "bulgariavarna");

        return DriverManager.getConnection(CONNECTION_STRING + "minions_db", properties);
    }
}
