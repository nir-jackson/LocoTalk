package il.co.nolife.locotalk;

import android.content.Context;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.google.api.client.util.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import il.co.nolife.locotalk.DataTypes.LocoUser;

/**
 * Created by Victor Belski on 9/10/2015.
 * Designed to handle all JSON based communication between LocoTal users
 */
public class GcmMessageHandler {

    public static final String TAG = "GcmMessageHandler";

    public static void HandleMessage(Context context, JSONObject message) {

        ApiHandler.Initialize(context);
        String dataType = message.optString("type");
        if(dataType != null) {

            try {
                if (dataType.compareTo("ping") == 0) {

                    String mail = message.getString("mail");
                    Log.i(TAG, "Dude pinged !:" + mail);
                    User myUser = ApiHandler.GetUser();
                    if (myUser != null) {
                        String m = "{ 'type':'pong', 'mail':'" + myUser.getMail() + "' }";
                        ApiHandler.SendGCMMessage(mail, m);
                        Log.i(TAG, m);
                    }

                } else if (dataType.compareTo("pong") == 0) {

                    String mail = message.getString("mail");
                    Log.i(TAG, "Dude ponged !:" + mail);
                    DataAccessObject dao = new DataAccessObject(context);
                    dao.ValidateFriend(mail);
                    AppController.UserPonged(mail);

                } else if (dataType.compareTo("newForum") == 0) {

                    long forumId = message.getLong("forumId");
                    String owner = message.getString("owner");
                    String name = message.getString("name");
                    GeoPt location = new GeoPt();
                    float lat = (float)message.getDouble("lat");
                    float lon = (float)message.getDouble("lon");
                    location.setLatitude(lat);
                    location.setLongitude(lon);
                    JSONArray arr = message.optJSONArray("participants");
                    List<LocoUser> users = new ArrayList<LocoUser>();
                    for (int i = 0; i < arr.length(); ++i) {

                        LocoUser user = new LocoUser();
                        JSONObject obj = arr.getJSONObject(i);
                        user.setMail(obj.getString("mail"));
                        user.setName(obj.getString("name"));
                        user.setIcon(obj.getString("icon"));
                        users.add(user);

                    }

                    DataAccessObject dao = new DataAccessObject(context);
                    dao.CreateUnownedForum(users, location, name, owner, forumId);

                } else if (dataType.compareTo("forumMessage") == 0) {

                    long forumId = message.getLong("forumId");
                    String owner = message.getString("owner");
                    JSONObject mo = message.getJSONObject("message");
                    Message actualMessage = new Message();
                    actualMessage.setContnet(mo.getString("context"));
                    actualMessage.setTimestamp(new DateTime(mo.getLong("time")));
                    actualMessage.setFrom(mo.getString("from"));

                    DataAccessObject dao = new DataAccessObject(context);
                    dao.WriteMessageToForum(forumId, owner, actualMessage);

                } else if (dataType.compareTo("eventMessage") == 0) {

                    long eventId = message.getLong("eventId");
                    String owner = message.getString("owner");
                    int radius = message.getInt("radius");
                    GeoPt loc = new GeoPt();
                    float lat = (float)message.getDouble("lat");
                    float lon = (float)message.getDouble("lon");
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    String name = message.getString("name");
                    JSONObject mo = message.getJSONObject("message");
                    Message actualMessage = new Message();
                    actualMessage.setContnet(mo.getString("context"));
                    actualMessage.setTimestamp(new DateTime(mo.getLong("time")));
                    actualMessage.setFrom(mo.getString("from"));

                    DataAccessObject dao = new DataAccessObject(context);
                    dao.WriteMessageToEvent(eventId, name, owner, loc, radius, actualMessage);

                }
            } catch(JSONException e) {
                Log.e(TAG, "Could not parse GCM Message of type '" + dataType + "': " + e.getMessage());
            }
        }

    }

}
