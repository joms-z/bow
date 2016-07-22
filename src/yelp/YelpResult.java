package yelp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Joms on 6/29/2016.
 */
public class YelpResult {
    public static final char categoriesDelimiter = '|';

    private String name;
    private String address;
    private String city;
    private String state;
    private List<String> categories;

    public YelpResult(String name, String address, String city, String state, String categories) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;

        if (categories != null && !categories.trim().equals(""))
            this.categories = Arrays.asList(categories.split(Character.toString(categoriesDelimiter)));
        else
            this.categories = null;
    }

    public YelpResult(JSONObject yelpResult) {
        name = (String)yelpResult.get("name");

        //address
        JSONArray addresses = (JSONArray)((JSONObject)yelpResult.get("location")).get("address");
        if (addresses.size() > 0)
            address = (String)addresses.get(0);
        else
            address = "";

        city = (String)((JSONObject)yelpResult.get("location")).get("city");
        state = (String)((JSONObject)yelpResult.get("location")).get("state_code");

        //categories
        JSONArray c = (JSONArray) yelpResult.getOrDefault("categories", null);
        if (c != null) {
            categories = new ArrayList<>();
            for (int i = 0; i < c.size(); i++)
                categories.add((String) ((JSONArray) c.get(i)).get(0));
        }
        else
            categories = null;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> preprocess() {
        List<String> categoryWords = new ArrayList<>();
        for (String category : categories) {
            String [] words = category.split("[,_/&]");
            for (String word : words) {
                if (!word.trim().equals(""))
                    categoryWords.add(word.replace("'", "").replace("-", " ").toLowerCase().trim());
            }
        }
        return categoryWords;
    }

    @Override
    public String toString() {
        return "YelpResult{" +
                "categoriesDelimiter='" + categoriesDelimiter + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", categories=" + categories +
                '}';
    }
}
