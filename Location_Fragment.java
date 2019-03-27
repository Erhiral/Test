package com.membership.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatDialog;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.membership.Activity.HomeActivity;
import com.membership.ApiClient;
import com.membership.ApiInterface;
import com.membership.DataStorage;
import com.membership.R;
import com.membership.Response.LocationModel;
import com.membership.ResponseModel.LocationListResponse;
import com.membership.Utils.CheckNetwork;
import com.membership.Utils.Const;
import com.membership.Utils.CustomHeaderWithRelative;
import com.membership.Utils.GPSTracker;
import com.membership.Utils.Prefs;
import com.membership.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.github.florent37.runtimepermission.RuntimePermission.askPermission;


@SuppressLint("ValidFragment")
public class Location_Fragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    View rootView;
    private final String TAG = Location_Fragment.this.getClass().getSimpleName();
    MarkerOptions markerOptions;
    private  GoogleMap mgoogleMap;
    private AppCompatDialog dialogTripCancel;
    private Double lat;
    private Double lng;
    private Marker marker;
    private static final LatLng BOUNDS = new LatLng(23.025753, 72.492060);
    //    private static final LatLngBounds BOUNDS = new LatLngBounds(new LatLng(23.025753, 72.492060), new LatLng(23.0333, 72.6167));
    //    private PlaceAutocompleteAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    private final static int PLACE_AUTOCOMPLETE_REQUEST_CODE_TO = 12;
    FragmentManager fragmentManager;
    RelativeLayout lin_header;
    private Context context;
    ImageView iv_menu, iv_myLocation;
    DrawerLayout drawer;
//    private MapAreaManager circleManager;

    List<LocationListResponse> locationListResponse= new ArrayList<>();
    public Location_Fragment(Context context, FragmentManager fragmentManager, RelativeLayout lin_header, DrawerLayout drawer) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.lin_header = lin_header;
        this.drawer = drawer;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        if (rootView != null) {
//            ViewGroup parent = (ViewGroup) rootView.getParent();
//            if (parent != null)
//                parent.removeView(rootView);
//        }
//        try {
            rootView = inflater.inflate(R.layout.location_fragment, container, false);
//        } catch (InflateException e) {
//            /* map is already there, just return view as it is */
//        }


        CustomHeaderWithRelative.setOuter(getActivity(), drawer, lin_header, "Locate My Friends");
        lin_header.setVisibility(View.VISIBLE);
        iv_myLocation = (ImageView) rootView.findViewById(R.id.iv_myLocation);
//        CheckPermission();
//        if(CheckNetwork.isInternetAvailable(getContext())){
//            api();
//        }else {
//            Toast.makeText(getContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
//        }
        final GPSTracker gpsTracker = new GPSTracker(context);
        if (gpsTracker.canGetLocation()) {
            Log.d("mytag", "in gps enabled");
            lat = gpsTracker.getLatitude();
            lng = gpsTracker.getLongitude();
            Prefs.getPrefInstance().setValue(context, Const.LAT, String.valueOf(lat));
            Prefs.getPrefInstance().setValue(context, Const.LON, String.valueOf(lng));
        } else {
            gpsTracker.showSettingsAlert();
        }
        initMap();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                CheckPermission();
////                askpermissionthis();
//            }
//        },100);
    }

    private void initMap() {
        if(mgoogleMap == null){
            SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
            if (fragment != null) {
                fragment.getMapAsync(this);
            }
        }
    }

    private void mapListener() {
        mgoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
//                mgoogleMap.getCameraPosition();
            }
        });

        mgoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
//                Utils.getInstance().d("onCameraMove");
            }
        });

        mgoogleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {
//                Utils.getInstance().d("onCameraMoveCanceled");

            }
        });
        mgoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
//                Utils.getInstance().d("onCameraIdle");
            }
        });

        mgoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
//                LatLng latLng = new LatLng(lat, lng);
//                CameraPosition cameraPosition = new CameraPosition.Builder()
//                        .target(latLng)
//                        .zoom(10)
//                        .bearing(90)
//                        .tilt(30)
//                        .build();
//                mgoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Utils.getInstance().d(TAG  + "Inside get onMapReady");
        mgoogleMap = googleMap;
        mgoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mgoogleMap.getUiSettings().setCompassEnabled(false);
        mgoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mgoogleMap.setMyLocationEnabled(true);
