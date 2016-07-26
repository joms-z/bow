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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        YelpQuery other = (YelpQuery) obj;
        return (this.name.equals(other.getName()) && this.location.equals(other.getLocation()));
    }

    @Override
    public int hashCode() {
        return (this.name+this.location).hashCode();
    }
}
