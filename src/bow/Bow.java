package bow;

import java.util.List;

/**
 * Created by Joms on 7/11/2016.
 */
public class Bow {
    private List<String> yelp;
    private List<String> factual;

    public Bow(List<String> yelp, List<String> factual) {
        this.yelp = yelp;
        this.factual = factual;
    }

    public Bow() {
        yelp = null;
        factual = null;
    }

    public List<String> getYelp() {
        return yelp;
    }

    public List<String> getFactual() {
        return factual;
    }

    public void setYelp(List<String> yelp) {
        this.yelp = yelp;
    }

    public void setFactual(List<String> factual) {
        this.factual = factual;
    }
}