//        lat = 23.0312111;
//        lng = 72.5606431;
        final GPSTracker gpsTracker = new GPSTracker(context);
        if (gpsTracker.canGetLocation()) {
            Log.d("mytag", "in gps enabled");
            lat = gpsTracker.getLatitude();
            lng = gpsTracker.getLongitude();
            Prefs.getPrefInstance().setValue(context, Const.LAT,String.valueOf(lat));
            Prefs.getPrefInstance().setValue(context, Const.LON,String.valueOf(lng));
            Log.d("mytag","latlong"+ lat+"  "+lng+" ");
        } else {
            gpsTracker.showSettingsAlert();
        }
        LatLng myLocation = new LatLng(lat,lng);
        String latlong=Prefs.getPrefInstance().getValue(context, Const.LAT, "");
        String longl=Prefs.getPrefInstance().getValue(context, Const.LON, "");
        if(!latlong.isEmpty()&&!longl.isEmpty()) {
            if(CheckNetwork.isInternetAvailable(context)){
                api();
            } else {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }else {
            if(CheckNetwork.isInternetAvailable(context)){
                api();
            } else {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }
//        if(marker != null){
//            marker.setPosition(myLocation);
//        } else {
//            marker = mgoogleMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
//        }

        mgoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mgoogleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
//                    mgoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)
                .zoom(10)
//                .bearing(90)
                .tilt(30)
                .build();
        mgoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mapListener();

        iv_myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = mgoogleMap.getMyLocation();
                LatLng latLng = new LatLng(lat,lng);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(16)
//                        .bearing(90)
                        .tilt(30)
                        .build();
                mgoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        mgoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.infowindow, null);
                // Getting reference to the TextView to set title
                TextView note = (TextView) v.findViewById(R.id.nation_name);
                note.setText(marker.getTitle() );
                return v;
            }
        });

        mgoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getSnippet()!=null){
//                    Log.d("mytag", "useradd  marker.getSnippet();"+ marker.getSnippet());
//                    new AlertDialog.Builder(context)
//                            .setCancelable(false)
//                            .setMessage("Is A Nation Member ?")
//                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }).show();



//                    pushFragment(new Fragment_Department(context, fragmentManager, lin_header, drawer, Integer.valueOf(marker.getSnippet())), "depart", true);
                    //call the intent here
                }
            }
        });

//        if(hospitalgeoLists.size()>0) {
//            for (int i = 0; i>=1; i++){
//                Log.d("mytag","marker set");
//                LatLng myLocation1 = new LatLng(Double.valueOf(hospitalgeoLists.get(i).getHospitalLatitude()),Double.valueOf(hospitalgeoLists.get(i).getHospitalLongitude()));
////                usermarker.setPosition(myLocation1);
////                usermarker=mgoogleMap.addMarker(new MarkerOptions().position(myLocation1).title(hospitalgeoLists.get(i).getHospitalName()));
//                markerOptions = new MarkerOptions()
//                        .position(myLocation1)
//                        .title(hospitalgeoLists.get(i).getHospitalName());
//
//                usermarker = mgoogleMap.addMarker(markerOptions);
////                            progressDialog.dismiss();
//
//            }
//        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onPause() {
        Utils.getInstance().d(TAG + "onPause");
        super.onPause();
