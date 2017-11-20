package com.example.tejasbhoir.locationappupdated;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    double currentLatitude;
    double currentLongitude;
    DatabaseHelper mydb;
    ArrayList<LatLng> latlongList;
    Button dismissTypeButton;
    TextView coordinates, nameNoType, timeNoType, coordinatesDisplay;
    EditText nameTyped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        latlongList = new ArrayList<>();
        dismissTypeButton = (Button) findViewById(R.id.dismissType);
        nameTyped = (EditText) findViewById(R.id.nameEnter);
        coordinates = (TextView) findViewById(R.id.coordinates);
        nameNoType = (TextView) findViewById(R.id.nameNoType);
        timeNoType = (TextView) findViewById(R.id.timeNoType);
        coordinatesDisplay = (TextView) findViewById(R.id.coordinatesDisplay);
        mydb = new DatabaseHelper(MapsActivity.this);

        Intent intent = new Intent();
        currentLatitude = intent.getDoubleExtra("latitude", 40.5246);
        currentLongitude = intent.getDoubleExtra("longitude", -74.4629);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        final LatLng currentLoc = new LatLng(currentLatitude, currentLongitude);
        LatLng Werblin = new LatLng(0, 0);
        LatLng BSC = new LatLng(0, 0);
        LatLng ARC = new LatLng(0, 0);
        LatLng NicApt = new LatLng(0, 0);
        LatLng Davidson = new LatLng(0, 0);

        String select1 = "SELECT * FROM Check_Ins WHERE ID = 1";
        Cursor rowData = mydb.rowData(select1);
        while (rowData.moveToNext()) {
            Werblin = new LatLng(rowData.getDouble(2), rowData.getDouble(3));
            latlongList.add(0, Werblin);
        }

        String select2 = "SELECT * FROM Check_Ins WHERE ID = 2";
        rowData = mydb.rowData(select2);
        while (rowData.moveToNext()) {
            BSC = new LatLng(rowData.getDouble(2), rowData.getDouble(3));
            latlongList.add(1, BSC);
        }

        String select3 = "SELECT * FROM Check_Ins WHERE ID = 3";
        rowData = mydb.rowData(select3);
        while (rowData.moveToNext()) {
            ARC = new LatLng(rowData.getDouble(2), rowData.getDouble(3));
            latlongList.add(2, ARC);
        }

        String select4 = "SELECT * FROM Check_Ins WHERE ID = 4";
        rowData = mydb.rowData(select4);
        while (rowData.moveToNext()) {
            NicApt = new LatLng(rowData.getDouble(2), rowData.getDouble(3));
            latlongList.add(3, NicApt);
        }

        String select5 = "SELECT * FROM Check_Ins WHERE ID = 5";
        rowData = mydb.rowData(select5);
        while (rowData.moveToNext()) {
            Davidson = new LatLng(rowData.getDouble(2), rowData.getDouble(3));
            latlongList.add(4, Davidson);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.addMarker(new MarkerOptions().position(Werblin)
                .title("Werblin Gym"));
        googleMap.addMarker(new MarkerOptions().position(BSC)
                .title("Busch Student Center"));
        googleMap.addMarker(new MarkerOptions().position(ARC)
                .title("Allison Road Classrooms"));
        googleMap.addMarker(new MarkerOptions().position(NicApt)
                .title("Nichols Apartments"));
        googleMap.addMarker(new MarkerOptions().position(Davidson)
                .title("Davidson Apartments"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(point.latitude, point.longitude)).title("New Marker").draggable(true);

                googleMap.addMarker(marker);

            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                coordinatesDisplay.setText("[" + marker.getPosition().latitude + ", " + marker.getPosition().longitude + "]");
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                final Location locationA = new Location(LocationManager.GPS_PROVIDER);
                locationA.setLatitude(position.latitude);
                locationA.setLongitude(position.longitude);
                boolean proximity = false;

                for (int i = 0; i < 5; i++) {

                    Location locationB = new Location(LocationManager.GPS_PROVIDER);
                    locationB.setLatitude(latlongList.get(i).latitude);
                    locationB.setLongitude(latlongList.get(i).longitude);

                    if (locationA.distanceTo(locationB) < 30) {
                        proximity = true;

                        final LinearLayout popupNoType = (LinearLayout) findViewById(R.id.popupNoType);
                        popupNoType.setVisibility(View.VISIBLE);
                        Button dismissNoTypeButton = (Button) findViewById(R.id.dismissNoType);

                        String select = "select * from Check_Ins where ID = " + (i+1);
                        Cursor rowDataPop = mydb.rowData(select);
                        while (rowDataPop.moveToNext()) {
                            String name = rowDataPop.getString(1);
                            String time = rowDataPop.getString(4);
                            nameNoType.setText(name);
                            timeNoType.setText(time);
                        }

                        dismissNoTypeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupNoType.setVisibility(View.GONE);
                            }
                        });
                        break;
                    }

                }

                if (!proximity) {
                    final LinearLayout popupType = (LinearLayout) findViewById(R.id.popupType);
                    popupType.setVisibility(View.VISIBLE);
                    Button dismissTypeButton = (Button) findViewById(R.id.dismissType);

                        dismissTypeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String coordinatesString = "[" + locationA.getLatitude() + ", " + locationA.getLongitude() + "]";
                                coordinates.setText(coordinatesString);
                                String nameRetrieved = nameTyped.getText().toString();
                                if (nameRetrieved == null) {
                                    Toast.makeText(MapsActivity.this, "Fix Empty name", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    mydb.insertDataLocationName(nameRetrieved);
                                    coordinates.setText("");
                                    popupType.setVisibility(View.GONE);
                                }
                            }
                        });
                }
            }
        });

    }
}

