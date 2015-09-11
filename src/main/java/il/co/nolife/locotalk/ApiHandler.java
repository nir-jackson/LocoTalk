package il.co.nolife.locotalk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.Aroundmeapi;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserLocation;
import com.aroundme.EndpointApiCreator;
import com.aroundme.deviceinfoendpoint.Deviceinfoendpoint;
import com.aroundme.deviceinfoendpoint.model.DeviceInfo;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Victor Belski on 9/7/2015.
 * Handles all operation involving the AroundMeApi
 */
public class ApiHandler {

    static {
        instance = new ApiHandler();
    }

    public static final String SENDER_ID = "1047488186224";
    static ApiHandler instance;

    Aroundmeapi api;
    Context context;
    String regId;
    LocoTalkSharedPreferences prefs;
    User user;

    List<IApiCallback<Void>> delayedCalls;

    Boolean initialized = false;

    ApiHandler() {

        delayedCalls = new ArrayList<IApiCallback<Void>>();
        initialized = false;

        try {
            api = EndpointApiCreator.getApi(Aroundmeapi.class);
        } catch (Exception e) {
            Log.e(getClass().toString(), e.getMessage());
        }

    }

    public static void Initialize(Context context) {

        if(!instance.initialized) {

            instance.prefs = new LocoTalkSharedPreferences(context);
            instance.context = context;
            instance.user = instance.prefs.GetUser();
            instance.RegisterAsync(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    instance.initialized = true;
                    for (IApiCallback<Void> c : instance.delayedCalls) {
                        c.Invoke(null);
                    }
                }
            });

        }

    }

    public static void RetrieveMessage(final long messageId, final IApiCallback<Message> onEnd) {

        if (instance.initialized) {
            instance.RetrieveMessageAsync(messageId, onEnd);
        } else {

            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    RetrieveMessage(messageId, onEnd);
                }
            });

        }

    }

    public static void SendGCMMessage(final String mail, final String message) {

        if(instance.initialized) {
            instance.SendGCMMessageAsync(mail, message);
        } else {

            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    SendGCMMessage(mail, message);
                }
            });

        }

    }

    public static User GetUser() {
        return instance.prefs.GetUser();
    }

    public static void SetMyLocation(GeoPt loc) {

        instance.user.setLocation(new UserLocation().setPoint(loc));
        instance.prefs.StoreUser(instance.user);
        instance.ReportLocationAsync(loc, null);

    }

    public static void Login(final User user, final IApiCallback<Boolean> callback) {

        if(instance.initialized) {

            String pass = instance.prefs.GetPassword();
            if(pass.isEmpty()) {
                instance.Register(user, callback);
            }
            instance.LoginAsync(user, new IApiCallback<User>() {
                @Override
                public void Invoke(User result) {

                    if (result != null) {

                        instance.user = result;
                        instance.prefs.StoreUser(result);

                        if(callback != null) {
                            callback.Invoke(true);
                        }

                    } else {
                        instance.Register(user, callback);
                    }

                }
            });
        } else {
            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    Login(user, callback);
                }
            });
        }

    }

    void Register(User user, final IApiCallback<Boolean> callback) {

        SecureRandom random = new SecureRandom();
        final String pass = new BigInteger(130, random).toString(32);
        user.setPassword(pass);
        instance.RegisterUserAsync(user, new IApiCallback<User>() {
            @Override
            public void Invoke(User result) {
                if(result != null) {

                    instance.prefs.StorePassword(pass);
                    result.setPassword(pass);
                    instance.LoginAsync(result, new IApiCallback<User>() {
                        @Override
                        public void Invoke(User result) {

                            if(result != null) {

                                instance.user = result;
                                instance.prefs.StoreUser(result);

                                if(callback != null) {
                                    callback.Invoke(true);
                                }
                            } else {
                                if(callback != null) {
                                    callback.Invoke(false);
                                }
                            }

                        }
                    });

                } else {
                    if(callback != null) {
                        callback.Invoke(false);
                    }
                }
            }
        });

    }

    public static void SendMessageToUser(final Message message, final IApiCallback<Boolean> onEnd) {

        if(instance.initialized) {
            instance.SendStructuredMessageAsync(message, onEnd);
        } else {

            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    SendMessageToUser(message, onEnd);
                }
            });

        }

    }

    public static void GetUsersAroundMe(final int radius, final IApiCallback<List<UserAroundMe>> onEnd) {

        if(instance.initialized) {
            instance.GetUsersAroundMeAsync(radius, onEnd);
        } else {
            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    GetUsersAroundMe(radius, onEnd);
                }
            });
        }

    }

    public static void CreateForum(final LocoForum forum) {

        if(instance.initialized) {

            StringBuilder builder = new StringBuilder();

            builder.append("{ 'type' : 'newForum', 'forumId':");
            builder.append(forum.getId());
            builder.append(", 'owner':");
            builder.append(forum.getOwner());
            builder.append("', 'name':'");
            builder.append(forum.getName());
            builder.append("', 'lat':");
            builder.append(forum.getLocation().getLatitude());
            builder.append(", 'lon':");
            builder.append(forum.getLocation().getLongitude());
            builder.append(", 'participants': [");
            for (LocoUser u : forum.getUsers()) {
                builder.append("{ 'mail':'");
                builder.append(u.getMail());
                builder.append("', 'name':'");
                builder.append(u.getName());
                builder.append("', 'icon':'");
                builder.append(u.getIcon());
                builder.append("' }, ");
            }
            builder.append("] }");

            String json = builder.toString();

            for (LocoUser user : forum.getUsers()) {
                instance.SendGCMMessageAsync(user.getMail(), json);
            }

        } else {

            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    CreateForum(forum);
                }
            });

        }

    }

    public static void SendForumMessage(final LocoForum forum, final Message message) {

        if(instance.initialized) {

            StringBuilder builder = new StringBuilder();

            builder.append("{ 'type' : 'forumMessage', 'forumId':");
            builder.append(forum.getId());
            builder.append(", 'owner':'");
            builder.append(forum.getOwner());
            builder.append("', 'message': { 'from':'");
            builder.append(message.getFrom());
            builder.append("', 'time':");
            builder.append(new Date().getTime());
            builder.append(", 'content':'");
            builder.append(message.getContnet());
            builder.append("' } }");

            String json = builder.toString();

            for (LocoUser user : forum.getUsers()) {
                instance.SendGCMMessageAsync(user.getMail(), json);
            }

        } else {

            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    SendForumMessage(forum, message);
                }
            });

        }

    }

    public static void SendEventMessage(final LocoEvent event, final Message message, final int radius) {

        if(instance.initialized) {

            StringBuilder builder = new StringBuilder();

            builder.append("{ 'type' : 'eventMessage', 'eventId':");
            builder.append(event.getId());
            builder.append(", 'owner':'");
            builder.append(event.getOwner());
            builder.append("', 'lat':");
            builder.append(event.getLocation().getLatitude());
            builder.append(", 'lon':");
            builder.append(event.getLocation().getLongitude());
            builder.append(", 'name':'");
            builder.append(event.getName());
            builder.append("', 'message': { 'from':'");
            builder.append(message.getFrom());
            builder.append("', 'time':");
            builder.append(new Date().getTime());
            builder.append(", 'content':'");
            builder.append(message.getContnet());
            builder.append("' } }");

            final String json = builder.toString();

            instance.GetUsersAroundPoint(radius, event.getLocation().getLatitude(), event.getLocation().getLongitude(), new IApiCallback<List<UserAroundMe>>() {
                @Override
                public void Invoke(List<UserAroundMe> result) {
                    if(result != null) {
                        for (UserAroundMe user : result) {
                            instance.SendGCMMessageAsync(user.getMail(), json);
                        }
                    }
                }
            });

        } else {

            instance.delayedCalls.add(new IApiCallback<Void>() {
                @Override
                public void Invoke(Void result) {
                    SendEventMessage(event, message, radius);
                }
            });

        }

    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    void RegisterAsync(final IApiCallback<Void> onEnd) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {

                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    Log.i(getClass().toString(), gcm.toString());
                    regId = gcm.register(SENDER_ID);
                    Log.i(getClass().toString(), "Device registered, registration ID=" + regId);

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    SendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    prefs.StoreRegistrationId(regId);

                } catch (IOException ex) {

                    Log.e(getClass().toString(), ex.getMessage());
                    return false;
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.

                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {

                if(onEnd != null) {
                    onEnd.Invoke(null);
                }

            }

        }.execute();

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
     * since the device sends upstream messages to a server that echoes back the
     * message using the 'from' address in the message.
     */
    void SendRegistrationIdToBackend() {
        try {
            com.aroundme.deviceinfoendpoint.Deviceinfoendpoint endpoint = EndpointApiCreator
                    .getApi(Deviceinfoendpoint.class);
            DeviceInfo existingInfo = endpoint.getDeviceInfo(regId).execute();

            boolean alreadyRegisteredWithEndpointServer = false;
            if (existingInfo != null
                    && regId.equals(existingInfo.getDeviceRegistrationID())) {
                alreadyRegisteredWithEndpointServer = true;
            }

            if (!alreadyRegisteredWithEndpointServer) {
				/*
				 * We are not registered as yet. Send an endpoint message
				 * containing the GCM registration id and some of the device's
				 * product information over to the backend. Then, we'll be
				 * registered.
				 */
                DeviceInfo deviceInfo = new DeviceInfo();
                endpoint.insertDeviceInfo(
                        deviceInfo.setDeviceRegistrationID(regId)
                                .setTimestamp(System.currentTimeMillis())
                                .setDeviceInformation(URLEncoder.encode(android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT, "UTF-8"))).execute();
            }
        } catch (Exception e) {
            Log.e(getClass().toString(), e.getMessage());
        }

    }

    void RegisterUserAsync(final User user, final IApiCallback<User> onEnd) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    User u = api.register(user).execute();
                    if(onEnd != null) {
                        onEnd.Invoke(u);
                    }
                } catch (IOException e) {
                    Log.e(getClass().toString(), e.getMessage());
                    if(onEnd != null){
                        onEnd.Invoke(null);
                    }
                }

                return false;

            }

        }.execute();

    }

    void LoginAsync(final User user, final IApiCallback<User> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    User u = api.login(user.getMail(), user.getPassword(), user.getRegistrationId()).execute();
                    if(onEnd != null) {
                        onEnd.Invoke(u);
                    }
                }catch (Exception e) {
                    Log.e(getClass().toString(), e.getMessage());
                    if(onEnd != null) {
                        onEnd.Invoke(null);
                    }
                }
                return null;

            }

        }.execute();

    }

    void SendMessageAsync(final String message, final IApiCallback<Boolean> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Message newMessage = new Message();
                newMessage.setContnet(message);
                try {
                    api.sendMessage(newMessage).execute();
                    if(onEnd != null) {
                        onEnd.Invoke(true);
                    }
                } catch(IOException e) {
                    Log.e(getClass().toString(), e.getMessage());
                    if(onEnd != null) {
                        onEnd.Invoke(false);
                    }
                }

                return null;

            }

        }.execute();

    }

    void ReportLocationAsync(final GeoPt location, final IApiCallback<Boolean> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    api.reportUserLocation(user.getMail(), location).execute();
                    if(onEnd != null) {
                        onEnd.Invoke(true);
                    }
                } catch(IOException e) {
                    Log.e(getClass().toString(), "Error reporting user location: " + e.getMessage());
                    if(onEnd != null) {
                        onEnd.Invoke(false);
                    }
                }

                return null;

            }

        }.execute();

    }

    void SendStructuredMessageAsync(final Message message, final IApiCallback<Boolean> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    api.sendMessage(message).execute();
                    if(onEnd != null) {
                        onEnd.Invoke(true);
                    }
                } catch(IOException e) {
                    Log.e(getClass().toString(), e.getMessage());
                    if(onEnd != null) {
                        onEnd.Invoke(false);
                    }
                }

                return null;
            }
        }.execute();

    }

    void GetUsersAroundMeAsync(final int radius, final IApiCallback<List<UserAroundMe>> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    GeoPt loc = user.getLocation().getPoint();
                    List<UserAroundMe> retVal = api.getUsersAroundMe(loc.getLongitude(), loc.getLatitude(), radius, user.getMail()).execute().getItems();
                    if(onEnd != null) {
                        onEnd.Invoke(retVal);
                    }
                } catch(IOException e) {
                    Log.e(getClass().toString(), "Failed to get users around me .... fucking shit: " + e.getMessage());
                    if(onEnd != null) {
                        onEnd.Invoke(null);
                    }
                }
                return null;

            }
        }.execute();

    }

    void GetUsersAroundPoint(final int radius, final float lat, final float lon, final IApiCallback<List<UserAroundMe>> callback) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {

                    List<UserAroundMe> retVal = api.getUsersAroundMe(lon, lat, radius, user.getMail()).execute().getItems();
                    if(callback!= null) {
                        callback.Invoke(retVal);
                    }

                } catch(IOException e) {
                    Log.e(getClass().toString(), "Failed to get users around me .... fucking shit: " + e.getMessage());
                    if(callback != null) {
                        callback.Invoke(null);
                    }
                }
                return null;

            }
        }.execute();

    }

    void SendForumMessage(final LocoForum forum, final String message, final IApiCallback<Boolean> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                String sentMessage = "{ 'type': 'forumMessage', "
                        + "'message': "
                            + " 'from': '" + user.getMail() + "'"
                            + " 'time': " + new DateTime(new Date()).getValue()
                            + " 'content': '" + message + "' }";

                try {
                    for (LocoUser u : forum.getUsers()){
                        api.sendGcmMessage(u.getMail(), sentMessage);
                    }
                    if(onEnd != null) {
                        onEnd.Invoke(true);
                    }
                } catch (IOException e) {
                    Log.e(getClass().toString(), "Could not send forum message");
                    if(onEnd != null) {
                        onEnd.Invoke(false);
                    }
                }

                return null;
            }
        }.execute();

    }

    void SendCreateForum(final LocoForum forum, final IApiCallback<Boolean> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {



                return null;
            }
        }.execute();

    }

    void RetrieveMessageAsync(final long messageId, final IApiCallback<Message> onEnd) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Message m = api.getMessage(messageId).execute();
                    if(onEnd != null) {
                        onEnd.Invoke(m);
                    }
                } catch (IOException e) {
                    Log.e(getClass().toString(), e.getMessage());
                    if(onEnd != null) {
                        onEnd.Invoke(null);
                    }
                }
                return null;
            }

        }.execute();

    }

    void SendGCMMessageAsync(final String mail, final String message) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    api.sendGcmMessage(mail, message).execute();
                } catch (IOException e) {
                    Log.e(getClass().toString(), e.getMessage());
                }
                return null;
            }
        }.execute();

    }

}
