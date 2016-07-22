package sel;

import bow.Bow;
import util.StringUtils;

import java.util.ArrayList;

/**
 * Created by Joms on 6/30/2016.
 */
public class MerchantDetails {
    private String name;
    private Bow bow;
    private ArrayList<Store> stores;
    private ArrayList<String> txIds;

    public MerchantDetails() {
        name = "";
        bow = new Bow();
        stores = new ArrayList<>();
        txIds = new ArrayList<>();
    }

    public MerchantDetails(String merchantName, String storeId, String storeName, String streetAddress, String city,
                           String state, String txId) {
        name = merchantName;
        bow = new Bow();

        stores = new ArrayList<>();
        if (StringUtils.isField(storeId))
            stores.add(new Store(storeId, storeName, streetAddress, city, state));

        txIds = new ArrayList<>();
        txIds.add(txId);
    }

    public String getName() {
        return name;
    }

    public Bow getBow() {
        return bow;
    }

    public ArrayList<Store> getStores() {
        return stores;
    }

    public ArrayList<String> getTxIds() {
        return txIds;
    }

    public void addTransactionId(String txId) {
        txIds.add(txId);
    }

    public boolean areStoresEmpty() {
        return stores.size()==0;
    }

    public void reinitialize(String merchantName, String storeId, String storeName, String streetAddress, String city,
                             String state) {
        name = merchantName;
        bow = new Bow();

        if (StringUtils.isField(storeId))
            stores.add(new Store(storeId, storeName, streetAddress, city, state));
    }

    public void addStore(Store s) {
        stores.add(s);
    }

    public void addStore(String id, String name, String streetAddress, String city, String state) {
        Store s = new Store(id, name, streetAddress, city, state);
        addStore(s);
    }
}
