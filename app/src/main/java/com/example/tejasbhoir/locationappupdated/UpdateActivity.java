package com.example.tejasbhoir.locationappupdated;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class UpdateActivity extends AppCompatActivity {

    EditText id, name, latitude, longitude, time, address, idChoice;
    Button updateButton, idNumberButton;
    ArrayList receivedList;
    DatabaseHelper mydb;
    int position;
    boolean outOfBounds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateButton = (Button) findViewById(R.id.sendUpdate);
        idNumberButton = (Button) findViewById(R.id.idButton);
        id = (EditText) findViewById(R.id.id);
        name = (EditText) findViewById(R.id.name);
        latitude = (EditText) findViewById(R.id.latitude);
        longitude = (EditText) findViewById(R.id.longitude);
        time = (EditText) findViewById(R.id.time);
        address = (EditText) findViewById(R.id.address);
        idChoice = (EditText) findViewById(R.id.IDNumber);

        receivedList = new ArrayList();

        Intent intent = new Intent();
        receivedList = intent.getStringArrayListExtra("List");

        mydb = new DatabaseHelper(this);

        setIDNumber();
        update();
    }

    public void setIDNumber() {
        idNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = Integer.parseInt(idChoice.getText().toString());
                Cursor cursor2 = mydb.getAllData();
                if (cursor2.getCount() < position) {
                    outOfBounds = true;
                    Toast.makeText(UpdateActivity.this, "Out of bounds", Toast.LENGTH_SHORT).show();
                }
                else {
                    String select = "SELECT * FROM Check_Ins WHERE ID = " + idChoice.getText().toString();
                    Cursor cursor = mydb.rowData(select);

                    while (cursor.moveToNext()) {
                        id.setText(cursor.getString(0));
                        name.setText(cursor.getString(1));
                        latitude.setText(cursor.getString(2));
                        longitude.setText(cursor.getString(3));
                        time.setText(cursor.getString(4));
                        address.setText(cursor.getString(5));
                    }
                }

            }
        });
    }

    public void update() {
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!outOfBounds) {
                    boolean isUpdated = mydb.updateData(id.getText().toString(), name.getText().toString(), Double.parseDouble(latitude.getText().toString()),
                            Double.parseDouble(longitude.getText().toString()), time.getText().toString(), address.getText().toString());

                    if (isUpdated)
                        Toast.makeText(UpdateActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(UpdateActivity.this, "Data Not Updated", Toast.LENGTH_SHORT).show();

                    String updatedRow = "ID: " + id.getText().toString() + "\n"
                            + "Name: " + name.getText().toString() + "\n"
                            + "Latitude: " + latitude.getText().toString() + "\n"
                            + "Longitude: " + longitude.getText().toString() + "\n"
                            + "Time: " + time.getText().toString() + "\n"
                            + "Address" + address.getText().toString() + "\n\n";

                    receivedList.set(position - 1, updatedRow);
                    Intent intent2 = new Intent(UpdateActivity.this, MainActivity.class);
                    intent2.putStringArrayListExtra("updatedList", receivedList);
                }
                else
                    Toast.makeText(UpdateActivity.this, "Fix out of bounds error", Toast.LENGTH_LONG).show();
            }
        });
    }
}
