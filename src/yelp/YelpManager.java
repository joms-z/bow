package yelp;

import util.DBService;

import java.sql.*;
import java.util.*;

import static util.DBService.closeConnection;
import static util.DBService.createInClause;
import static util.StringUtils.joinList;
import static yelp.YelpAPI.queryAPI;
import static yelp.YelpResult.categoriesDelimiter;

import com.wantedtech.common.xpresso.strings.FuzzyWuzzy;

/**
 * Created by Joms on 6/29/2016.
 */
public class YelpManager {
    //TODO: Load these from a properties file
    private static final String yelpCacheDbUrl = "jdbc:mysql://sf-dev-amc-01.obisolution.com:1194";
    private static final String yelpCacheDbUserName = "joms.zacharia";
    private static final String yelpCacheDbPassword = "SF@1841b";
    private static final String yelpCacheDatabase = "smartfin";
    private static final String yelpCacheDriver = "com.mysql.jdbc.Driver";

    private static YelpAPI yelpAPI;
    //static cache
    private static Map<YelpQuery, YelpResult> yelpCache;
    //TODO:
    //Rows to add to cache. This is updated whenever getYelpResults is called with updateDbCache = true
    private static Set<YelpQueryResult> yelpQueryResultSetToAddToDb;
    static {
        yelpAPI = new YelpAPI();
        loadYelpCache();
        yelpQueryResultSetToAddToDb = new HashSet<>();
    }

    private static void loadYelpCache() {
        yelpCache = new HashMap<>();
    }

    private void partialLoadYelpCache(List<YelpQuery> yelpQueries) {
        /*
           This function loads only the specified keys from the database into the
           in-memory cache.
          */

        //TODO: Unwanted memory usage and iteration to generate in clause.
        List<String> queryNames = new ArrayList<>();
        List<String> queryLocations = new ArrayList<>();
        for (YelpQuery yelpQuery : yelpQueries) {
            if (!yelpCache.containsKey(yelpQuery)) {
                queryNames.add(yelpQuery.getName());
                queryLocations.add(yelpQuery.getLocation());
            }
        }
        String inClauseNames = createInClause(queryNames);
        String inClauseLocations = createInClause(queryLocations);

        String loadCacheQuery = "SELECT query_name, query_location, yelp_name, yelp_address, yelp_city, yelp_state, " +
                "yelp_categories FROM smartfin.YelpCache2 WHERE query_name IN (" + inClauseNames +
                ") AND query_location IN (" + inClauseLocations + ");";

        System.out.println(loadCacheQuery);

        Connection conn = new DBService(yelpCacheDbUserName, yelpCacheDbPassword, yelpCacheDbUrl, yelpCacheDatabase,
                yelpCacheDriver).getConnection();
        Statement s = null;
        ResultSet rs = null;

        try {
            s = conn.createStatement();
            s.executeQuery(loadCacheQuery);
            rs = s.getResultSet();

            while (rs.next())
                yelpCache.put(new YelpQuery(rs.getString("query_name"), rs.getString("query_location")),
                        new YelpResult(rs.getString("yelp_name"), rs.getString("yelp_address"), rs.getString("yelp_city"),
                                rs.getString("yelp_state"), rs.getString("yelp_categories")));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (s != null) s.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            closeConnection(conn);
        }
    }

    public static String createQueryLocation(String address, String city, String state) {
        address = address==null? "":address.trim();
        city = city==null? "":city.trim();
        state = state==null? "":state.trim();

        StringBuilder queryLocation = new StringBuilder(address);
        if (!address.equals("") && !city.equals("")) queryLocation.append(",").append(city);
        else queryLocation.append(city);

        if (!queryLocation.toString().equals("") && !state.equals("")) queryLocation.append(",").append(state);
        else queryLocation.append(state);

        return  queryLocation.toString();
    }

    public YelpResult getFromYelpCache(YelpQuery yelpQuery) {
        if (yelpQuery==null) return null;
        return yelpCache.getOrDefault(yelpQuery, null);
    }

