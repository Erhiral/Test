package com.globalwebsoft.global_truecaller.Other;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.globalwebsoft.global_truecaller.InterFace.OnNewLocationListener;
import com.globalwebsoft.global_truecaller.MainActivity;
import com.globalwebsoft.global_truecaller.R;
import com.globalwebsoft.global_truecaller.Retrofit.APIClient;
import com.globalwebsoft.global_truecaller.Retrofit.APIInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;


public class CallReceiverNew extends BroadcastReceiver {
    private static LinearLayout ly1;
    TelephonyManager telManager;
    Context context;
    Boolean isshowing = true;
    String number;
    int broadcastcall = 0;
    boolean startedCall = false;
    boolean broadcastTriggerd = false;
    private static int mLastState;
    public static final int MIN_TIME_REQUEST = 5 * 1000;
    String globalstatus = "";
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    public static final int REQUEST_CODE = 5912;
    private static Context _context;
    private static Location currentLocation;
    private static Location prevLocation;
    private static LocationManager locationManager;

    MyProgressDialog myProgressDialog;

    private  static  LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            try {
                String strStatus = "";
                switch(status){
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        strStatus = "GPS_EVENT_FIRST_FIX";
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        strStatus = "GPS_EVENT_SATELLITE_STATUS";
                        break;
                    case GpsStatus.GPS_EVENT_STARTED:
                        strStatus = "GPS_EVENT_STARTED";
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        strStatus = "GPS_EVENT_STOPPED";
                        break;
                    default:
                        strStatus = String.valueOf(status);
                        break;
                }
                Toast.makeText(_context, "Status: " + strStatus,
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                Toast.makeText(_context, "***new location***",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        }
    };


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        context = context;

        if (extras != null) {
            if (intent.getAction().equals("android.intent.action.READ_CALL_LOG")) {
                number = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            } else {
                number = intent.getStringExtra("incoming_number");
                this.context = context;
                startedCall = false; // New added boolean
                telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCallStateChanged(final int state, final String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            _context = context;
            locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_REQUEST, 5, locationListener);
                Location gotLoc = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(gotLoc != null) {
                    double latitude = gotLoc.getLatitude();
                    double longitude = gotLoc.getLongitude();
                    Toast.makeText(context, "Mobile Location (NW): \nLatitude: " + latitude + "\nLongitude: " + longitude,
                            Toast.LENGTH_LONG).show();
                }
            } else {
          Toast.makeText(context, "please turn on GPS", Toast.LENGTH_LONG).show();
     //                t.setGravity(Gravity.CENTER, 0, 0);
    //                t.show();

                Intent settinsIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                settinsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settinsIntent);

//                PermissionUtil.checkPermission((Activity)context, Manifest.permission.ACCESS_FINE_LOCATION,
//                        new PermissionUtil.PermissionAskListener() {
//                            @Override
//                            public void onPermissionAsk() {
//                                ActivityCompat.requestPermissions(
//                                        (Activity) context,
//                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                                 REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
//                                );
//                            }
//                            @Override
//                            public void onPermissionPreviouslyDenied() {
//                                //show a dialog explaining permission and then request permission
//                            }
//                            @Override
//                            public void onPermissionDisabled() {
//                                Toast.makeText(context, "Permission Disabled.", Toast.LENGTH_SHORT).show();
//                            }
//                            @Override
//                            public void onPermissionGranted() {
//                               // readContacts();
//                            }
//                        });


//                final Dialog dialog = new Dialog(new ContextThemeWrapper(context.getApplicationContext(), R.style.DialogSlideAnim));
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                //  dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//                } else {
//                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
//                }
//                dialog.setContentView(R.layout.dialog_gps);
//
//                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog.getWindow().setGravity(Gravity.CENTER);
//                dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                final TextView button = dialog.findViewById(R.id.dialog_ok);
//
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.cancel();
//                        Intent i= new Intent(context,MainActivity.class);
//                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(i);
//                    }
//                });
//                dialog.show();

//
//                showMessageOKCancel("Please Turn On Location GPS.",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    Intent i= new Intent(context,MainActivity.class);
//                                    context.startActivity(i);
//                                }
//                            }
//                        });
            }

//
//            try
//            {
//                if (isOnline(context)) {
//                    Toast.makeText(context, "Internet", Toast.LENGTH_SHORT).show();
//                    Log.e("keshav", "Online Connect Intenet ");
//                } else {
//                    Toast.makeText(context, "Internet Not Connected", Toast.LENGTH_SHORT).show();
//                   // dialog(false);
//                    Log.e("keshav", "Conectivity Failure !!! ");
//                }
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//            }

            if(incomingNumber!=null) {
                Log.d("mytag","Cuttrnt State"+state);
                if (number != null) {
                    if (state != mLastState){
                        mLastState = state;
                        Log.d("mytag","State"+state);
                        globalstatus="";
                        try {
                            switch(state){
                                case TelephonyManager.CALL_STATE_RINGING: {
                                    Log.d("mytag", "CALL_STATE_RINGING" + number);
                                    final Dialog dialog = new Dialog(new ContextThemeWrapper(context.getApplicationContext(), R.style.DialogSlideAnim));
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView(R.layout.dialog);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                    dialog.getWindow().setLayout(
                                                    ViewGroup.LayoutParams.FILL_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT);

                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                    } else {
                                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                                    }
                                   TextView te_number = (TextView) dialog.findViewById(R.id.number);
                                   final TextView lead=(TextView)dialog.findViewById(R.id.lead);
                                   final TextView followup=(TextView)dialog.findViewById(R.id.followup);
                                   final TextView client=(TextView)dialog.findViewById(R.id.client);

                                   lead.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Toast.makeText(context, "Click lead", Toast.LENGTH_SHORT).show();
                                           globalstatus="lead";
                                           Log.d("mytag","globalstatus"+globalstatus);
                                           followup.setTextColor(context.getResources().getColor(R.color.black));
                                           lead.setTextColor(context.getResources().getColor(R.color.colorhint));
                                           client.setTextColor(context.getResources().getColor(R.color.black));
                                       }
                                   });

                                   followup.setOnClickListener(new View.OnClickListener(){
                                       @Override
                                       public void onClick(View v){
                                           globalstatus="Followup";
                                           followup.setTextColor(context.getResources().getColor(R.color.colorhint));
                                           lead.setTextColor(context.getResources().getColor(R.color.black));
                                           client.setTextColor(context.getResources().getColor(R.color.black));
                                       }
                                   });
                                   client.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           globalstatus="Client";
                                           client.setTextColor(context.getResources().getColor(R.color.colorhint));
                                           lead.setTextColor(context.getResources().getColor(R.color.black));
                                           followup.setTextColor(context.getResources().getColor(R.color.black));

                                       }
                                   });

