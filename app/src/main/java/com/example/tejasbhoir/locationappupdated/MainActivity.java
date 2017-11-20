package com.example.tejasbhoir.locationappupdated;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final String TAG = "GPS";
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    DatabaseHelper mydb;

    GoogleApiClient gac;
    LocationRequest locationRequest;
    TextView tvLatitude, tvLongitude, tvTime, addressEdit;
    EditText userLocationName;
    ListView checkInList;
    ArrayAdapter adapter;
    ArrayList<String> listData;
    Button button;
    Button deleteButton;
    Button updateData;
    Button mapsButton;
    String mAddressOutput;
    Location location, prevLocation;
    long prevTime, currenTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLatitude = (TextView) findViewById(R.id.latitude);
        tvLongitude = (TextView) findViewById(R.id.longitude);
        tvTime = (TextView) findViewById(R.id.time);
        addressEdit = (TextView) findViewById(R.id.address);
        button = (Button) findViewById(R.id.checkIn);
        updateData = (Button) findViewById(R.id.updateButton);
        deleteButton = (Button) findViewById(R.id.deleteAll);
        mapsButton = (Button) findViewById(R.id.mapsButton);
        userLocationName = (EditText) findViewById(R.id.locationName);
        checkInList = (ListView) findViewById(R.id.checkInList);
        listData = new ArrayList<>();
        mydb = new DatabaseHelper(this);
        prevLocation = null;

        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listData);
        checkInList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Intent intent2 = new Intent();
        if (intent2.getStringArrayListExtra("updatedList") != null) {
            listData = intent2.getStringArrayListExtra("updatedList");
        }
        else {
            Cursor result = mydb.getAllData();
            StringBuilder stringBuffer = new StringBuilder();
            while(result.moveToNext()) {
                stringBuffer.append("ID: " + result.getString(0) + "\n");
                stringBuffer.append("Name: " + result.getString(1) + "\n");
                stringBuffer.append("Latitude: " + result.getString(2) + "\n");
                stringBuffer.append("Longitude: " + result.getString(3) + "\n");
                stringBuffer.append("Time: " + result.getString(4) + "\n");
                stringBuffer.append("Address: " + result.getString(5) + "\n\n");
                listData.add(stringBuffer.toString());
                checkInList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                stringBuffer = new StringBuilder();
            }
        }

        checkIn();
        deleteAllEntries();
        updateData();
        showMap();

        isGooglePlayServicesAvailable();

        if (!isLocationEnabled())
            showAlert();

        locationRequest = new LocationRequest();
        long UPDATE_INTERVAL = 2 * 1000;
        locationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 2000;
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gac = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        gac.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        gac.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            updateUI(location);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        Log.d(TAG, "onConnected");

        Location ll = LocationServices.FusedLocationApi.getLastLocation(gac);
        Log.d(TAG, "LastLocation: " + (ll == null ? "NO LastLocation" : ll.toString()));

        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission was granted!", Toast.LENGTH_LONG).show();

                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                gac, locationRequest, this);
                    } catch (SecurityException e) {
                        Toast.makeText(MainActivity.this, "SecurityException:\n" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        Log.d("DDD", connectionResult.toString());
    }

    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");
        if (location != null) {
            prevLocation = location;
            prevTime = location.getTime();
        }
        location = loc;
        currenTime = location.getTime();
        tvLatitude.setText("Latitude: " + Double.toString(loc.getLatitude()));
        tvLongitude.setText("Longitude: " + Double.toString(loc.getLongitude()));
        tvTime.setText("Time: " + DateFormat.getTimeInstance().format(loc.getTime()));
        mAddressOutput = getCompleteAddressString(loc.getLatitude(), loc.getLongitude());
        addressEdit.setText(mAddressOutput);

        if ((prevLocation != null && prevLocation.distanceTo(location) > 100) || (prevLocation!=null && ((currenTime-prevTime) > 300000))) {
            listData.clear();
                mydb.insertDataCheckIn("AutoCheckIn_100m", location.getLatitude(), location.getLongitude(),
                        DateFormat.getTimeInstance().format(location.getTime()), mAddressOutput);

                Cursor result = mydb.getAllData();
                if (result.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "No check in items", LENGTH_SHORT).show();
                }
                else {
                    StringBuilder stringBuffer = new StringBuilder();
                    while(result.moveToNext()) {
                        stringBuffer.append("ID: " + result.getString(0) + "\n");
                        stringBuffer.append("Name: " + result.getString(1) + "\n");
                        stringBuffer.append("Latitude: " + result.getString(2) + "\n");
                        stringBuffer.append("Longitude: " + result.getString(3) + "\n");
                        stringBuffer.append("Time: " + result.getString(4) + "\n");
                        stringBuffer.append("Address: " + result.getString(5) + "\n\n");
                        listData.add(stringBuffer.toString());
                        checkInList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        stringBuffer = new StringBuilder();
                    }
                }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGooglePlayServicesAvailable() {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.d(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        Log.d(TAG, "This device is supported.");
        return true;
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Current location", strReturnedAddress.toString());
            } else {
                Log.w("Current location", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Current location", "Cannot get Address!");
        }
        return strAdd;
    }

    public void checkIn () {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listData.clear();
                checkInList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (userLocationName == null) {
                    Cursor result = mydb.getAllData();
                    boolean proximity = false;
                    if (result.getCount() == 0) {
                        mydb.insertDataCheckIn("", location.getLatitude(), location.getLongitude(),
                                DateFormat.getTimeInstance().format(location.getTime()), mAddressOutput);
                    }
                    else {
                        while(result.moveToNext()) {
                            Location A = new Location("point A");
                            A.setLatitude(result.getDouble(2));
                            A.setLongitude(result.getDouble(3));

                            float distance = A.distanceTo(location);
                            if (distance < 30) {
                                mydb.insertDataCheckIn(result.getString(1), location.getLatitude(), location.getLongitude(),
                                        DateFormat.getTimeInstance().format(location.getTime()), result.getString(5));
                                proximity = true;
                                break;
                            }
                        }
                        if (!proximity) {
                            mydb.insertDataCheckIn("", location.getLatitude(), location.getLongitude(),
                                    DateFormat.getTimeInstance().format(location.getTime()), mAddressOutput);
                        }

                        Cursor cursor2 = mydb.getAllData();
                        StringBuilder stringBuffer = new StringBuilder();
                        if (result.getCount() == 0) {
                            Toast.makeText(MainActivity.this, "No check in items", LENGTH_SHORT).show();
                        }
                        else {
                            while (cursor2.moveToNext()) {
                                stringBuffer.append("ID: " + result.getString(0) + "\n");
                                stringBuffer.append("Name: " + result.getString(1) + "\n");
                                stringBuffer.append("Latitude: " + result.getString(2) + "\n");
                                stringBuffer.append("Longitude: " + result.getString(3) + "\n");
                                stringBuffer.append("Time: " + result.getString(4) + "\n");
                                stringBuffer.append("Address: " + result.getString(5) + "\n\n");
                                listData.add(stringBuffer.toString());
                                checkInList.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                stringBuffer = new StringBuilder();
                            }
                        }
                    }
                }
                else {
                    Cursor size = mydb.getAllData();
                    boolean proximity = false;
                    if (size.getCount() == 0) {
                        mydb.insertDataCheckIn(userLocationName.getText().toString(), location.getLatitude(), location.getLongitude(),
                                DateFormat.getTimeInstance().format(location.getTime()), mAddressOutput);
                    }
                    else {
                        while(size.moveToNext()) {
                            Location A = new Location("point A");
                            A.setLatitude(size.getDouble(2));
                            A.setLongitude(size.getDouble(3));

                            float distance = A.distanceTo(location);
                            if (distance < 30) {
                                mydb.insertDataCheckIn(size.getString(1), location.getLatitude(), location.getLongitude(),
                                        DateFormat.getTimeInstance().format(location.getTime()), size.getString(5));
                                proximity = true;
                                break;
                            }
                        }
                        if (!proximity) {
                            mydb.insertDataCheckIn(userLocationName.getText().toString(), location.getLatitude(), location.getLongitude(),
                                    DateFormat.getTimeInstance().format(location.getTime()), mAddressOutput);
                        }
                    }

                    Cursor size2 = mydb.getAllData();
                    boolean boolCheck = mydb.isPresent(userLocationName.getText().toString());
                    if(!boolCheck) {
                        while(size2.moveToNext()) {
                            Location A = new Location("point A");
                            A.setLatitude(size2.getDouble(2));
                            A.setLongitude(size2.getDouble(3));

                            float distance = A.distanceTo(location);
                            if (distance < 30) {
                                proximity = true;
                                break;
                            }
                        }
                        if (!proximity) {
                            mydb.insertDataLocationName(userLocationName.getText().toString());
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Location already in database", Toast.LENGTH_LONG).show();
                    }

                    Cursor result = mydb.getAllData();
                    if (result.getCount() == 0) {
                        Toast.makeText(MainActivity.this, "No check in items", LENGTH_SHORT).show();
                    }
                    else {
                        StringBuilder stringBuffer = new StringBuilder();
                        while(result.moveToNext()) {
                            stringBuffer.append("ID: " + result.getString(0) + "\n");
                            stringBuffer.append("Name: " + result.getString(1) + "\n");
                            stringBuffer.append("Latitude: " + result.getString(2) + "\n");
                            stringBuffer.append("Longitude: " + result.getString(3) + "\n");
                            stringBuffer.append("Time: " + result.getString(4) + "\n");
                            stringBuffer.append("Address: " + result.getString(5) + "\n\n");
                            listData.add(stringBuffer.toString());
                            checkInList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            stringBuffer = new StringBuilder();
                        }
                    }
                }

                userLocationName.getText().clear();
                userLocationName.setSelectAllOnFocus(true);
                userLocationName.requestFocus();
            }
        });
    }

    public void deleteAllEntries() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mydb.deleteAllData();
                listData.clear();
                checkInList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "All Entries Deleted", LENGTH_SHORT).show();
            }
        });
    }

    public void updateData() {
        updateData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                intent.putStringArrayListExtra("List", listData);
                startActivity(intent);
            }
        });
    }

    public void showMap() {
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (location != null) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("latitude", location.getLatitude());
                    intent.putExtra("longitude", location.getLongitude());
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_LONG).show();
            }
        });
    }
}