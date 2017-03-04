package ro.cluj.totemz.realm;

/**
 * Created by sorin on 04.03.17.
 */

public class LocationRealm {
    private String clientID;
    private Double lat;
    private Double lon;


    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
