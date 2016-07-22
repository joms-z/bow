package bow;

import sel.MerchantDetails;
import sel.MerchantSelector;
import sel.Store;
import sel.TransactionLoader;
import util.DBService;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;

import static util.DBService.closeConnection;
import static util.DBService.getCurrentUnixTimestamp;
import static util.FileUtils.createFileIfNotExists;
import static util.StringUtils.joinList;
import static util.StringUtils.stringOr;
import static yelp.YelpResult.categoriesDelimiter;

/**
 * Created by Joms on 7/11/2016.
 */
public class BowManager {

    private static final String sfStoreFileName4Sq = "";
    private static final String merchantFileName4Sq = "";
    private static final String yelpBowFileName4Sq = "";

    private static final String bowDbUrl = "jdbc:mysql://sf-dev-amc-01.obisolution.com:1194";
    private static final String bowDbUserName = "joms.zacharia";
    private static final String bowDbPassword = "SF@1841b";
    private static final String bowDatabase = "smartfin";
    private static final String bowDriver = "com.mysql.jdbc.Driver";

    public void collectBowPipeline() {
        Map<String, MerchantDetails> uniqueMerchantsToUpdate = new TransactionLoader().loadTransactions();
        new MerchantSelector(uniqueMerchantsToUpdate).process();
        new BowAccumulator().addBow(uniqueMerchantsToUpdate);
        write4sqFiles(uniqueMerchantsToUpdate);
        updateDatabase(uniqueMerchantsToUpdate);
    }

    private void write4sqFiles(Map<String, MerchantDetails> uniqueMerchantsToUpdate) {
        try {
            File sfStoreFile = createFileIfNotExists(sfStoreFileName4Sq);
            File merchantFile = createFileIfNotExists(merchantFileName4Sq);
            File yelpBowFile = createFileIfNotExists(yelpBowFileName4Sq);

            Writer sfStoreWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(sfStoreFile), "utf-8"));
            Writer merchantWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(merchantFile), "utf-8"));
            Writer yelpBowWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(yelpBowFile), "utf-8"));

            for (String merchantId : uniqueMerchantsToUpdate.keySet()) {
                //merchantId write
                MerchantDetails merchantDetails = uniqueMerchantsToUpdate.get(merchantId);
                merchantWriter.write(merchantId);
                merchantWriter.write("\n");

                //sfStore write
                for (Store s : uniqueMerchantsToUpdate.get(merchantId).getStores()) {
                    String merchantName = stringOr(merchantDetails.getName(),
                            "NULL");
                    String storeName = stringOr(s.getName(), "NULL");
                    String storeCity = stringOr(s.getCity(), "NULL");
                    String storeState = stringOr(s.getState(), "NULL");
                    sfStoreWriter.write(merchantId + "\t" + merchantName + "\t" +
                            storeName + "\t" + storeCity + "\t" + storeState);

                    sfStoreWriter.write("\n");
                }

                //yelpBow write
                Bow bow = merchantDetails.getBow();
                String yelpBow = bow.getYelp().toString();
                String factualBow = bow.getFactual().toString();
                yelpBowWriter.write(merchantId + "\t" + yelpBow + "\t" + factualBow);
                yelpBowWriter.write("\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDatabase(Map<String, MerchantDetails> uniqueMerchantsToUpdate) {
        String insertBow = "";
        DBService dbService = new DBService(bowDbUserName, bowDbPassword, bowDbUrl, bowDatabase, bowDriver, true);
        Connection conn = dbService.getConnection();
        PreparedStatement s = null;

        try {
            s  = conn.prepareStatement("INSERT INTO MerchantBow (active, mr_id, supported, revision, created_on, " +
                    "modified_on, modified_by, supported_on, mr_name, yelp, factual, 4sq1, 4sq2, title, validated, " +
                    "category_id VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?))");
            for (String merchantId : uniqueMerchantsToUpdate.keySet()) {
                MerchantDetails merchantDetails = uniqueMerchantsToUpdate.get(merchantId);
                Bow bow = merchantDetails.getBow();

                s.setString(1, "1");
                s.setString(2, merchantId);
                s.setString(3, "1");
                s.setString(4, "0");
                s.setLong(5, getCurrentUnixTimestamp());
                s.setLong(6, getCurrentUnixTimestamp());
                s.setString(7, "bowCollector");
                s.setNull(8, Types.BIGINT);
                s.setString(9, merchantDetails.getName());
                s.setString(10, joinList(bow.getYelp(), categoriesDelimiter));
                s.setString(11, joinList(bow.getFactual(), categoriesDelimiter));
                s.setString(12, joinList(new ArrayList<>(), categoriesDelimiter));
                s.setString(13, joinList(new ArrayList<>(), categoriesDelimiter));
                s.setString(14, joinList(new ArrayList<>(), categoriesDelimiter));
                s.setString(15, "");
                s.setString(16, "");

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
        new BowManager().collectBowPipeline();
    }
}
