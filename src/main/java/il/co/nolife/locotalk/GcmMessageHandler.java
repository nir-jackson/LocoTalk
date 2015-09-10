package il.co.nolife.locotalk;

import android.content.Context;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.google.api.client.util.DateTime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Belski on 9/10/2015.
 * Designed to handle all JSON based communication between LocoTal users
 */
public class GcmMessageHandler {

    // ApiHandler has to be initialized before calling this function

    public static void HandleMessage(Context context, JSONObject message) {

        String dataType = message.optString("type");
        if(dataType != null) {

            if(dataType.compareTo("ping") == 0) {

                String mail = message.optString("mail");
                User myUser = ApiHandler.GetUser();
                if(myUser != null) {
                    ApiHandler.SendGCMMessage(mail, "{'type:'pong', 'mail':'" + myUser.getMail() + "'}");
                }

            } else if(dataType.compareTo("pong") == 0) {

                String mail = message.optString("mail");
                DataAccessObject dao = new DataAccessObject(context);
                dao.ValidateFriend(mail);

            } else if(dataType.compareTo("newForum") == 0) {

                long forumId = message.optLong("forumId");
                String name = message.optString("name");
                GeoPt loc = new GeoPt();
                String lat = message.optString("lat");
                String lon = message.optString("lon");
                loc.setLatitude(Float.parseFloat(lat));
                loc.setLongitude(Float.parseFloat(lon));
                JSONArray arr = message.optJSONArray("participants");
                List<LocoUser> users = new ArrayList<LocoUser>();
                for(int i = 0; i < arr.length(); ++i) {

                    LocoUser u = new LocoUser();
                    JSONObject obj = arr.optJSONObject(i);
                    u.setMail(obj.optString("mail"));
                    u.setName(obj.optString("name"));
                    u.setIcon(obj.optString("icon"));
                    users.add(u);

                }

                DataAccessObject dao = new DataAccessObject(context);
                if(!dao.CreateForum(users, loc, name, forumId)) {
                    User myUser = ApiHandler.GetUser();
                    for (LocoUser u : users) {
                        if(u.getMail() != myUser.getMail()) {
                            ApiHandler.SendGCMMessage(u.getMail(), "{ 'type':'removeUser', 'mail': '" + myUser.getMail() + "', 'forumId':" + forumId + "}");
                        }
                    }
                }

            } else if(dataType.compareTo("removeUser") == 0) {

                long forumId = message.optLong("forumId");
                String mail = message.optString("mail");

                DataAccessObject dao = new DataAccessObject(context);
                dao.RemoveUserFromForum(mail, forumId);

            } else if(dataType.compareTo("forumMessage") == 0) {

                long forumId = message.optLong("forumId");
                JSONObject mo = message.optJSONObject("message");
                Message m = new Message();
                m.setContnet(mo.optString("context"));
                m.setTimestamp(new DateTime(mo.optLong("time")));
                m.setFrom(mo.optString("from"));

                DataAccessObject dao = new DataAccessObject(context);
                dao.WriteMessageToForum(forumId, m);

            }
        }

    }

}
