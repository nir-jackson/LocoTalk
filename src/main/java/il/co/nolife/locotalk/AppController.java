package il.co.nolife.locotalk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import il.co.nolife.locotalk.DataTypes.LocoUser;

/**
 * Created by Victor Belski on 9/11/2015.
 */
public class AppController {

    public static final String TAG = "AppController";

    class RangeHelper {

        public static final double PI = 3.1415926535897932385;
        public static final double EARTH_RADIUs = 6371000;

        double radius;
        GeoPt point;

        public RangeHelper(GeoPt loc, double radius) {
            this.radius = radius;
            point = loc;
        }

        boolean checkIfInRange(GeoPt point) {
            return DistFrom(point) <= radius;
        }

        double DistFrom(GeoPt pt2) {

            double earthRadius = 6371000; //meters
            double dLat = Math.toRadians(pt2.getLatitude() - point.getLatitude());
            double dLng = Math.toRadians(pt2.getLongitude() - point.getLongitude());
            double a = (Math.sin(dLat/2) * Math.sin(dLat/2)) +
                    Math.cos(Math.toRadians(point.getLatitude())) * Math.cos(Math.toRadians(pt2.getLatitude())) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double dist = (earthRadius * c);

            return dist;

        }

    }

    static {

        instance = new AppController();

        AddUserPongedListener(new Callback<String>() {
            @Override
            public void Invoke(String result) {

                LocoUser user = instance.friends.get(result);
                if(user != null) {
                    user.setSafe(true);
                }

            }
        });

    }

    static AppController instance;

    private AppController() {

        newMessageListeners = new ArrayList<Callback<Message>>();
        newForumMsgListeners = new ArrayList<Callback<Long>>();
        newEventMsgListeners = new ArrayList<Callback<Long>>();
        userPongedListener = new ArrayList<Callback<String>>();
        cachedImages = new HashMap<String, Bitmap>();
        downloading = false;
        pendingDownloads = new ArrayList<Callback<Void>>();
        allUsers = new HashMap<String, LocoUser>();
        myUser = new LocoUser();
        friends = new HashMap<>();

    }

    List<Callback<Message>> newMessageListeners;
    List<Callback<Long>> newForumMsgListeners;
    List<Callback<Long>> newEventMsgListeners;
    List<Callback<String>> userPongedListener;

    HashMap<String, Bitmap> cachedImages;
    boolean downloading;
    List<Callback<Void>> pendingDownloads;

    HashMap<String, LocoUser> allUsers;
    HashMap<String, LocoUser> friends;

    LocoUser myUser;

    Activity mainActivity;

    RangeHelper GetRangeHelper(GeoPt loc, int range) {
        return new RangeHelper(loc, range);
    }

    public static void NewPrivateMessage(Message message) {
        for (Callback<Message> c : instance.newMessageListeners) {
            c.Invoke(message);
        }
    }

    public static void NewForumMessage(long forumId) {
        for (Callback<Long> c : instance.newForumMsgListeners) {
            c.Invoke(forumId);
        }
    }

    public static void NewEventMessage(long eventId) {
        for (Callback<Long> c : instance.newEventMsgListeners) {
            c.Invoke(eventId);
        }
    }

    public static void UserPonged(String user) {
        for(Callback<String> c : instance.userPongedListener) {
            c.Invoke(user);
        }
    }

    public static void AddPrivateMessageListener(Callback<Message> listener) {
        if(!instance.newMessageListeners.contains(listener)) {
            instance.newMessageListeners.add(listener);
        }
    }

    public static void AddNewForumMessageListener(Callback<Long> listener) {
        if(!instance.newForumMsgListeners.contains(listener)) {
            instance.newForumMsgListeners.add(listener);
        }
    }

    public static void AddUserPongedListener(Callback<String> listener) {
        if(!instance.userPongedListener.contains(listener)) {
            instance.userPongedListener.add(listener);
        }
    }

    public static void AddNewEventMessageListener(Callback<Long> listener) {
        if(!instance.newEventMsgListeners.contains(listener)) {
            instance.newEventMsgListeners.add(listener);
        }
    }

    public static void RemovePrivateMessageListener(Callback<Message> listener) {
        instance.newMessageListeners.remove(listener);
    }

    public static void RemoveNewForumMessageListener(Callback<Long> listener) {
        instance.newForumMsgListeners.remove(listener);
    }

