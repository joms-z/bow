package yelp;

/**
 * Created by Joms on 7/11/2016.
 */
public class YelpQueryResult {
    private YelpQuery yelpQuery;
    private YelpResult yelpResult;

    public YelpQueryResult(YelpQuery yelpQuery, YelpResult yelpResult) {
        this.yelpQuery = yelpQuery;
        this.yelpResult = yelpResult;
    }

    public YelpQuery getYelpQuery() {
        return yelpQuery;
    }

    public YelpResult getYelpResult() {
        return yelpResult;
    }
}
