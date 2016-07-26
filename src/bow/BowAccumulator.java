package bow;

import factual.FactualManager;
import factual.MerchantFactualCategory;
import sel.MerchantDetails;
import sel.Store;
import yelp.YelpManager;
import yelp.YelpQuery;
import yelp.YelpResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static factual.MerchantFactualCategory.preprocessFactualCatLabels;
import static yelp.YelpManager.createQueryLocation;

/**
 * Created by Joms on 7/11/2016.
 */
public class BowAccumulator {

    //This function should add the respective bow(yelp/factual) to uniqueMerchantsToUpdate
    public void addBow(Map<String, MerchantDetails> uniqueMerchantsToUpdate) {
        //TODO: Optimize and refactor
        List<String> merchantIds = new ArrayList<>(uniqueMerchantsToUpdate.keySet());
        //get factual bow
        FactualManager factualManager = new FactualManager();
        List<MerchantFactualCategory> merchantFactualCategories =
                factualManager.findPrimaryFactualCatIdsOfMerchants(merchantIds);

        //get yelp bow
        List<YelpQuery> yelpQueries = new ArrayList<>();
        for (String merchantId : merchantIds) {
            List<Store> stores = uniqueMerchantsToUpdate.get(merchantId).getStores();
            String queryName = "", queryAddress = "", queryCity = "", queryState = "";
            Store store;
            if (stores.size() > 0) {
                store = stores.get(0);
                if (store != null) {
                    queryName = store.getName();
                    queryAddress = store.getStreetAddress();
                    queryCity = store.getCity();
                    queryState = store.getState();
                }
            }
            yelpQueries.add(new YelpQuery(queryName, createQueryLocation(queryAddress, queryCity, queryState)));
        }
        YelpManager yelpManager = new YelpManager();
        List<YelpResult> yelpResults = yelpManager.getYelpResults(yelpQueries, false);


        assert merchantIds.size() == yelpResults.size();
        //add yelp bow to uniqueMerchantsToUpdate
        for (int i=0; i<merchantIds.size(); i++) {
            YelpResult yelpResult = yelpResults.get(i);
            if (yelpResult != null)
                uniqueMerchantsToUpdate.get(merchantIds.get(i)).getBow().setYelp(yelpResult.preprocess());
            else
                uniqueMerchantsToUpdate.get(merchantIds.get(i)).getBow().setYelp(null);
        }

        //add factual bow to uniqueMerchantsToUpdate
        for (MerchantFactualCategory mfc : merchantFactualCategories)
            uniqueMerchantsToUpdate.get(mfc.getMerchantId()).getBow().setFactual(
                    preprocessFactualCatLabels(mfc.getPrimaryFactualCategoryLabel()));
    }
}
