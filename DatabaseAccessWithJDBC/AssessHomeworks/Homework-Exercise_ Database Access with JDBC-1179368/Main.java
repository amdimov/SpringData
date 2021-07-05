import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


    public class Main {

        //
        //
        //

        private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
        private static final String DB_NAME = "minions_db";
        private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        private static Connection connection;

        public static void main(String[] args) throws SQLException, IOException {


            System.out.println("Please enter your personal user & pass in the private static Connection getConnection Method");
            System.out.println("See lines 325 & 327  and then start the program");
            System.out.println("If you have already done that ignore this message");

            connection = getConnection();

            System.out.println("Enter exercise number:");
            int exNum = Integer.parseInt(reader.readLine());

            switch (exNum) {
                case 2 -> exercise2();
                case 3 -> exercise3();
                case 4 -> exercise4();
                case 5 -> exercise5();
                case 6 -> exercise6();
                case 7 -> exercise7();
                case 8 -> exercise8();
                case 9 -> exercise9();
            }
        }

        private static void exercise6() throws IOException, SQLException {
            System.out.println("Please enter villain ID to delete");
            int villainId = Integer.parseInt(reader.readLine());



            PreparedStatement prepStateVillainName =
                    connection.prepareStatement("SELECT  v.name FROM villains AS v where v.id = ?;");
            prepStateVillainName.setInt(1, villainId);
            ResultSet rs = prepStateVillainName.executeQuery();
            if (!rs.next()){
                System.out.println("No such villain was found");

            }else {
                PreparedStatement prepStateAllMinions = connection.prepareStatement("DELETE FROM minions_villains\n" +
                        "WHERE villain_id = ?;");
                prepStateAllMinions.setInt(1, villainId);
                int affectedRows = prepStateAllMinions.executeUpdate();


                PreparedStatement prepStateDeleteVillain = connection.prepareStatement("DELETE FROM villains\n" +
                        "WHERE id = ?;");
                prepStateDeleteVillain.setInt(1, villainId);
                prepStateDeleteVillain.executeUpdate();

                System.out.println( rs.getString("name") + " was deleted");
                System.out.println( affectedRows + " minions released");
            }
        }

        private static void exercise4() throws IOException, SQLException {
            System.out.println("Please enter ONLY your Minion info here:");
            List<String> dataMinion = Arrays.stream(reader.readLine().split("\\s+")).collect(Collectors.toList());

            System.out.println("Please enter ONLY your Villain info here:");
            List<String> dataVillain = Arrays.stream(reader.readLine().split("\\s+")).collect(Collectors.toList());

            String minionName = dataMinion.get(1);
            int minionAge = Integer.parseInt(dataMinion.get(2));
            String town = dataMinion.get(3);

            String villainName = dataVillain.get(1);


            boolean townExists = checkIfEntityExists("towns" , town);

            if (!townExists){
                PreparedStatement prepStateAddTown =
                        connection.prepareStatement("INSERT INTO towns (`name`) values (?);");
                prepStateAddTown.setString(1, town);
                boolean townAdded = prepStateAddTown.execute();

                System.out.printf("Town %s was added to the database.%n", town);

            }

            boolean villainExists =  checkIfEntityExists("villains" , villainName);
            if (!villainExists){
                PreparedStatement prepStateAddVillain =
                        connection.prepareStatement("INSERT INTO villains (`name` , `evilness_factor`) values (? , ?);");
                prepStateAddVillain.setString(1,villainName);
                prepStateAddVillain.setString(2, "evil");
                boolean villainAdded = prepStateAddVillain.execute();

                System.out.printf("Villain %s was added to the database.%n", villainName);
            }

            PreparedStatement prepStateAddMinion =
                    connection.prepareStatement("INSERT INTO minions (`name` , `age`) values (? , ?);");
            prepStateAddMinion.setString(1,minionName);
            prepStateAddMinion.setInt(2, minionAge);
            boolean minionAdded = prepStateAddMinion.execute();



            PreparedStatement prepStateUpdateMinionVillainTable =
                    connection.prepareStatement("INSERT INTO minions_villains (minion_id , `villain_id`) values (? , ?);");
            prepStateUpdateMinionVillainTable.setInt(1, getEntityIdByName("minions" ,minionName ));
            prepStateUpdateMinionVillainTable.setInt(2, getEntityIdByName("villains" ,villainName));
            boolean tableUpdated = prepStateUpdateMinionVillainTable.execute();
            System.out.printf("Successfully added %s to be minion of %s.%n", minionName, villainName);

        }

        private static boolean checkIfEntityExists( String tableName , String entityName) throws SQLException {
            String query = "SELECT name FROM " + tableName + " WHERE name = ?;";
            PreparedStatement prepStateCheckEntity = connection.prepareStatement(query);
            prepStateCheckEntity.setString(1, entityName);
            ResultSet rs = prepStateCheckEntity.executeQuery();
            return  rs.next();
        }

        private static int getEntityIdByName(String tableName, String entityName) throws SQLException {

            String query = "SELECT id FROM " + tableName +  " WHERE name = ?;";

            PreparedStatement prepStateForId = connection.prepareStatement(query);
            prepStateForId.setString(1 , entityName);

            ResultSet rs3 = prepStateForId.executeQuery();
            rs3.next();
            return rs3.getInt("id");
        }

        private static void exercise9() throws IOException, SQLException {
            System.out.println("For this exercise you'll need to create a stored procedure named usp_get_older in MySQL Workbench. If you have it created " +
                    "ignore this message");
            System.out.println("If not -> you can copy this code and create it:  /*DELIMITER $$\n" +
                    "                    create procedure usp_get_older(input_id INT)\n" +
                    "                    begin\n" +
                    "                    UPDATE minions SET age = age + 1 WHERE  id = input_id;\n" +
                    "            end $$\n" +
                    "            DELIMITER ; */   ");
            System.out.println("Please, enter a minion id to get older");
            int idToGetOlder = Integer.parseInt(reader.readLine());

            CallableStatement callableStatement = connection.prepareCall("CALL usp_get_older(?)");
            callableStatement.setInt(1, idToGetOlder);

            int affectedRows  = callableStatement.executeUpdate();
        }

        private static void exercise8() throws IOException, SQLException {
            System.out.println("Please, enter minion ids separated by white space");

            List<String> ids = Arrays.stream(reader.readLine().split("\\s+")).collect(Collectors.toList());

            String query = "UPDATE minions\n" +
                    "SET age = age + 1 , name = LOWER(name) WHERE id IN (" + String.join(", " , ids) + ");";
            PreparedStatement prepState  = connection.prepareStatement(query);
            int rs = prepState.executeUpdate();

            PreparedStatement prepState2 = connection.prepareStatement("SELECT `name` , age FROM minions");
            ResultSet rs2 = prepState2.executeQuery();

            while (rs2.next()){
                System.out.printf("%s %d%n", rs2.getString(1) , rs2.getInt(2));
            }

        }

        private static void exercise7() throws SQLException {
            System.out.println("Warning: For this exercise you'll need fresh database. Please drop the modified minions_db and create it again. If you have already done that ignore this message ");

            PreparedStatement prepState = connection.prepareStatement("select name from minions;");

            ResultSet rs = prepState.executeQuery();

            LinkedList<String> minionNames = new LinkedList<String>();


            while (rs.next()){
                minionNames.add(rs.getString(1));
            }

            while (!minionNames.isEmpty()){
                System.out.println(minionNames.removeFirst());
                System.out.println(minionNames.removeLast());
            }
        }

        private static void exercise5() throws IOException, SQLException {

            System.out.println("Enter the chosen country");
            String country = reader.readLine();

            int numberOfAffectedRows = giveMeAffectedRows(country);

            if (numberOfAffectedRows > 0){
                System.out.println(numberOfAffectedRows + " town names were affected." );
                System.out.println( giveMeAffectedTowns(country));
            }
            else{
                System.out.println("No town names were affected.");
            }
        }


        private static String giveMeAffectedTowns(String country) throws SQLException {
            PreparedStatement prepState2_Towns =
                    connection.prepareStatement("select `name` from towns where country = ?;");

            prepState2_Towns.setString(1, country);

            ResultSet rs2_towns = prepState2_Towns.executeQuery();

            List<String> myTowns = new ArrayList<>();

            while (rs2_towns.next()){
                myTowns.add(rs2_towns.getString(1));
            }
            return  myTowns.toString();
        }


        private static int giveMeAffectedRows(String country) throws SQLException {
            PreparedStatement prepState_Update = connection.prepareStatement("UPDATE towns\n" +
                    "SET name = UPPER(name)\n" +
                    "WHERE country = ?;");

            prepState_Update.setString(1, country);
            return  prepState_Update.executeUpdate();
        }


        private static void exercise3() throws SQLException, IOException {
            System.out.println("Enter villain id:");
            int villainId = Integer.parseInt(reader.readLine());

            System.out.println( getVillainNameById(villainId) );

            System.out.println(getAllMinionsByVillainId(villainId));
        }

        private static String getAllMinionsByVillainId(int villainId) throws SQLException {

            PreparedStatement prepStateNamesOfMinions = connection.prepareStatement("SELECT  m.name , m.age FROM minions as m\n" +
                    "join minions_villains mv on m.id = mv.minion_id\n" +
                    "where villain_id = ?;");
            prepStateNamesOfMinions.setInt(1, villainId);

            ResultSet rs2 = prepStateNamesOfMinions.executeQuery();

            int counter = 1;

            String toReturn = "";

            while (rs2.next()){
                toReturn += String.format("%d. %s %d%n", counter++,
                        rs2.getString("name"),
                        rs2.getInt("age"));
            }

            return toReturn;
        }

        private static String  getVillainNameById(int villainId) throws SQLException {
            PreparedStatement prepStateVillainName = connection.prepareStatement("SELECT  v.name FROM villains AS v\n" +
                    "where v.id = ?;");
            prepStateVillainName.setInt(1, villainId);
            ResultSet rs = prepStateVillainName.executeQuery();
            if (!rs.next()){
                return "No villain with ID 10 exists in the database.";

            }
            return "Villain: " + rs.getString("name");
        }


        private static void exercise2() throws SQLException {
            PreparedStatement prepState =
                    connection.prepareStatement("SELECT v.name, COUNT( distinct MINION_ID) AS 'Count of minions'\n" +
                            "FROM villains AS v\n" +
                            "JOIN minions_villains AS mv ON v.id = mv.villain_id\n" +
                            "GROUP BY v.id\n" +
                            "having  `Count of minions` > 15\n" +
                            "order by  `Count of minions` DESC\n" +
                            "LIMIT 1;");


            ResultSet rs = prepState.executeQuery();

            while (rs.next()){
                System.out.printf("%s %d" , rs.getString(1) , rs.getInt(2));
            }
        }

        private static Connection getConnection() throws IOException, SQLException {
        /*  This is the recommended usual approach:
        System.out.println("Enter user");
        String user = reader.readLine();
        user = user.equals("") ? "root" : user;

        System.out.println("Enter password");
        String password = reader.readLine().trim();
        password = password.equals("") ? "" : password;
         */

            Properties properties = new Properties();

            //properties.setProperty("user" , user);
            properties.setProperty("user" , "WRITE_YOUR_USERNAME_HERE");
            //properties.setProperty("password" , password);
            properties.setProperty("password" , "WRITE_YOUR_PASSWORD_HERE");

            Connection connection =
                    DriverManager.getConnection(CONNECTION_STRING  + DB_NAME, properties);

            return connection;

            // ili po-kratko: return DriverManager.getConnection(CONNECTION_STRING  + DB_NAME, properties);
            // vmesto gornite 3 reda (329 - 332), no go ostavqm taka umishleno za po-golqma qsnota!
        }
    }


