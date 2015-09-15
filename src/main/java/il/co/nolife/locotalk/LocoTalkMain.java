package il.co.nolife.locotalk;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.aroundme.EndpointApiCreator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import il.co.nolife.locotalk.DataTypes.EChatType;
import il.co.nolife.locotalk.DataTypes.LocoEvent;
import il.co.nolife.locotalk.DataTypes.LocoForum;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.ViewClasses.SimpleDialog;

// import com.google.android.gms.location.LocationListener;


public class LocoTalkMain extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, SimpleDialog.DialogClickListener {

    private static final String TAG = "LocoTalkMain";
    private static final int RC_SIGN_IN = 0;
    public static final int MAX_RANGE = 100000;
    private boolean mIntentInProgress;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;

    HashMap<Marker, LocoUser> currentUserMap;
    HashMap<String, Marker> reverseMarkersMap;
    HashMap<Marker, LocoUser> friendsMarkersMap;
    HashMap<String, Marker> reverseFriendsMap;
    HashMap<Marker, LocoForum> forumMarkerMap;
    HashMap<Marker, LocoEvent> eventMarkerMap;

    Marker myMarker;
    Circle myRangeCircle;
    int myRange = 20000;
    int myCircleColor;

    Marker selectedMarker;
    Circle eventRadius;
    CameraPosition cameraPosition;
    int eventCircleColor;

    BitmapDescriptor personMarkerIcon;
    BitmapDescriptor safePersonMarkerIcon;
    BitmapDescriptor friendMarkerIcon;
    BitmapDescriptor safeFriendMarkerIcon;
    BitmapDescriptor myMarkerIcon;
    BitmapDescriptor forumMarkerIcon;
    BitmapDescriptor eventMarkerIcon;

    Thread workerThread;
    Boolean pause = false;
    Boolean skipSleep = false;
    Boolean positionRetrieved = false;
    Boolean waitingForLocationServices = false;
    Boolean inUserDrawingPhase = false;
    int sleepTime;

    DataAccessObject dao;

    IApiCallback<String> userPongedListener;
    IApiCallback<Long> newForumListener;
    IApiCallback<Long> newEventListener;

