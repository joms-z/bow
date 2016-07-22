package util;

import java.sql.*;
import java.util.*;
import java.util.Date;


public class DBService {
    private String dbUserName;
    private String dbPassword;
    private String dbURL;
    private String database;
    private String driver;
    private boolean batchUpdate;

    public DBService(String dbUserName, String dbPassword, String dbURL, String database, String driver) {
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.dbURL = dbURL;
        this.database = database;
        this.driver = driver;
        this.batchUpdate = false;
    }

    public DBService(String dbUserName, String dbPassword, String dbURL, String database, String driver,
                     boolean batchUpdate) {
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.dbURL = dbURL;
        this.database = database;
        this.driver = driver;
        this.batchUpdate = batchUpdate;
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(driver).newInstance();
            if (!batchUpdate)
                conn = DriverManager.getConnection(dbURL + "/" + database, dbUserName, dbPassword);
            else
                conn = DriverManager.getConnection(dbURL + "/" + database + "?rewriteBatchedStatements=true",
                        dbUserName, dbPassword);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void closeConnection(Connection dbConnection) {
        try {
            dbConnection.close();
        } catch (SQLException e1) {
            System.out.println("Could not close database connection");
            e1.printStackTrace();
        }
    }

    public Map<String, List<List<String>>> connectAndFetchDataAsMap(String query, int keyIndex, int[] otherIndices) {
        /*
          This function returns the result from the db as a map.
          keyIndex - the field index in the query that should be used as the key for the map
          otherIndices - the other field indices that would be grouped as a list of strings
          Each key would map to a list of list of strings where the inner list of strings corresponds to
          one row in the ResultSet and the outer list would contain different rows that map to the same
          keyField.
         */
        Connection conn = getConnection();
        Statement s = null;
        ResultSet rs = null;
        Map<String, List<List<String>>> result = new HashMap<>();

        try {
            s = conn.createStatement();
            s.executeQuery(query);
            rs = s.getResultSet();

            String keyField;
            while (rs.next()) {
                List<String> otherFields = new ArrayList<>();
                keyField = rs.getString(keyIndex);
                for (int i=0; i<otherIndices.length; i++)
                    otherFields.add(rs.getString(otherIndices[i]));
                result.computeIfAbsent(keyField, k -> new ArrayList<>()).add(otherFields);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (rs != null) rs.close();
                if (s != null) s.close();
                if (conn != null) conn.close();
            }
            catch (SQLException e) { e.printStackTrace(); }
        }

        return result;
    }

    public static String createInClause(Collection<String> items) {
        StringBuilder inClause = new StringBuilder("");
        for (String s : items) {
            inClause.append(s);
            inClause.append(",");
        }
        inClause.deleteCharAt(inClause.length() - 1);
        return inClause.toString();
    }

    public static Timestamp getCurrentTimeStamp() {
        Date today = new Date();
        return new Timestamp(today.getTime());
    }

    public static long getCurrentUnixTimestamp() {
        return System.currentTimeMillis()/1000L;
    }

    public static void main(String[] args) {
        System.out.println(getCurrentTimeStamp());
    }

}