package ro.cluj.totemz.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mihai on 7/2/2017.
 */

public class TotemzUser {


    public String email;
    public String name;
    public LatLng location;

    TotemzUser() {

    }

    public TotemzUser(String email, String name, LatLng location) {
        this.email = email;
        this.name = name;
        this.location = location;
    }
}
