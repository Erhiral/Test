package com.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
//import com.medbeds.EventBus.NewLocationFound;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;



public class LocationFounder implements LocationView,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {
    private static final String TAG = "LocationFounder";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 7000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static Location lastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Context context;
//    private EventBus eventBus = EventBus.getDefault();
    public LocationFounder(Context context)

    {
        this.context = context;
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
//        new MultiDatePicker(context);
    }

    private synchronized void buildGoogleApiClient() {
        Log.i("mytag", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Utils.getInstance().d(TAG + " onConnected ");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Utils.getInstance().d(TAG + " onConnectionSuspended ");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Utils.getInstance().d(TAG + " onConnectionFailed ");
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        Utils.getInstance().d(TAG + " onResult ");
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    status.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
//        Utils.getInstance().d(TAG + " onLocationChanged ");
        lastKnownLocation = location;
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Prefs.getPrefInstance().setValue(context, Const.LAT, String.valueOf(lat));
        Prefs.getPrefInstance().setValue(context, Const.LON, String.valueOf(lng));
        Utils.getInstance().d(TAG + " lat : "+lat+" lng : "+lng);
//        Toast.makeText(this,"lat :"+location.getLatitude()+" long : "+location.getLongitude()+" Accuracy : "+ location.getAccuracy(),Toast.LENGTH_SHORT).show();

//            eventBus.post(new NewLocationFound(location));
        if (location.getAccuracy() < 20) {
        }
    }

    @Override
    public void onResume() {
//        if (!eventBus.isRegistered(this)) {
//            eventBus.register(this);
//        }
    }
//
//    @Subscribe(threadMode = ThreadMode.BACKGROUND)
//    public void gotNewLocation(NewLocationFound newLocationFound) {
//        Location location = newLocationFound.getLocation();
//
//    }

    public static Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    @Override
    public void onDestroy() {
//        if (eventBus.isRegistered(this)) {
//            eventBus.unregister(this);
//        }
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onStart() {
        Utils.getInstance().d("");
        mGoogleApiClient.connect();

    }

    @Override
    public void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        );
        mGoogleApiClient.disconnect();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
            }
        });

    }
}
