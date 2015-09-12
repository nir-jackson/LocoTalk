package il.co.nolife.locotalk;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.aroundme.EndpointApiCreator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocoTalkMain extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "LocoTalkMain";
    private static final int RC_SIGN_IN = 0;
    private boolean mIntentInProgress;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;

    HashMap<Marker, LocoUser> currentUserMarkers;
    Marker myMarker;
    LocoUser myUser;

    BitmapDescriptor personMarkerIcon;

    AsyncTask<Void, Void, Void> backgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "it has to work!");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        EndpointApiCreator.initialize(null);
        setContentView(R.layout.map_activity);
        ApiHandler.Initialize(this);

        personMarkerIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person));

        buildGoogleApiClient2();
        //buildGoogleApiClient();

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if(backgroundTask != null) {
            backgroundTask.cancel(true);
        }

    }

    protected void onResume() {
        super.onResume();

        if(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
            LocationServiceEnabled();
        }

    }

    protected synchronized void buildGoogleApiClient2(){

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
                setUpMap();
            }

        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MarkerClicked(marker);
                return false;
            }
        });

    }

    void MarkerClicked(Marker marker){

        Log.i(TAG, "pressed Marker, suppose to open new activity");
        Intent intent = new Intent(this,ChatActivity.class);
        startActivity(intent);


        //Log.i(getClass().toString(), marker.getTitle());
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

            myUser = new LocoUser();

            myUser.setName(personName);
            if (personEmail.compareTo("nir.jackson89@gmail.com")==0){
                personEmail="nir.jackson890@gmail.com";
            }else if (personEmail.compareTo("iloriginal@gmail.com")==0){
                personEmail="iloriginal0@gmail.com";
            }

            myUser.setMail(personEmail);
            myUser.setIcon(personPhoto);

            ApiHandler.GetRegistrationId(new IApiCallback<String>() {
                @Override
                public void Invoke(String result) {
                    myUser.setRegId(result);
                    ApiHandler.Login(myUser.toUser(), new IApiCallback<Boolean>() {
                        @Override
                        public void Invoke(Boolean result) {
                            if(result) {
                                Log.i(TAG, "Successfully logged in to AroundMe backend");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.splash).setVisibility(View.GONE);
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
            if(mLastLocation != null) {
                LocationServiceEnabled();
            } else {

                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                startActivity(intent);

            }

        }

        setUpMapIfNeeded();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void LocationServiceEnabled() {
        Log.i(TAG,"LocationServiceEnabled function");
        backgroundTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                while(true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Log.i(TAG,"stam bdika");
                            final GeoPt myLoc = new GeoPt();

                            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            myLoc.setLatitude((float) mLastLocation.getLatitude());
                            myLoc.setLongitude((float) mLastLocation.getLongitude());

                            myUser.setLocation(myLoc);
                            ApiHandler.SetMyLocation(myLoc);

                            if (myMarker == null) {
                                myMarker = mMap.addMarker(new MarkerOptions()
                                        .title(myUser.getName())
                                        .position(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()))
                                        .icon(personMarkerIcon));
                            }else {
                                myMarker.setPosition(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()));
                            }
                            ApiHandler.GetUsersAroundMe(10000, new IApiCallback<List<UserAroundMe>>() {
                                @Override
                                public void Invoke(List<UserAroundMe> result) {
                                    if (result != null) {
                                        Log.i(TAG,result.toString());
                                        if (currentUserMarkers != null) {
                                            for (Map.Entry<Marker, LocoUser> p : currentUserMarkers.entrySet()) {
                                                p.getKey().remove();
                                            }
                                        }
                                        currentUserMarkers = new HashMap<Marker, LocoUser>();

                                        for (UserAroundMe u : result) {

                                            if (u.getMail().compareTo(myUser.getMail()) != 0) {

                                                LocoUser nUser = new LocoUser(u);
                                                AppController.AddUserToCache(nUser.toUser());
                                                Date seenDate = new Date(u.getLastSeen().getValue());
                                                Date now = new Date();
                                                Date checkDate = new Date(now.getTime() - 300000); // Current minus 5 minutes
                                                if (seenDate.after(checkDate)) {

                                                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(nUser.getLocation().getLatitude(), nUser.getLocation().getLongitude()))
                                                            .title(nUser.getName())
                                                            .icon(personMarkerIcon));
                                                    currentUserMarkers.put(newMarker, nUser);

                                                }

                                            }

                                        }

                                    }
                                }
                            });
                        }
                    });
                    try {
                        Thread.sleep(3000, 0);
                    } catch (InterruptedException e) {
                        Log.e("TickerThread", e.getMessage());
                        e.printStackTrace();
                    }

                }

            }

        };

        backgroundTask.execute();

    }

}
