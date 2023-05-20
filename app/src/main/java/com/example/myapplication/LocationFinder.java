package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import net.sqlcipher.database.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.sql.Timestamp;
import java.util.List;


public class LocationFinder extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    protected LocationManager locationManager;

    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SQLiteDatabase.loadLibs(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_page);

        Button loc_but=(Button)findViewById(R.id.LocationButton);
        TextView longitude=(TextView)findViewById(R.id.longitude);
        TextView latitude=(TextView)findViewById(R.id.latitude);
        TextView timestamp=(TextView)findViewById(R.id.timestamp);

        loc_but.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           Location location=getLocationService();
                                           longitude.setText("Longitude: "+location.getLongitude());
                                           latitude.setText("Latitude: "+location.getLatitude());
                                           timestamp.setText("TimeStamp: "+(new Timestamp(System.currentTimeMillis())).toString());
                                       }
                                   }
        );

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                new CountDownTimer(36000000,900000){
                    public void onTick(long millisUtilFinished){getLocationService(); }
                    public void onFinish(){

                    }
                }.start();
            }
        },900000);

    }

    public Location getLocationService() {
        //location
        CovidSymptoms symptomsEntity = CovidSymptomsMainPage.symptoms;
        String ts = (new Timestamp(System.currentTimeMillis())).toString();

        Location myLocation = getLastKnownLocation();
        symptomsEntity.longitude=myLocation.getLongitude();
        symptomsEntity.latitude=myLocation.getLatitude();

        symptomsEntity.timestamp=ts;
        DatabaseHelper db = CovidSymptomsMainPage.databaseHelper;

        boolean isUpdated = db.insertCovidSymptomsToDB(symptomsEntity,LocationFinder.this);
        if(isUpdated) Toast.makeText(LocationFinder.this,"Location updated successfully", Toast.LENGTH_LONG).show();
        else Toast.makeText(LocationFinder.this,"Location update failed", Toast.LENGTH_LONG).show();

        return myLocation;
    }


    //location
    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