                                    final EditText tv_client_name = (EditText) dialog.findViewById(R.id.tv_client_name);
                                    final EditText tv_description = (EditText) dialog.findViewById(R.id.tv_description);
                                    final EditText tv_date = (EditText) dialog.findViewById(R.id.tv_date);
                                    final EditText tv_time = (EditText) dialog.findViewById(R.id.tv_time);

                                    tv_date.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v){
                                            final DatePickerDialog datePickerDialog;
                                            final SimpleDateFormat dateFormat;
                                            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                            final Calendar newDate = Calendar.getInstance();
                                            datePickerDialog = new DatePickerDialog(context.getApplicationContext(), new DatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                                      int dayOfMonth) {
                                                    newDate.set(year, monthOfYear, dayOfMonth);
                                                    tv_date.setText(dateFormat.format(newDate.getTime()));
                                                }
                                            }, 2019, newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
                                            Calendar calendar= Calendar.getInstance();
                                            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                                     //       datePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                datePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                            } else {
                                                datePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                                            }
                                            datePickerDialog.show();

//                                            tv_time.setOnClickListener(new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//
//                                                    // TODO Auto-generated method stub
////                                                    datePickerDialog.show();
//                                                }
//                                            });
                                        }
                                    });

                                    tv_time.setOnClickListener(new View.OnClickListener(){
                                        @Override
                                        public void onClick(View v) {
                                            //   Toast.makeText(context, "time", Toast.LENGTH_SHORT).show();
                                            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getRootView().getContext(),
                                                    new TimePickerDialog.OnTimeSetListener() {

                                                        @Override
                                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                            tv_time.setText( "" + hourOfDay + ":" + minute);
                                                        }
                                                    }
                                                    , 10, 10, false

                                            );

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                timePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                            } else {
                                                timePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                                            }
                                            timePickerDialog.show();

                                        }
                                    });

                                    if (incomingNumber != null) {
                                        te_number.setText(incomingNumber);
                                    }
                                    tv_client_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (hasFocus) {
                                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                                InputMethodManager imm = (InputMethodManager) dialog.getWindow().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                if (imm != null)
                                                    imm.showSoftInput(tv_client_name, InputMethodManager.SHOW_IMPLICIT);
                                            }
                                        }
                                    });
                                    tv_description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if(hasFocus){
                                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                                InputMethodManager imm = (InputMethodManager) dialog.getWindow().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                if (imm != null)
                                                    imm.showSoftInput(tv_description, InputMethodManager.SHOW_IMPLICIT);
                                            }
                                        }
                                    });

                                     final TextView button = dialog.findViewById(R.id.dialog_ok);
                                         button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (tv_client_name.getText().toString().trim().isEmpty()) {
                                                Toast.makeText(context, "Please Enter Client Name", Toast.LENGTH_SHORT).show();
                                            } else if (tv_description.getText().toString().isEmpty()) {
                                                Toast.makeText(context, "Please Enter Client Communication", Toast.LENGTH_SHORT).show();
                                            } else {
                                                dialog.dismiss();
                                                if (incomingNumber != null) {
                                                    Toast.makeText(context, "data insert SucessFully", Toast.LENGTH_SHORT).show();
                                                    Register();
                                                    /*if (button.getText().toString().equals("Submit")) {

                                                        Log.d("mytag", "addd Database" + incomingNumber);
                                                        Realm realm = null;
                                                        try {
                                                            realm = Realm.getDefaultInstance();
                                                            realm.executeTransaction(new Realm.Transaction(){
                                                                @Override
                                                                public void execute(Realm realm) {
                                                                    Number num = realm.where(DatabaseModel.class).max("id");
                                                                    int nextId = (num == null) ? 1 : num.intValue() + 1;
                                                                    DatabaseModel student = realm.createObject(DatabaseModel.class, nextId);
                                                                    student.setName(tv_client_name.getText().toString().trim());
                                                                    student.setCallstatus("InComing");
                                                                    student.setGlobalsatus(globalstatus);
                                                                    student.setMobile(number);
                                                                    student.setDate(tv_date.getText().toString().trim());
                                                                    student.setTime(tv_time.getText().toString().trim());
                                                                    student.setDescription(tv_description.getText().toString().trim());
                                                                    realm.insertOrUpdate(student);
                                                                }
                                                            });
                                                        } finally {
                                                            if (realm != null) {
                                                                realm.close();
                                                            }
                                                        }
                                                    }*/
                                                }
//                                                else {
//
//                                                }
                                            }
                                        }
                                    });
                               //     dialog.show();
                                    break;
                                }
                                case TelephonyManager.CALL_STATE_OFFHOOK:{

                                    if (state != TelephonyManager.CALL_STATE_RINGING){

                                        if (incomingNumber != null) {
                                            broadcastcall++;
                                            Log.d("mytag", "CALL_STATE_OFFHOOK" + incomingNumber.toString());
                                        }
                                    }

                                    Log.d("mytag", "Number" + number);
                                }
                                break;
                                case TelephonyManager.CALL_STATE_IDLE:{
                                    Log.d("mytag", "IDLE" + incomingNumber);
//                        if (broadcastcall==1) {
                                    Log.d("mytag", "IDLE CAll Dailog" + incomingNumber);
                                    final Dialog dialog = new Dialog(new ContextThemeWrapper(context.getApplicationContext(), R.style.DialogSlideAnim));
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    //  dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                    } else {
                                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                                    }
                                    dialog.setContentView(R.layout.dialog);
                                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                    TextView te_number = (TextView) dialog.findViewById(R.id.number);
                                    final EditText tv_client_name = (EditText) dialog.findViewById(R.id.tv_client_name);
                                    final EditText tv_description = (EditText) dialog.findViewById(R.id.tv_description);

                                    final EditText tv_date = (EditText) dialog.findViewById(R.id.tv_date);
                                    final EditText tv_time = (EditText) dialog.findViewById(R.id.tv_time);
                                    tv_time.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                         //   Toast.makeText(context, "time", Toast.LENGTH_SHORT).show();
                                            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getRootView().getContext(),
                                                    new TimePickerDialog.OnTimeSetListener() {

                                                        @Override
                                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                            tv_time.setText( "" + hourOfDay + ":" + minute);
                                                        }
                                                    }
                                                    , 10, 10, false

                                            );

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                timePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                            } else {
                                                timePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                                            }
                                            timePickerDialog.show();

                                        }
                                    });

                                    tv_date.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final DatePickerDialog datePickerDialog;
                                            final SimpleDateFormat dateFormat;
                                            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                            final Calendar newDate = Calendar.getInstance();
                                            datePickerDialog = new DatePickerDialog(v.getRootView().getContext(), new DatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                                      int dayOfMonth) {
                                                    newDate.set(year, monthOfYear, dayOfMonth);
                                                    tv_date.setText(dateFormat.format(newDate.getTime()));
                                                }
                                            }, 2019, newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
                                         //   datePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                datePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                                            } else {
                                                datePickerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                                            }

                                            Calendar calendar= Calendar.getInstance();
                                            datePickerDialog.getDatePicker().setMinDate(newDate.getTimeInMillis());
                                            datePickerDialog.show();

