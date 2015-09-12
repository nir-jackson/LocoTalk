package il.co.nolife.locotalk;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserLocation;

/**
 * Created by Victor Belski on 9/10/2015.
 */
public class LocoUser {

    String regId;
    String mail;
    String name;
    String icon;
    GeoPt location;
    Boolean safe;

    public LocoUser() {
        mail = "";
        name = "";
        icon = "";
        location = new GeoPt();
        safe = false;
    }

    public LocoUser(User user) {

        regId = user.getRegistrationId();
        mail = user.getMail();
        name = user.getFullName();
        icon = user.getImageUrl();
        location = user.getLocation().getPoint();
        safe = false;

    }

    public LocoUser(UserAroundMe user) {

        regId = "Nothing";
        mail = user.getMail();
        name = user.getDisplayName();
        location = user.getLocation();
        safe = false;
        icon = "";

    }

    public User toUser() {

        User user = new User();

        user.setRegistrationId(regId);
        user.setMail(mail);
        user.setFullName(name);
        user.setImageUrl(icon);
        user.setLocation(new UserLocation().setPoint(location));

        return user;

    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public Boolean getSafe() {
        return safe;
    }

    public void setSafe(Boolean safe) {
        this.safe = safe;
    }
}
