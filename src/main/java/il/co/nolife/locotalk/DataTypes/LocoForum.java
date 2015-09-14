package il.co.nolife.locotalk.DataTypes;

import java.util.List;

/**
 * Created by Victor Belski on 9/8/2015.
 */
public class LocoForum extends LocoEvent {

    List<LocoUser> users;

    public List<LocoUser> getUsers() { return users; }

    public void setUsers(List<LocoUser> users) {
        this.users = users;
    }

}
