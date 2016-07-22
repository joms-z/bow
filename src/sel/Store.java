package sel;

/**
 * Created by Joms on 6/30/2016.
 */
public class Store {
    private String id;
    private String name;
    private String streetAddress;
    private String city;
    private String state;

    public Store(String id, String name, String streetAddress, String city, String state) {
        this.id = id;
        this.name = name;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }
}