    public static void RemoveNewEventMessageListener(Callback<Long> listener) {
        instance.newEventMsgListeners.remove(listener);
    }

    public static void RemoveUserPongedListener(Callback<String> listener) {
        instance.userPongedListener.remove(listener);
    }

    public static void GetImage(final String url, final Callback<Bitmap> callback) {

        Log.i(TAG, "Trying to get user image: " + url);

        if(instance.cachedImages.containsKey(url)) {
            if(callback != null) {
                Log.i(TAG, "imediatly found image: " + url);
                callback.Invoke(instance.cachedImages.get(url));
            }
        } else {

            if(!instance.downloading) {

                instance.downloading = true;
                Log.i(TAG, "Trying to download image: " + url);

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        try {

                            URL urlStream = new URL(url);
                            Bitmap image = BitmapFactory.decodeStream(urlStream.openStream());
                            instance.cachedImages.put(url, image);
                            Log.i(TAG, "Image downloaded: " + url);
                            if(callback != null) {
                                callback.Invoke(image);
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Could not download image on url:" + url + ", Error:" + e.getMessage());
                        }

                        instance.downloading = false;

                        if(instance.pendingDownloads.size() > 0) {

                            Log.i(TAG, "Calling the next download: " + url);
                            Callback<Void> nextDownload = instance.pendingDownloads.get(0);
                            instance.pendingDownloads.remove(0);
                            nextDownload.Invoke(null);

                        }

                        return null;

                    }

                }.execute();

            } else {
                instance.pendingDownloads.add(new Callback<Void>() {
                    @Override
                    public void Invoke(Void result) {
                        GetImage(url, callback);
                    }
                });
            }

        }

    }

    public static LocoUser GetUser(String mail) {
        LocoUser retVal = instance.allUsers.get(mail);
        if(retVal != null) {
            return retVal;
        } else {
            return new LocoUser();
        }
    }

    public static void SetUsers(List<LocoUser> users) {

        instance.allUsers = new HashMap<>();
        instance.friends = new HashMap<>();
        for (LocoUser u : users) {
            instance.allUsers.put(u.getMail(), u);
            if(u.getFriend()) {
                instance.friends.put(u.getMail(), u);
            }
        }

    }

    public static LocoUser GetMyUser() {
        return instance.myUser;
    }

    public static synchronized void SetMyUser(LocoUser user) {
        instance.myUser = user;
    }

    public static HashMap<String, LocoUser> GetFriends() {
        return instance.friends;
    }

    public static List<LocoUser> GetSafeFriends() {

        List<LocoUser> safeFriends = new ArrayList<>();

        for (LocoUser friend : instance.friends.values()) {
            if(friend.getSafe()) {
                safeFriends.add(friend);
            }
        }

        return safeFriends;

    }

    public static boolean CheckIfFriend(String mail) {
        return instance.friends.containsKey(mail);
    }

    public static boolean CheckIfSafeFriend(String mail) {

        LocoUser friend = instance.friends.get(mail);
        if(friend != null) {
            return friend.getSafe();
        } else {
            return false;
        }

    }

    public static boolean CheckKnownUser(String mail) {
        return instance.allUsers.containsKey(mail);
    }

    public static boolean CheckIfSafe(String mail) {

        LocoUser user = instance.allUsers.get(mail);
        if(user != null) {
            return user.getSafe();
        } else {
            return false;
        }

    }

    public static synchronized List<LocoUser> GetKnownUsersAround(GeoPt location, int range) {

        List<LocoUser> retVal = new ArrayList<>();
        RangeHelper rh = instance.GetRangeHelper(location, range);

        for (LocoUser user : instance.allUsers.values()) {
            if(rh.checkIfInRange(user.getLocation())) {
                retVal.add(user);
            }
        }

        return retVal;

    }

    public static synchronized void UpdateUsersLocation(Collection<LocoUser> users, DataAccessObject dao) {

        dao.UpdateUsersLocation(users);
        for (LocoUser user : users) {

            LocoUser u = instance.allUsers.get(user.getMail());
            if(u != null) {
                u.setLocation(user.getLocation());
            }

        }

    }

    public static void SetMainActivity(Activity activity) {
        instance.mainActivity = activity;
    }

    public static Activity GetMainActivity() {
        return instance.mainActivity;
    }

}
