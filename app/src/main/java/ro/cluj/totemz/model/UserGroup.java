package ro.cluj.totemz.model;

import java.util.List;

/**
 * Created by mihai on 7/2/2017.
 */

public class UserGroup {
    public  String name;
    public  TotemzUser owner;
    public  List<TotemzUser> users;

    public UserGroup(String name, TotemzUser owner, List<TotemzUser> users) {
        this.name = name;
        this.owner = owner;
        this.users = users;
    }
    public UserGroup(){

    }
}
