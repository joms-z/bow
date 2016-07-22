package factual;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.Counter;
import util.DBService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static util.JSONUtils.getJSONObjectFromFile;
import static util.JSONUtils.toList;

/**
 * Created by Joms on 7/5/2016.
 */
public class FactualManager {
    private static JSONObject storeBow2FactualCatId;
    private static JSONObject factualCatId2Labels;
    //Set of merchants that do not have factual bow
    private static Set<String> mcpPatchMerchants;
    private static final int DEFAULT_LIMIT = 2;

    static {
        loadStoreBow2FactualCatIdMapping();
        loadFactualCatId2LabelsMapping();
        loadMcpPatchMerchants();
    }

    //TODO: Resource Management
    public static void loadStoreBow2FactualCatIdMapping() {
        storeBow2FactualCatId = getJSONObjectFromFile("C:\\Users\\Joms\\Desktop\\store_bow_2_id.json");
    }

    public static void loadFactualCatId2LabelsMapping() {
        factualCatId2Labels = getJSONObjectFromFile("C:\\Users\\Joms\\Desktop\\factual_taxonomy_with_all.json");
    }

    public static void loadMcpPatchMerchants() {
        mcpPatchMerchants = new HashSet<>();
        String fileName = "C:\\Users\\Joms\\Desktop\\mcpPatchFactualBows_13KSubset.csv";
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] merchantRow = line.split(",");
                mcpPatchMerchants.add(merchantRow[0]);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    //TODO: Change static variable to have a hashMap instead of a json
    public List<String> getFactualCatLabels(String factualCatId) {
        JSONArray categoryLabels =
                (JSONArray)((JSONObject) factualCatId2Labels.get(factualCatId)).get("all_labels");
        List<String> labels = toList(categoryLabels, String.class);
        return labels;
    }

    public List<String> getFactualCatParents(String factualCatId) {
        JSONArray parents = (JSONArray) (((JSONObject) factualCatId2Labels.get(factualCatId)).get("parents"));
        List<String> p = toList(parents, String.class);
        return p;
    }

    public String findPrimaryFactualCatIdOfStore(String storeFactualBOW) {
        /*
            Given the factual bow stored in the merchant directory, this function finds the
            primary factual category id corresponding to it
         */
        String factualCatId = null;
        boolean checked = false;
        int separatorIndex;

        if (storeFactualBOW==null || storeFactualBOW.trim().equals(""))
            return null;

        while(factualCatId==null && !checked) {
            if (storeBow2FactualCatId.containsKey(storeFactualBOW))
                factualCatId = (String) storeBow2FactualCatId.get(storeFactualBOW);
            else {
                separatorIndex = storeFactualBOW.lastIndexOf('|');
                if (separatorIndex == -1)
                    checked = true;
                else
                    storeFactualBOW = storeFactualBOW.substring(0, separatorIndex);
            }
        }
        return factualCatId;
    }

    public List<String> findPrimaryFactualCatIdsOfMerchant(List<String> storeFactualBows, int limit) {
        /*
          Given a list of storeBows of all stores of a merchant, finds the primary factual category id
          of all stores and returns a list of factual category ids in descending order of frequency.
         */
        Counter<String> factualCategoryIds = new Counter<>();
        for (String storeFactualBow : storeFactualBows)
            if (storeFactualBow!=null && !storeFactualBow.trim().equals(""))
                factualCategoryIds.add(findPrimaryFactualCatIdOfStore(storeFactualBow));

        Set<String> factualCategoryIdsToRemove = new HashSet<>();
        for (String factualCategoryId : factualCategoryIds.keySet()) {
            List p = getFactualCatParents(factualCategoryId);
            factualCategoryIdsToRemove.addAll(p);
        }

        for (String factualCategoryId : factualCategoryIdsToRemove)
            factualCategoryIds.remove(factualCategoryId);

        return factualCategoryIds.mostCommon(limit);
    }


    public List<String> findPrimaryFactualCatIdsOfMerchant(List<String> storeFactualBows) {
        /*
          Given a list of storeBows of all stores of a merchant, finds the primary factual category id
          of all stores and returns a list of factual category ids in descending order of frequency.
         */
        return findPrimaryFactualCatIdsOfMerchant(storeFactualBows, storeFactualBows.size());
    }

    public List<MerchantFactualCategory> findPrimaryFactualCatIdsOfMerchants(Collection<String> merchantIds) {
        /*
          Given a list of merchantIds, for each merchantId, finds the primary factual category id
          of all stores and returns a list of factual category ids in descending order of frequency.
         */

        //TODO: merchantIds without factual categories are now not returned.
        StringBuilder query = new StringBuilder("SELECT merchant_id, id, bow FROM smartfin.Store where active=1 AND " +
                "merchant_id IN (");

        //TODO: merchantIds without factual categories are now not returned.
        List<String> mcpMerchants = new ArrayList<>();
        for (String merchantId : merchantIds) {
            if (mcpPatchMerchants.contains(merchantId))
                mcpMerchants.add(merchantId);
            else
                query.append(merchantId + ",");
        }
        if (query.charAt(query.length()-1) == ',')
            query.deleteCharAt(query.length()-1);
        query.append(")");

        //TODO: Move to config/property files.
        //String dbUrl = "jdbc:mysql://sf-prod-tier4-01.obisolution.com:1194";
        String dbUrl = "jdbc:mysql://sf-dev-amc-01.obisolution.com:1194";
        //String dbUserName = "";
        String dbUserName = "joms.zacharia";
        //String dbPassword = "";
        String dbPassword = "SF@1841b";
        String database = "smartfin";
        String driver = "com.mysql.jdbc.Driver";

        DBService db = new DBService(dbUserName, dbPassword, dbUrl, database, driver);
        Map<String, List<List<String>>> result = db.connectAndFetchDataAsMap(query.toString(), 1, new int[] {2, 3});

        List<MerchantFactualCategory> merchantsFactualCats = new ArrayList<>();
        ArrayList<String> storeFactualBows = new ArrayList<>();
        for (String key : result.keySet()) {
            //Each key corresponds to a merchant
            storeFactualBows.clear();
            for (List<String> row : result.get(key)) {
                //add the storeFactualBow for that store
                storeFactualBows.add(row.get(1));
            }
            List<String> primaryFactualCatIds = findPrimaryFactualCatIdsOfMerchant(storeFactualBows);
            //TODO: merchantIds without factual categories are now not returned.
            if (primaryFactualCatIds.size() > 0) {
                List<String> primaryFactualCatLabel = getFactualCatLabels(primaryFactualCatIds.get(0));
                merchantsFactualCats.add(new MerchantFactualCategory(key, primaryFactualCatIds,
                        primaryFactualCatLabel));
            }
        }
        return  merchantsFactualCats;
    }


    public MerchantFactualCategory findPrimaryFactualCatIdsOfMerchant(String merchantId) {
        /*
          Given a merchantId, finds the primary factual category id
          of all stores and returns a list of factual category ids in descending order of frequency.
         */
        List<String> merchantIds = new ArrayList<>();
        merchantIds.add(merchantId);
        List<MerchantFactualCategory> res = findPrimaryFactualCatIdsOfMerchants(merchantIds);
        return res.get(0);
    }

    public static void main(String[] args) {
        FactualManager a1 = new FactualManager();
        List<String> merchantIds = new ArrayList<>();
        merchantIds.add("310037777");
        merchantIds.add("2360213527");
        merchantIds.add("1530236004");
        merchantIds.add("2324637060");
        merchantIds.add("2326554929");

        System.out.println(a1.findPrimaryFactualCatIdsOfMerchants(merchantIds));
    }
}
