package sel;

import util.DBService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static util.DBService.closeConnection;
import static util.DBService.createInClause;

/**
 * Created by Joms on 6/29/2016.
 */
public class MerchantSelector {
    //TODO: Connection pooling, prepared statements, better db service class(with option to both stream query results
    //      and get them as a whole), avoid duplicate code
    //TODO: Load these from a properties file
    //TODO: Temporary Hack having two connection info
    //private static final String storesDbUrl = "jdbc:mysql://sf-prod-tier4-01.obisolution.com:1194";
    private static final String storesDbUrl = "jdbc:mysql://sf-dev-amc-01.obisolution.com:1194";
    //private static final String storesDbUserName = "joms";
    private static final String storesDbUserName = "joms.zacharia";
    //private static final String storesDbPassword = "";
    private static final String storesDbPassword = "SF@1841b";
    private static final String storesDatabase = "smartfin";
    private static final String storesDriver = "com.mysql.jdbc.Driver";

    private static final String mrBowDbUrl = "jdbc:mysql://sf-an1.smartfinancellc.biz:1194";
    private static final String mrBowDbUserName = "root";
    private static final String mrBowDbPassword = "ding1bat";
    private static final String mrBowDatabase = "smartfin";
    private static final String mrBowDriver = "com.mysql.jdbc.Driver";

    //Yelp limit
    private static final int merchantsLimit = 15000;
    private static final int storesPerMerchant = 5;

    Map<String, MerchantDetails> uniqueMerchantsToUpdate;

    public MerchantSelector(Map<String, MerchantDetails> uniqueMerchantsToUpdate) {
        this.uniqueMerchantsToUpdate = uniqueMerchantsToUpdate;
    }

    public void removeProcessedMerchants() {
        Set<String> merchants = uniqueMerchantsToUpdate.keySet();
        String inClauseMerchants = createInClause(merchants);

        //TODO: Use PreparedStatement or temporary table
        String processedMerchantsQuery = "SELECT merchant_id FROM smartfin.MerchantBOW WHERE " +
                "merchant_id IN (" + inClauseMerchants + ");";

        Connection conn = new DBService(mrBowDbUserName, mrBowDbPassword, mrBowDbUrl, mrBowDatabase, mrBowDriver).getConnection();
        Statement s = null;
        ResultSet rs = null;

        try {
            s = conn.createStatement();
            s.executeQuery(processedMerchantsQuery);
            rs = s.getResultSet();

            String merchantId;
            while (rs.next()) {
                uniqueMerchantsToUpdate.remove(rs.getString("merchant_id"));
            }

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

    public void limitMerchants() {
        int merchantsCount = uniqueMerchantsToUpdate.size();
        int deletedCount = 0;
        Iterator<Map.Entry<String,MerchantDetails>> iter = uniqueMerchantsToUpdate.entrySet().iterator();
        while (iter.hasNext()) {
            if (deletedCount >= merchantsCount - merchantsLimit)
                break;
            Map.Entry<String,MerchantDetails> entry = iter.next();
            iter.remove();
            deletedCount++;
        }
    }

    public void addNStores() {
        StringBuilder getNStoresQuery = new StringBuilder();
        Set<String> merchantKeys = uniqueMerchantsToUpdate.keySet();
        //System.out.println(merchantKeys.size());
        for (String s : merchantKeys)
            getNStoresQuery.append("SELECT merchant_id, id, name, address, city, state " +
                    "FROM smartfin.Store WHERE " +
                    "active=1 AND merchant_id=" + s + " LIMIT " + storesPerMerchant + " UNION ALL ");

        String q = getNStoresQuery.toString().substring(0, getNStoresQuery.length()-11);
        System.out.println(q);

        Connection conn = new DBService(storesDbUserName, storesDbPassword, storesDbUrl, storesDatabase,
                storesDriver).getConnection();
        Statement s = null;
        ResultSet rs = null;

        try {
            s = conn.createStatement();
            s.executeQuery(q);
            rs = s.getResultSet();

            String merchantId;
            while (rs.next()) {
                merchantId = rs.getString("merchant_id");
                uniqueMerchantsToUpdate.get(merchantId).addStore(rs.getString("id"), rs.getString("name"),
                        rs.getString("address"), rs.getString("city"), rs.getString("state"));
            }

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

    public void process() {
        removeProcessedMerchants();
        limitMerchants();
        addNStores();
    }

    public static void main(String[] args) {
        new MerchantSelector(new TransactionLoader().loadTransactions()).process();
    }
}
