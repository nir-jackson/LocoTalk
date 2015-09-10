package il.co.nolife.locotalk;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;

import java.util.List;

/**
 * Created by Victor Belski on 9/8/2015.
 */
public class Forum {

    long forumId;
    String name;
    List<LocoUser> users;
    GeoPt loc;

    public long getForumId() { return forumId; }

    public void setForumId(long forumId) { this.forumId = forumId; }

    public String getName() { return name; }

    public void setName(String namr) { this.name = name; }

    public List<LocoUser> getUsers() { return users; }

    public void setUsers(List<LocoUser> users) {
        this.users = users;
    }

    public GeoPt getLoc() {
        return loc;
    }

    public void setLoc(GeoPt loc) {
        this.loc = loc;
    }
}
