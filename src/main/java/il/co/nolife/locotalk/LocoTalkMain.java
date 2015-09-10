package il.co.nolife.locotalk;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
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


public class LocoTalkMain extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "LocoTalkMain";
    private static final int RC_SIGN_IN = 0;
    private boolean mIntentInProgress;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    double lat,lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(getClass().toString(), "it has to work!");
        EndpointApiCreator.initialize(null);
        setContentView(R.layout.map_activity);
        ApiHandler.Initialize(this);


        buildGoogleApiClient2();
        //buildGoogleApiClient();

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

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.

        if (mMap == null) {

            Log.i(getClass().toString(),"map was Null");
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

        BitmapDescriptor desc = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.person));
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Person").icon(desc));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MarkerClicked(marker);
                return false;
            }
        });

    }

    void MarkerClicked(Marker marker){
        Log.i(getClass().toString(), marker.getTitle());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        // Log.i(getClass().toString(),mLastLocation.toString());
        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
        }

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {

            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String personPhoto = currentPerson.getImage().getUrl();
            String personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
            User u = new User();
            u.setFullName(personName);
            u.setMail(personEmail);
            u.setImageUrl(personPhoto);
            GeoPt geo = new GeoPt();
            geo.setLatitude((float) lat);
            geo.setLongitude((float) lon);
            final GeoPt fGeo = geo;

            ApiHandler.Login(u, new IApiCallback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    ApiHandler.SetMyLocation(fGeo);
                }
            });

        }
        setUpMapIfNeeded();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
