package il.co.nolife.locotalk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Victor Belski on 9/11/2015.
 */
public class AppController {

    public static final String TAG = "AppController";

    static {
        instance = new AppController();
    }

    static AppController instance;

    private AppController() {

        newMessageListeners = new ArrayList<IApiCallback<Message>>();
        newForumListeners = new ArrayList<IApiCallback<Long>>();
        newForumMsgListeners = new ArrayList<IApiCallback<Long>>();
        newEventListeners = new ArrayList<IApiCallback<Long>>();
        newEventMsgListeners = new ArrayList<IApiCallback<Long>>();
        newFriendListeners = new ArrayList<IApiCallback<String>>();
        userPongedListener = new ArrayList<IApiCallback<String>>();
        cachedImages = new HashMap<String, Bitmap>();
        downloading = false;
        pendingDownloads = new ArrayList<IApiCallback<Void>>();
        userCache = new HashMap<String, User>();
        myUser = new LocoUser();
        friends = new HashMap<>();

    }

    List<IApiCallback<Message>> newMessageListeners;
    List<IApiCallback<Long>> newForumListeners;
    List<IApiCallback<Long>> newForumMsgListeners;
    List<IApiCallback<Long>> newEventListeners;
    List<IApiCallback<Long>> newEventMsgListeners;
    List<IApiCallback<String>> newFriendListeners;
    List<IApiCallback<String>> userPongedListener;
    HashMap<String, Bitmap> cachedImages;
    Boolean downloading;
    List<IApiCallback<Void>> pendingDownloads;
    HashMap<String, User> userCache;
    HashMap<String, LocoUser> friends;

    LocoUser myUser;

    public static void NewPrivateMessage(Message message) {
        for (IApiCallback<Message> c : instance.newMessageListeners) {
            c.Invoke(message);
        }
    }

    public static void NewForum(long forumId) {
        for (IApiCallback<Long> c : instance.newForumListeners) {
            c.Invoke(forumId);
        }
    }

    public static void NewForumMessage(long forumId) {
        for (IApiCallback<Long> c : instance.newForumMsgListeners) {
            c.Invoke(forumId);
        }
    }

    public static void NewEvent(long eventId) {
        for (IApiCallback<Long> c : instance.newEventListeners) {
            c.Invoke(eventId);
        }
    }

    public static void NewEventMessage(long eventId) {
        for (IApiCallback<Long> c : instance.newEventMsgListeners) {
            c.Invoke(eventId);
        }
    }

    public static void NewFriend(String user) {
        for (IApiCallback<String> c : instance.newFriendListeners) {
            c.Invoke(user);
        }
    }

    public static void UserPonged(String user) {
        for(IApiCallback<String> c : instance.userPongedListener) {
            c.Invoke(user);
        }
    }

    public static void AddPrivateMessageListener(IApiCallback<Message> listener) {
        instance.newMessageListeners.add(listener);
    }

    public static void AddNewForumListener(IApiCallback<Long> listener) {
        instance.newForumListeners.add(listener);
    }

    public static void AddNewForumMessageListener(IApiCallback<Long> listener) {
        instance.newForumMsgListeners.add(listener);
    }

    public static void AddNewFriendListener(IApiCallback<String> listener) {
        instance.newFriendListeners.add(listener);
    }

    public static void AddUserPongedListener(IApiCallback<String> listener) {
        instance.userPongedListener.add(listener);
    }

    public static void AddNewEventListener(IApiCallback<Long> listener) {
        instance.newEventListeners.add(listener);
    }

    public static void AddNewEventMessageListener(IApiCallback<Long> listener) {
        instance.newEventMsgListeners.add(listener);
    }

    public static void RemovePrivateMessageListener(IApiCallback<Message> listener) {
        instance.newMessageListeners.remove(listener);
    }

    public static void RemoveNewForumListener(IApiCallback<Long> listener) {
        instance.newForumListeners.remove(listener);
    }

    public static void RemoveNewForumMessageListener(IApiCallback<Long> listener) {
        instance.newForumMsgListeners.remove(listener);
    }

    public static void RemoveNewEventListener(IApiCallback<Long> listener) {
        instance.newEventListeners.remove(listener);
    }

    public static void RemoveNewEventMessageListener(IApiCallback<Long> listener) {
        instance.newEventMsgListeners.remove(listener);
    }

    public static void RemoveNewFriendListener(IApiCallback<String> listener) {
        instance.newFriendListeners.remove(listener);
    }

    public static void RemoveUserPongedListener(IApiCallback<String> listener) {
        instance.userPongedListener.remove(listener);
    }

    public static void GetImage(final String url, final IApiCallback<Bitmap> callback) {

        if(instance.cachedImages.containsKey(url)) {
            if(callback != null) {
                callback.Invoke(instance.cachedImages.get(url));
            }
        } else {

            if(!instance.downloading) {

                instance.downloading = true;

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        try {

                            URL urlStream = new URL(url);
                            Bitmap image = BitmapFactory.decodeStream(urlStream.openStream());
                            instance.cachedImages.put(url, image);
                            if(callback != null) {
                                callback.Invoke(image);
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Could not download image on url:" + url + ", Error:" + e.getMessage());
                        }

                        instance.downloading = false;

                        if(instance.pendingDownloads.size() > 0) {

                            IApiCallback<Void> nextDownload = instance.pendingDownloads.get(0);
                            instance.pendingDownloads.remove(0);
                            nextDownload.Invoke(null);

                        }

                        return null;
                    }

                }.execute();

            } else {
                instance.pendingDownloads.add(new IApiCallback<Void>() {
                    @Override
                    public void Invoke(Void result) {
                        GetImage(url, callback);
                    }
                });
            }

        }

    }

    public static void AddUserToCache(User user) {

        if(!instance.userCache.containsKey(user.getMail())) {

            instance.userCache.put(user.getMail(), user);
            GetImage(user.getImageUrl(), null);

        }

    }

    public static void AddUsersToCache(Collection<User> users) {

        for (User u : users) {
            if(!instance.userCache.containsKey(u.getMail())) {
                instance.userCache.put(u.getMail(), u);
                GetImage(u.getImageUrl(), null);
            }
        }

    }

    public static User GetUserFromCache(String mail) {
        User retVal = instance.userCache.get(mail);
        if(retVal != null) {
            return retVal;
        } else {
            return new User();
        }
    }

    public static LocoUser GetMyUser() {
        return instance.myUser;
    }

    public static void SetMyUser(LocoUser user) {
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

    public static Boolean CheckIfFriend(String mail) {
        return instance.friends.containsKey(mail);
    }

    public static Boolean CheckIfSafeFriend(String mail) {

        LocoUser friend = instance.friends.get(mail);
        if(friend != null) {
            return friend.getSafe();
        } else {
            return false;
        }
    }

    public static void SetFriends(Collection<LocoUser> friends) {

        instance.friends = new HashMap<>();
        for (LocoUser f : friends) {
            instance.friends.put(f.getMail(), f);
        }

    }

    public static void UpdateFriendsLocation(Collection<LocoUser> users, DataAccessObject dao) {

        Boolean updated = false;
        for (LocoUser u : users) {

            LocoUser friend = instance.friends.get(u.getMail());
            if(friend != null) {
                friend.setLocation(u.getLocation());
                updated = true;
            }

        }

        if(updated) {
            dao.UpdateFriendsLocation(instance.friends.values());
        }

    }

}