    List<IApiCallback<Void>> waitingForMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "it has to work!");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pause = false;
        EndpointApiCreator.initialize(null);
        setContentView(R.layout.map_activity);
        ApiHandler.Initialize(this);
        dao = new DataAccessObject(this);
        myCircleColor = Color.parseColor("#5555ff");
        eventCircleColor = Color.parseColor("#9c39f1");

        final TextView rangeText = (TextView) findViewById(R.id.main_picker_text);
        rangeText.setText(Integer.toString(myRange / 1000) + " Km");

        SeekBar rangePicker = (SeekBar) findViewById(R.id.main_range_picker);
        rangePicker.setMax(MAX_RANGE);
        rangePicker.setProgress(myRange);
        rangePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    myRange = progress;
                    if(progress < 1000) {
                        rangeText.setText(Integer.toString(progress) + "m");
                    } else {
                        rangeText.setText(Integer.toString(progress / 1000) + "Km");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (workerThread != null) {
                    workerThread.interrupt();
                    skipSleep = true;
                }
            }
        });

        eventMarkerMap = new HashMap<>();
        forumMarkerMap = new HashMap<>();
        currentUserMap = new HashMap<>();
        reverseMarkersMap = new HashMap<>();
        friendsMarkersMap = new HashMap<>();
        reverseFriendsMap = new HashMap<>();

        personMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person));
        safePersonMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person_blue));
        friendMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person_yellow));
        safeFriendMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person_green));
        myMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person_red));
        forumMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.forum));
        eventMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.event));

        waitingForMap = new ArrayList<>();

        userPongedListener = new IApiCallback<String>() {
            @Override
            public void Invoke(String result) {

                AppController.SetUsers(dao.GetAllUsers());

                if(workerThread != null) {
                    if(inUserDrawingPhase) {
                        skipSleep = true;
                    }
                    workerThread.interrupt();
                }

                Log.i(TAG, "User ponged");

            }
        };

        buildGoogleApiClient();

    }

    protected void onStart() {
        super.onStart();

        AppController.SetUsers(dao.GetAllUsers());

        AppController.AddUserPongedListener(userPongedListener);
        mGoogleApiClient.connect();
        LocationServiceEnabled();

        if(mMap != null) {

            RefreshEventMarkers();
            RefreshForumMarkers();
            RefreshFriendsMarkers();

        }

    }

    protected void onStop() {
        super.onStop();

        AppController.RemoveUserPongedListener(userPongedListener);
        AppController.RemoveForumsChangedListener(newForumListener);
        AppController.RemoveEventsChangedListener(newEventListener);

        pause = true;
        workerThread.interrupt();
        workerThread = null;

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    protected synchronized void buildGoogleApiClient(){

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (!mIntentInProgress && connectionResult.hasResolution()) {
                            try {
                                mIntentInProgress = true;
                                startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                                        RC_SIGN_IN, null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {
                                // The intent was canceled before it was sent.  Return to the default
                                // state and attempt to connect to get an updated ConnectionResult.
                                mIntentInProgress = false;
                                mGoogleApiClient.connect();
                            }
                        }

                    }
                })
                .addApi(LocationServices.API)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();

            }
        }

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.

        if (mMap == null) {

            Log.i(TAG, "map was Null");
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        MarkerClicked(marker);
                    }
                });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        LongMapClicked(latLng);
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //MarkerClicked(marker);
                        LocoEvent event = eventMarkerMap.get(marker);
                        if (event != null) {
                            selectedMarker = marker;

                            eventRadius = mMap.addCircle(new CircleOptions()
                                    .center(new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude()))
                                    .radius(20000)
                                    .strokeWidth(3)
                                    .strokeColor(eventCircleColor));

                            Log.i("ClickCheck", "Selecting " + selectedMarker.getTitle());
                        }
                        return false;
                    }
                });

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (selectedMarker != null) {
                            Log.i("ClickCheck", "Deselecting " + selectedMarker.getTitle());
                            selectedMarker = null;
                            if (eventRadius != null) {
                                eventRadius.remove();
                                eventRadius = null;
                            }
                        }
                    }
                });

                onMapReady();

            }

        }

    }

    void LongMapClicked (LatLng latLng){
        EventOrForumActivity.Reset();
        Intent chooseIntent = new Intent(this, EventOrForumActivity.class);
        chooseIntent.putExtra("lon",latLng.longitude);
        chooseIntent.putExtra("lat",latLng.latitude);
        startActivity(chooseIntent);
    }

    void MarkerClicked(Marker marker){

        LocoUser user = currentUserMap.get(marker);
        if(user != null) {

            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("type", EChatType.PRIVATE.ordinal());
            chatIntent.putExtra("from", user.getMail());
            startActivity(chatIntent);

        } else {

            user = friendsMarkersMap.get(marker);

            String s = "";
            for (Marker m: friendsMarkersMap.keySet()) {
                s += m.getTitle() + ":" + m + ", ";
            }

            Log.i("CHECK", s + ". Clicked marker: " + marker);

            if(user != null) {

                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra("type", EChatType.PRIVATE.ordinal());
                chatIntent.putExtra("from", user.getMail());
                startActivity(chatIntent);

            } else {

                LocoEvent event = eventMarkerMap.get(marker);
                if (event != null) {

                    Intent chatIntent = new Intent(this, ChatActivity.class);
                    chatIntent.putExtra("type", EChatType.EVENT.ordinal());
                    chatIntent.putExtra("eventId", event.getId());
                    startActivity(chatIntent);

                } else {

                    LocoForum forum = forumMarkerMap.get(marker);
                    if (forum != null) {

                        Intent chatIntent = new Intent(this, ChatActivity.class);
                        chatIntent.putExtra("type", EChatType.FORUM.ordinal());
                        chatIntent.putExtra("forumId", forum.getId());
                        startActivity(chatIntent);

                    } else {
                        Log.e(TAG, "Could not find marker in the current maps");
                    }

                }

            }

        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i("This is bull shit", Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).toString());

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {

            Log.i(getClass().toString(),"On Connect2");
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String personPhoto = currentPerson.getImage().getUrl();
            String personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);

            LocoUser myUser = new LocoUser();

            myUser.setName(personName);
            if (personEmail.compareTo("nir.jackson89@gmail.com")==0){
                personEmail="nir.jackson890@gmail.com";
            }else if (personEmail.compareTo("iloriginal@gmail.com")==0){
                personEmail="iloriginal0@gmail.com";
            }

            myUser.setMail(personEmail);
            myUser.setIcon(personPhoto);
            AppController.SetMyUser(myUser);
            // Log.i(TAG, myUser.toString());

            ApiHandler.GetRegistrationId(new IApiCallback<String>() {
                @Override
                public void Invoke(String result) {
                    AppController.GetMyUser().setRegId(result);
                    Log.i(TAG, AppController.GetMyUser().toString());
                    ApiHandler.Login(AppController.GetMyUser().toUser(), new IApiCallback<Boolean>() {
                        @Override
                        public void Invoke(Boolean result) {
                            if(result) {
                                Log.i(TAG, "Successfully logged in to AroundMe backend");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.main_splash).setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                Log.e(TAG, "Failed to log in to the AroundMe backend");
                            }
                        }
                    });
                }
            });

            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation == null) {

                if(!waitingForLocationServices) {

                    Bundle dialogBundle = new Bundle();
                    dialogBundle.putString("title", "Location");
                    dialogBundle.putString("positive", "Go");
                    dialogBundle.putString("negative", "Later");
                    dialogBundle.putString("content", "Your location services are turned off, this application cannot operate without access to your current location.\nPlease turn on location services");
                    SimpleDialog d = new SimpleDialog();
                    d.setArguments(dialogBundle);
                    d.show(getFragmentManager(), "SimpleDialog");

                }

            }

        }

        setUpMapIfNeeded();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onMapReady() {

        for (IApiCallback<Void> c : waitingForMap) {
            c.Invoke(null);
        }

        newForumListener = new IApiCallback<Long>() {
            @Override
            public void Invoke(final Long result) {
                RefreshForumMarkers();
            }
        };

        newEventListener = new IApiCallback<Long>() {
            @Override
            public void Invoke(Long result) {
                RefreshEventMarkers();
            }
        };

        AppController.AddForumsChangedListener(newForumListener);
        AppController.AddEventsChangedListener(newEventListener);

        for (Marker marker : eventMarkerMap.keySet()) {
            marker.remove();
        }

        RefreshForumMarkers();
        RefreshEventMarkers();

    }

    public void RefreshForumMarkers() {

        if(forumMarkerMap != null) {
            for (Marker marker : forumMarkerMap.keySet()) {
                marker.remove();
            }
        }

        forumMarkerMap = new HashMap<>();
        List<LocoForum> forums = dao.GetAllForums();

        for (LocoForum forum : forums) {

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(forum.getLocation().getLatitude(), forum.getLocation().getLongitude()))
                    .icon(forumMarkerIcon)
                    .title(forum.getName()));

            forumMarkerMap.put(marker, forum);

        }

    }

    public void RefreshEventMarkers() {

        if(eventMarkerMap != null) {
            for (Marker marker : eventMarkerMap.keySet()) {
                marker.remove();
            }
        }

        eventMarkerMap = new HashMap<>();
        List<LocoEvent> events = dao.GetAllEvents();

        for (LocoEvent event : events) {

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude()))
                    .icon(eventMarkerIcon)
                    .title(event.getName()));

            Log.i("EVENTCHECK", "event name: " + event.getName());

            eventMarkerMap.put(marker, event);

        }

    }

    void RefreshFriendsMarkers() {

        Collection<LocoUser> friends = AppController.GetFriends().values();
        for(Marker marker : friendsMarkersMap.keySet()) {
            marker.remove();
        }

        friendsMarkersMap = new HashMap<>();
        reverseFriendsMap = new HashMap<>();

        for (LocoUser friend : friends) {

            MarkerOptions options = new MarkerOptions()
                    .title(friend.getName())
                    .position(new LatLng(friend.getLocation().getLatitude(), friend.getLocation().getLongitude()));

            if(friend.getSafe()) {
                options.icon(safeFriendMarkerIcon);
            } else {
                options.icon(friendMarkerIcon);
            }

            Marker marker = mMap.addMarker(options);

            friendsMarkersMap.put(marker, friend);
            reverseFriendsMap.put(friend.getMail(), marker);

        }

    }

    public void LocationServiceEnabled() {

        pause = false;
        Log.i(TAG, "Starting worker");
        if(workerThread == null) {
            workerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Log.i(TAG, pause.toString());
                    while (!pause) {

                        sleepTime = 30000;

                        Log.i(TAG, "Worker thread tick");
                        final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                        if (mLastLocation != null) {

                            final GeoPt myLoc = new GeoPt();
                            myLoc.setLatitude((float) mLastLocation.getLatitude());
                            myLoc.setLongitude((float) mLastLocation.getLongitude());

                            AppController.GetMyUser().setLocation(myLoc);
                            ApiHandler.SetMyLocation(myLoc);
                            GetNewUsers();

                            if(mMap == null) {
                                sleepTime = 1000;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (mMap != null) {

                                        if (myMarker == null) {

                                            myMarker = mMap.addMarker(new MarkerOptions()
                                                    .title(AppController.GetMyUser().getName())
                                                    .position(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()))
                                                    .icon(myMarkerIcon));


                                            if (mMap.getCameraPosition().zoom < 5) {

                                                cameraPosition = new CameraPosition.Builder()
                                                        .target(myMarker.getPosition())
                                                        .zoom(11)
                                                        .bearing(0)
                                                        .tilt(0)
                                                        .build();

                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                            }

                                            positionRetrieved = true;


                                        } else {
                                            myMarker.setPosition(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()));
                                        }

                                        if (myMarker != null) {
                                            if (myRangeCircle == null) {
                                                myRangeCircle = mMap.addCircle(new CircleOptions()
                                                        .strokeColor(myCircleColor)
                                                        .strokeWidth(3)
                                                        .radius(myRange)
                                                        .center(myMarker.getPosition()));
                                            } else {
                                                myRangeCircle.setCenter(myMarker.getPosition());
                                                myRangeCircle.setRadius(myRange);
                                            }
                                        }

                                        for (Marker m : currentUserMap.keySet()) {
                                            m.remove();
                                        }

                                        currentUserMap = new HashMap<>();
                                        reverseMarkersMap = new HashMap<>();

                                        List<LocoUser> usersAroundMe = AppController.GetKnownUsersAround(myLoc, myRange);
                                        inUserDrawingPhase = true;

                                        for (LocoUser user : usersAroundMe) {

                                            if (!AppController.CheckIfFriend(user.getMail())) {

                                                Log.i("MARKERCHECK", "Creating marker for " + user.getMail());

                                                MarkerOptions options = new MarkerOptions()
                                                        .position(new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()))
                                                        .title(user.getName());

                                                if (AppController.CheckIfSafe(user.getMail())) {
                                                    options.icon(safePersonMarkerIcon);
                                                } else {
                                                    options.icon(personMarkerIcon);
                                                }

                                                Marker marker = mMap.addMarker(options);
                                                currentUserMap.put(marker, user);
                                                reverseMarkersMap.put(user.getMail(), marker);

                                            }


                                        }

                                    } else {
                                        sleepTime = 1000;
                                    }

                                }

                            });

                        } else {
                            sleepTime = 1000;
                        }

                        inUserDrawingPhase = false;

                        if(!skipSleep) {
                            try {
                                Thread.sleep(sleepTime, 0);
                            } catch (InterruptedException e) {
                                if (workerThread != null) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            skipSleep = false;
                        }

                    }

                }
            });

            workerThread.start();

        }

    }

    void GetNewUsers() {

        ApiHandler.GetUsersAroundMe(AppController.GetMyUser().getLocation(), myRange, AppController.GetMyUser().getMail(), new IApiCallback<List<UserAroundMe>>() {
            @Override
            public void Invoke(List<UserAroundMe> result) {
                if (result != null) {

                    List<UserAroundMe> fResult = result;
                    List<LocoUser> users = new ArrayList<>();
                    List<LocoUser> newUsers = new ArrayList<>();

                    for (UserAroundMe u : fResult) {
                        if (u.getMail().compareTo(AppController.GetMyUser().getMail()) != 0) {

                            LocoUser nUser = new LocoUser(u);
                            if (nUser.getLocation() != null) {
                                users.add(nUser);
                                if (!AppController.CheckKnownUser(nUser.getMail())) {
                                    newUsers.add(nUser);
                                }
                            }

                        }
                    }

                    if(newUsers.size() > 0) {

                        if(inUserDrawingPhase) {
                            skipSleep = true;
                        }
                        dao.AddUsers(newUsers);
                        AppController.SetUsers(dao.GetAllUsers());
                        for (LocoUser user : newUsers) {
                            ApiHandler.Ping(user.getMail());
                        }

                    }
                    AppController.UpdateUsersLocation(users, dao);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RefreshFriendsMarkers();
                        }
                    });

                }

            }

        });

    }

    @Override
    public void onPositive() {

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        waitingForLocationServices = true;

    }

    @Override
    public void onNegative() {
        waitingForLocationServices = true;
    }

}