//                                            tv_time.setOnClickListener(new View.OnClickListener() {
//                                                @Override

//                                                public void onClick(View v) {
//
//                                              // TODO Auto-generated method stub
////                                                    datePickerDialog.show();
//                                                }
//                                            });
                                        }
                                    });

                   tv_client_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (hasFocus) {
                                                Log.d("mytag", "hasfocus call");
                                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                                InputMethodManager imm = (InputMethodManager) dialog.getWindow().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                if (imm != null)
                                                    imm.showSoftInput(tv_client_name, InputMethodManager.SHOW_IMPLICIT);
                                            }
                                        }
                                    });

                                    tv_description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (hasFocus) {
                                                Log.d("mytag", "tv_description call");
                                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                                InputMethodManager imm = (InputMethodManager) dialog.getWindow().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                if (imm != null)
                                                    imm.showSoftInput(tv_description, InputMethodManager.SHOW_IMPLICIT);
                                            }
                                        }
                                    });
                                    if (number != null) {
                                        te_number.setText(number);
                                    }

                                    final TextView lead=(TextView)dialog.findViewById(R.id.lead);
                                    final TextView followup=(TextView)dialog.findViewById(R.id.followup);
                                    final TextView client=(TextView)dialog.findViewById(R.id.client);

                                    lead.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(context, "Click lead", Toast.LENGTH_SHORT).show();
                                            globalstatus="lead";
                                            Log.d("mytag","globalstatus"+globalstatus);
                                            followup.setTextColor(context.getResources().getColor(R.color.black));
                                            lead.setTextColor(context.getResources().getColor(R.color.colorhint));
                                            client.setTextColor(context.getResources().getColor(R.color.black));
                                        }

                                    });

                                    followup.setOnClickListener(new View.OnClickListener(){
                                        @Override
                                        public void onClick(View v){
                                            globalstatus="Followup";
                                            followup.setTextColor(context.getResources().getColor(R.color.colorhint));
                                            lead.setTextColor(context.getResources().getColor(R.color.black));
                                            client.setTextColor(context.getResources().getColor(R.color.black));
                                        }
                                    });

                                    client.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v){
                                            globalstatus="Client";
                                            client.setTextColor(context.getResources().getColor(R.color.colorhint));
                                            lead.setTextColor(context.getResources().getColor(R.color.black));
                                            followup.setTextColor(context.getResources().getColor(R.color.black));
                                        }
                                    });

                                    final TextView button = dialog.findViewById(R.id.dialog_ok);
