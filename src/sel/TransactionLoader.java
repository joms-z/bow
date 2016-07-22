package sel;

import util.DBService;
import util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static util.DBService.closeConnection;

/**
 * Created by Joms on 6/30/2016.
 */
public class TransactionLoader {
    //TODO: Load these from a properties file
    private static final String dbUrl = "jdbc:mysql://sf-prod-tier4-01.obisolution.com:1194";
    private static final String dbUserName = "joms";
    private static final String dbPassword = "fhQyvyIDnfcrfeJmcX4mgaR";
    private static final String database = "smartfin";
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String txTable = "TestRun";
    private static final int tx_count = 25000;

    private static int attempts = 0;

    public Map<String, MerchantDetails> loadTransactions() {
        Map<String, MerchantDetails> uniqueMerchantsToUpdate= new HashMap<>();

        String loadTxQuery = "SELECT tx_id, mr_id AS merchant_id, st_id AS store_id, " +
                "mr_name AS merchant_name, st_name AS store_name, " +
                "st_address AS store_street_address, st_city AS store_city, st_state AS store_state " +
                "FROM "+txTable+" WHERE source<>'REST' and source<>'TEST_TX' AND " +
                "((score > 0.4 and run_id < 20160423000000) or (score >= 0.65 and run_id >= 20160423000000)) " +
                "ORDER BY tx_id LIMIT "+tx_count+" OFFSET 0;";

        Connection conn = new DBService(dbUserName, dbPassword, dbUrl, database, driver).getConnection();
        Statement s = null;
        ResultSet rs = null;

        try {
            s = conn.createStatement();
            s.executeQuery(loadTxQuery);
            rs = s.getResultSet();

            String merchantId;
            while (rs.next()) {
                merchantId = rs.getString("merchant_id");
                //TODO: These transactions should be marked as processed.
                if (!StringUtils.isField(merchantId)) continue;

                if (!uniqueMerchantsToUpdate.containsKey(merchantId))
                    uniqueMerchantsToUpdate.put(merchantId, new MerchantDetails(rs.getString("merchant_name"),
                            rs.getString("store_id"), rs.getString("store_name"),
                            rs.getString("store_street_address"), rs.getString("store_city"),
                            rs.getString("store_state"), rs.getString("tx_id")));
                else {
                    uniqueMerchantsToUpdate.get(merchantId).addTransactionId(rs.getString("tx_id"));

                    //Store info is always given preference: #TF and FT
                    //(truth table for tx_dict['store_id'] and unique_merchants['merchant_id']['store_id'])
                    if (StringUtils.isField(rs.getString("store_id")) && uniqueMerchantsToUpdate.get(merchantId).areStoresEmpty())
                        uniqueMerchantsToUpdate.get(merchantId).reinitialize(rs.getString("merchant_name"),
                                rs.getString("store_id"), rs.getString("store_name"),
                                rs.getString("store_street_address"), rs.getString("store_city"), rs.getString("store_state"));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (rs != null) rs.close();
                if (s != null) s.close();
            }
            catch (SQLException e) { e.printStackTrace(); }
            closeConnection(conn);
        }
        return uniqueMerchantsToUpdate;
    }
}