//        mGoogleApiClient.stopAutoManage(getActivity());
//        mGoogleApiClient.disconnect();
        mgoogleMap = null;
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        FragmentManager fM = getFragmentManager();

    }

    @Override
    public void onStop(){
        super.onStop();
        Utils.getInstance().d(TAG + "onStop");
    }

    @Override
    public void onStart(){
        super.onStart();
        Utils.getInstance().d(TAG + "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Utils.getInstance().d(TAG + "onResume");
        initMap();
//        initViews();
//        listener();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Utils.getInstance().d(TAG + "onDestroy");
//        mGoogleApiClient.stopAutoManage(getActivity());
//        mGoogleApiClient.disconnect();

    }

    public void api() {
//        locationListResponse= new ArrayList<>();
         ApiInterface  apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LocationModel> getUserCall = apiInterface.getlocationlist(Integer.parseInt(String.valueOf(DataStorage.read("id",DataStorage.INTEGER))),Prefs.getPrefInstance().getValue(context,Const.LAT,""),Prefs.getPrefInstance().getValue(context,Const.LON,""));
        getUserCall.enqueue(new Callback<LocationModel>() {
            @Override
            public void onResponse(Call<LocationModel> call, Response<LocationModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals(1)){
                        locationListResponse = response.body().getData();
                        if(locationListResponse.size()>0){
                            for (int i = 0; i < locationListResponse.size(); i++) {
                                Log.d("mytag", "marker set");

                                if (locationListResponse.get(i).getLatitude() != "") {
                                    Log.d("mytag","Marker set defulat name");
                                    LatLng myLocation1 = new LatLng(Double.parseDouble(locationListResponse.get(i).getLatitude()), Double.parseDouble(locationListResponse.get(i).getLongitude()));
//                usermarker.setPosition(myLocation1);
//                usermarker=mgoogleMap.addMarker(new MarkerOptions().position(myLocation1).title(hospitalgeoLists.get(i).getHospitalName()));
                                      String defulat_name;
                                      if(locationListResponse.get(i).getDefault_name().isEmpty()) {
                                          if (locationListResponse.get(i).getDefault_name_status() == 0) {
                                              defulat_name = "Nation Member";
                                          } else {
                                              defulat_name = (String) DataStorage.read("fname", DataStorage.STRING);
                                          }
                                      }else {
                                          defulat_name=locationListResponse.get(i).getDefault_name();}


                                           MarkerOptions markerOptions = new MarkerOptions()
                                            .position(myLocation1)
//                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_icon), context.getResources().getDimensionPixelSize(R.dimen._23sdp), context.getResources().getDimensionPixelSize(R.dimen._35sdp), false)))
                                            .title(defulat_name)
                                            .snippet(String.valueOf(locationListResponse.get(i).getFname()));
                                           mgoogleMap.addMarker(markerOptions).showInfoWindow();
                                    Log.d("mytag","Marker set defulat name");

//                            Bitmap myBitmapDevice = BitmapFactory.decodeFile(imgFile.getPath());
//                            Log.d("mytt", "aaa==aa======xxx>" + myBitmapDevice);
//                            int height = 100;
//                            int width = 100;
//                            Bitmap smallMarker = Bitmap.createScaledBitmap(myBitmapDevice, width, height, false);

                                    // usermarker.showInfoWindow();

                                    //  usermarker.showInfoWindow();


                                    final int finalI = i;
//                            mgoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                                @Override
//                                public boolean onMarkerClick(Marker marker) {
//                                    Log.d("mytag","jhdcaskjfbh"+marker);
////                                    if(marker.equals(usermarker)) {
////                                    if(marker)
//                                        Log.d("mytag", "useradd  user");
//                                        Log.d("mytag", "useradd hospitalid"+hospitalgeoLists.get(finalI).getHospitalId());
//                                        pushFragment(new Fragment_Department(context, fragmentManager, lin_header, drawer, hospitalgeoLists.get(finalI).getHospitalId()), "depart", true);
//                                        //call the intent here
//
//                                   // }
//                                        return true;
//
//
////                                    return true;
//                                }
//                            });


                                }
                            }
                        }

                    }
                }

            }

            @Override
            public void onFailure(Call<LocationModel> call, Throwable t) {


            }
        });


        }

    private void CheckPermission(){
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Fine Location");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Coarse Location");
        if(permissionsList.size() > 0){
          if(!Settings.System.canWrite(getContext())){
              askpermissionthis();
//                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }

            Log.d("mytag","Permisstion Fragment");

            return;
        }
    }


    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
//                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION == PackageManager.PERMISSION_GRANTED))&&perms.get (Manifest.permission.ACCESS_COARSE_LOCATION == PackageManager.PERMISSION_GRANTED)) {
//                    SelectImage();
//                } else {
//                    Toast.makeText(getContext(), "Some Permission is Denied", Toast.LENGTH_SHORT).show();
//                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void askpermissionthis(){
       // if (f instanceof Location_Fragment) {
        askPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .onAccepted((result) -> {
                    //all permissions already granted or just granted
                    for (int i = 0; i <result.getAccepted().size() ; i++) {
                        Log.d("thistag", "Accepted : " + result.getAccepted().get(i));
                    }
                    Integer  permi= Integer.parseInt(Prefs.getPrefInstance().getValue(getContext(),Const.Status,""));


                })
                .onDenied((result) -> {
                    //the list of denied permissions
                    for (String permission : result.getDenied()) {
                        Log.d("thistag", "Denied : " + permission);

                    }
                    //permission denied, but you can ask again, eg:

                    new AlertDialog.Builder(getContext())
                            .setCancelable(false)
                            .setMessage("We need to access your location services in order to show you other members on the map. Please allow the same.")
                            .setPositiveButton("yes", (dialog, which) -> {
                                result.askAgain();
                            }) // ask again
                            .show();

                })
                .onForeverDenied((result) -> {
                    //the list of forever denied permissions, user has check 'never ask again'
                    for (String permission : result.getForeverDenied()) {
                        Log.d("thistag", "Forever Denied : " + permission);
                    }

                    new AlertDialog.Builder(getContext())
                            .setCancelable(false)
                            .setMessage("We need to access your location services in order to show you other members on the map. Please allow the same.")
                            .setPositiveButton("yes", (dialog, which) -> {
                                result.goToSettings();
                            }) // ask again

                            .show();

                    // you need to open setting manually if you really need it



                })
                .ask();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) {
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
    }


}