//                                    Realm realm = null;
//                                    try{
//                                        realm = Realm.getDefaultInstance();
//                                        realm.executeTransaction(new Realm.Transaction(){
//                                            @Override
//                                            public void execute(Realm realm){
//                                                Number num = realm.where(DatabaseModel.class).max("id");
//                                                // int nextId = (num == null) ? 1 : num.intValue() + 1;
//                                                // S//tudentRelam student = realm.createObject(StudentRelam.class, nextId);
//                                                DatabaseModel employee = realm.where(DatabaseModel.class).equalTo("mobile", number).findFirst();
//                                               if(employee!=null){
//                                                   Log.d("mytag", "mobile" + employee);
//                                                   //DatabaseModel student = realm.where(DatabaseModel.class).equalTo("id", item.getId()).findFirst();
//                                                   tv_client_name.setText(employee.getName());
//                                                   tv_description.setText(employee.getDescription());
//                                                   tv_date.setText(employee.getDate());
//                                                   tv_time.setText(employee.getTime());
//                                                   Log.d("mytag", "name" + employee.getName());
//                                                   Log.d("mytag", "mobile" + employee.getMobile());
//
//                                                   if(employee.getGlobalsatus().equals("Client")){
//                                                       globalstatus="Client";
//                                                       client.setTextColor(context.getResources().getColor(R.color.colorhint));
//                                                   }else if(employee.getGlobalsatus().equals("Followup")){
//                                                       globalstatus="Followup";
//                                                       followup.setTextColor(context.getResources().getColor(R.color.colorhint));
//                                                   }else if(employee.getGlobalsatus().equals("lead")){
//                                                       globalstatus="lead";
//                                                       lead.setTextColor(context.getResources().getColor(R.color.colorhint));
//                                                   }
//                                                   button.setText("Update");
//                                               }
//                                            }
//                                        });
//                                    } finally {
//                                        if (realm != null) {
//                                            realm.close();
//                                        }
//                                    }
                              button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v){
                                            if(incomingNumber != null){
                                                Log.d("mytag", "addd Database" + incomingNumber);
                                                if (tv_client_name.getText().toString().trim().isEmpty()) {
                                                    Toast.makeText(context, "Please Enter  Name", Toast.LENGTH_SHORT).show();
                                                } else if (tv_description.getText().toString().isEmpty()) {
                                                    Toast.makeText(context, "Please Enter description", Toast.LENGTH_SHORT).show();
                                                }if(globalstatus==null){
                                                    Toast.makeText(context, "Please select Status Communication", Toast.LENGTH_SHORT).show();
                                                } else if(tv_date.getText().toString().trim().isEmpty()){
                                                    Toast.makeText(context, "Please Enter Date", Toast.LENGTH_SHORT).show();
                                                } else  if(tv_time.getText().toString().isEmpty()){
                                                    Toast.makeText(context, "Please Enter Time", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    dialog.dismiss();
                                                    Toast.makeText(context, "Sucessfully insert android", Toast.LENGTH_SHORT).show();
                                                    Register();

                                                  /* if(button.getText().toString().equals("Submit")) {
                                                        Realm realm = null;
                                                        try {
                                                            realm = Realm.getDefaultInstance();
                                                            realm.executeTransaction(new Realm.Transaction() {
                                                                @Override
                                                                public void execute(Realm realm) {
                                                                    Number num = realm.where(DatabaseModel.class).max("id");
                                                                    int nextId = (num == null) ? 1 : num.intValue() + 1;
                                                                    DatabaseModel student = realm.createObject(DatabaseModel.class, nextId);
                                                                    student.setName(tv_client_name.getText().toString().trim());
                                                                    student.setMobile(number);
                                                                    student.setCallstatus("Outgoing");
                                                                    student.setGlobalsatus(globalstatus);
                                                                    student.setDescription(tv_description.getText().toString().trim());
                                                                    student.setDate(tv_date.getText().toString().trim());
                                                                    student.setTime(tv_time.getText().toString().trim());
                                                                    realm.insertOrUpdate(student);
                                                                }
                                                            });
                                                        } finally {
                                                            if (realm != null) {
                                                                realm.close();
                                                            }
                                                        }
                                                    }else {
                                                        Realm realm = null;
                                                        try{
                                                            realm = Realm.getDefaultInstance();
                                                            realm.executeTransaction(new Realm.Transaction() {
                                                                @Override
                                                                public void execute(Realm realm){
                                                                    Number num = realm.where(DatabaseModel.class).max("id");
                                                                    // int nextId = (num == null) ? 1 : num.intValue() + 1;
                                                                    // S//tudentRelam student = realm.createObject(StudentRelam.class, nextId);
                                                                    DatabaseModel employee = realm.where(DatabaseModel.class).equalTo("mobile", number).findFirst();
                                                                    if(employee!=null) {
                                                                        Log.d("mytag", "mobile" + employee);
                                                                        //DatabaseModel student = realm.where(DatabaseModel.class).equalTo("id", item.getId()).findFirst();
                                                                      employee.setName(tv_client_name.getText().toString().trim());
                                                                      employee.setDescription(tv_description.getText().toString().trim());
                                                                      employee.setGlobalsatus(globalstatus);
                                                                      employee.setDate(tv_date.getText().toString().trim());
                                                                      employee.setTime(tv_time.getText().toString().trim());
                                                                      tv_description.setText(employee.getDescription());
                                                                      Log.d("mytag", "mobile" + employee.getMobile());
                                                                      button.setText("Update");
                                                                    }
                                                                }
                                                            });
                                                        } finally {
                                                            if(realm != null) {
                                                                realm.close();
                                                            }
                                                        }
                                                   }*/
                                                }
                                            }
                                        }
                                    });
                                    dialog.show();
                                    break;
                                }
                            }
                        } catch (Exception ex){
                        }
                    }
                }
            }
        }
    };

