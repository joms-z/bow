package yelp;

/**
 * Created by Joms on 6/29/2016.
 */
public class YelpQuery {
    private String name;
    private String location;

    public YelpQuery(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