    public YelpResult getYelpResult(YelpQuery yelpQuery) {
        //Does not load the yelp cache nor updates database.

        YelpResult yelpResult = getFromYelpCache(yelpQuery);
        if (yelpResult != null)
            return yelpResult;

        yelpResult = queryAPI(yelpAPI, yelpQuery);
        yelpQueryResultSetToAddToDb.add(new YelpQueryResult(yelpQuery, yelpResult));
        return yelpResult;
    }

    public YelpResult getYelpResult(String queryName, String queryAddress, String queryCity, String queryState) {
        //Does not load the yelp cache nor updates database.

        YelpQuery yelpQuery = new YelpQuery(queryName, createQueryLocation(queryAddress, queryCity, queryState));
        return getYelpResult(yelpQuery);
    }

    public boolean verifyYelpResult(YelpQuery yelpQuery, YelpResult yelpResult) {
        if (FuzzyWuzzy.ratio(yelpQuery.getName(), yelpResult.getName()) > 81) return true;
        if (FuzzyWuzzy.partial_ratio(yelpQuery.getName(), yelpResult.getName()) > 95 &&
                FuzzyWuzzy.ratio(yelpQuery.getName(), yelpQuery.getLocation()) > 65)
            return true;
        if (FuzzyWuzzy.token_sort_ratio(yelpQuery.getName(), yelpResult.getName(), null) > 90)
            return true;

        return false;
    }

    public List<YelpResult> getYelpResults(List<YelpQuery> yelpQueries, boolean updateDbCache) {
        /*
          Returns an ArrayList of yelpResults parallel to the input ArrayList of yelpQueries
           1. first updates the in-memory cache from the database.
           2. verifies whether each result is correct
           3. updates db cache
         */
        List<YelpResult> yelpResults = new ArrayList<>();
        partialLoadYelpCache(yelpQueries);
        for (YelpQuery yelpQuery : yelpQueries) {
            YelpResult yelpResult = getYelpResult(yelpQuery);
            if (yelpResult != null && (verifyYelpResult(yelpQuery, yelpResult)))
                yelpResults.add(yelpResult);
            else
                yelpResults.add(null);
        }

        if (updateDbCache)
            updateDbCache();

        return yelpResults;
    }

    public List<YelpResult> getYelpResults(List<YelpQuery> yelpQueries) {
        return getYelpResults(yelpQueries, true);
    }

    private static void updateDbCache() {
        DBService dbService = new DBService(yelpCacheDbUserName, yelpCacheDbPassword, yelpCacheDbUrl, yelpCacheDatabase,
                yelpCacheDriver, true);
        Connection conn = dbService.getConnection();
        PreparedStatement s = null;

        try {
            s  = conn.prepareStatement("INSERT INTO YelpCache2 (query_name, query_location, " +
                    "yelp_name, yelp_address, yelp_city, yelp_state, yelp_categories) VALUES (?,?,?, ?, ?, ?, ?)");
            for (YelpQueryResult yqr : yelpQueryResultSetToAddToDb) {
                String name="", address="", city="", state="";
                List<String> categories=null;
                YelpResult yr = yqr.getYelpResult();
                if (yr != null) {
                    name = yr.getName();
                    address = yr.getAddress();
                    city = yr.getCity();
                    state = yr.getState();
                    categories = yr.getCategories();
                }
                s.setString(1, yqr.getYelpQuery().getName());
                s.setString(2, yqr.getYelpQuery().getLocation());
                s.setString(3, name);
                s.setString(4, address);
                s.setString(5, city);
                s.setString(6, state);
                s.setString(7, joinList(categories, categoriesDelimiter));

                s.addBatch();
            }
            s.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (s != null) s.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            closeConnection(conn);
        }
    }

    public static void main(String[] args) {
        String s = "[Abcdefg, asdasd _ asdasd / asdasd & asdasd & ";
        String[] s1 = s.split("[,_/&]");
        for (String s2 : s1) {
            System.out.println(s2.trim());
        }
    }
}
