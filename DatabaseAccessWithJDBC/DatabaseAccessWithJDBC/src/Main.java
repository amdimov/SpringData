import Connection.DB_Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        //TODO - set up your DB user and password in Connection/db_user_password.properties
        DB_Config db_config = new DB_Config();
        Connection connection = db_config.getConnection("minions_db");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        SolutionMethods.callSolutionMethods();
        System.out.println();
        System.out.print("Which solution would you like to check [2-9]: ");
        try {
            int problemNumber = Integer.parseInt(reader.readLine());
            System.out.println();

            switch (problemNumber){
                case 2:
                    Solutions.Problem_2_Get_Villains(connection);
                    break;
                case 3:
                    Solutions.Problem_3_Get_Minion_Names(connection, reader);
                    break;
                case 4:
                    Solutions.Problem_4_Add_Minion(connection, reader);
                    break;
                case 5:
                    Solutions.Problem_5_Change_Town_Names_Casing(connection, reader);
                    break;
                case 6:
                    System.out.println("Oops. Sorry, not enough time :-)");
                    break;
                case 7:
                    Solutions.Problem_7_Print_All_Minion_Names(connection);
                    break;
                case 8:
                    Solutions.Problem_8_Increase_Minions_Age(connection, reader);
                    break;
                case 9:
                    Solutions.Problem_9_Increase_Age_Stored_Procedure(connection, reader);
                    break;
                default:
                    System.out.println("Invalid input");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
