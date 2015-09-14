package il.co.nolife.locotalk.DataTypes;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;

/**
 * Created by Victor Belski on 9/11/2015.
 */
public class LocoEvent {

    long id;
    String name;
    String owner;
    GeoPt location;
    int radius;
    long conversation;

    public long getId() { return id; }

    public void setId(long forumId) { this.id = forumId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt locaction) {
        this.location = locaction;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public long getConversation() {
        return conversation;
    }

    public void setConversation(long conversation) {
        this.conversation = conversation;
    }

}