//    private boolean isOnline(Context context) {
//        try {
//            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getActiveNetworkInfo();
//            //should check null because in airplane mode it will be null
//            return (netInfo != null && netInfo.isConnected());
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }


    private static void gotLocation(Location location) {
        prevLocation = currentLocation == null ? null : new Location(
                currentLocation);
        currentLocation = location;
        if (isLocationNew()){
            OnNewLocationReceived(location);
            Toast.makeText(_context, "new location saved", Toast.LENGTH_SHORT).show();
            stopLocationListener();
        }
    }

    private static boolean isLocationNew() {
        if (currentLocation == null) {
            return false;
        } else if (prevLocation == null) {
            return true;
        } else if (currentLocation.getTime() == prevLocation.getTime()) {
            return false;
        } else {
            return true;
        }
    }

    public static void stopLocationListener() {
        locationManager.removeUpdates(locationListener);
        Toast.makeText(_context, "provider stoped", Toast.LENGTH_SHORT)
                .show();
    }

    // listener ----------------------------------------------------
    static ArrayList<OnNewLocationListener> arrOnNewLocationListener =
            new ArrayList<OnNewLocationListener>();

    // Allows the user to set a OnNewLocationListener outside of this class
    // and react to the event.
    // A sample is provided in ActDocument.java in method: startStopTryGetPoint
    public static void setOnNewLocationListener(
            OnNewLocationListener listener) {
        arrOnNewLocationListener.add(listener);
    }

    public static void clearOnNewLocationListener(
            OnNewLocationListener listener) {
        arrOnNewLocationListener.remove(listener);
    }

    // This function is called after the new point received
    private static void OnNewLocationReceived(Location location) {
        // Check if the Listener was set, otherwise we'll get an Exception
        // when we try to call it

        if (arrOnNewLocationListener != null) {
            // Only trigger the event, when we have any listener
            for (int i = arrOnNewLocationListener.size() - 1; i >= 0; i--) {
                arrOnNewLocationListener.get(i).onNewLocationReceived(
                        location);
            }
        }
    }


    private void Register(){
        //myProgressDialog = MyProgressDialog.show(context, "", "", true, false, null);
        Log.d("mytag", "Api call");
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseLOgon> req = apiInterface.registration("Customer","7778857202","123","shdfsiofhioahdf");
        req.enqueue(new Callback<ResponseLOgon>() {
            @Override
            public void onResponse(Call<ResponseLOgon> call, retrofit2.Response<ResponseLOgon> response) {
                //myProgressDialog.dismiss();
                if(response.isSuccessful()) {
                    if (response.body().getCode().equals("1")) {
                        Log.d("mytag", "Successfully");

                        Toast.makeText(context, "Register Successfully", Toast.LENGTH_SHORT).show();


                    }
                    if (response.body().getCode().equals("2")) {
                        Toast.makeText(context, "Please fill all valid details", Toast.LENGTH_SHORT).show();
                    }

                }else {

                    Log.d("mytag","Response "+ response.message());
                    Log.d("mytag","Response Error "+ response.body());
                    Toast.makeText(context, "Server Down or Network problem"+response.message() , Toast.LENGTH_SHORT).show();

                }
            }
            @Override
            public void onFailure(Call<ResponseLOgon> call, Throwable t){
                t.printStackTrace();
               // myProgressDialog.dismiss();
                Toast.makeText(context, "Server Down or Network problem" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {

        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}
