import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SolutionMethods {
    public static void callSolutionMethods(){
        try {
            Class clazz = Class.forName("Solutions");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                String[] s = method.getName().split("_");
                String collect = String.join(" ", s);
                System.out.println(collect);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Map<String, Integer> getValueKeyByName(String table, Connection connection){
        Map<String, Integer> idNameMap = new HashMap<>();
        try {
            PreparedStatement addTown = connection
                    .prepareStatement(String.format("SELECT %s.id, %s.`name` FROM %s",table, table, table));
            ResultSet resultSet = addTown.executeQuery();
            while (resultSet.next()){
                idNameMap.put(resultSet.getString(table+".name"), resultSet.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idNameMap;

    }
    public static Integer getLastId(Map<String, Integer> map){
        return map.values().stream().max(Integer::compareTo).orElse(-1);
    }

    public static int insertInDatabase(String sqlInsert, Connection connection, String... values){
        try {
            PreparedStatement statement = connection.prepareStatement(sqlInsert);
            for (int i = 0; i < values.length; i++) {
                if (!Character.isDigit(values[i].charAt(0))) {
                    statement.setString(i+1, values[i]);
                }else {
                    statement.setInt(i+1, Integer.parseInt(values[i]));
                }
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -2;
    }
    public static int getMinionId(Connection connection, String minionName){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT minions.id FROM minions WHERE name = ?"
            );
            preparedStatement.setString(1, minionName);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static String insertWildcardInMinionsAgeUpdateQuery(String[] ids){
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE minions\n" +
                "SET age = age +1, `name` = LOWER(`name`)\n" +
                "WHERE id IN(");
        for (int i = 0; i < ids.length; i++) {
            if (i == ids.length - 1){
                builder.append("?");
                break;
            }
            builder.append("?, ");
        }
        builder.append(");");
        return builder.toString();
    }
}
