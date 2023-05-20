package com.example.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
//import android.database.sqlite.SQLiteDatabase;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import net.sqlcipher.database.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CovidSymptomsMainPage extends AppCompatActivity {
    public static CovidSymptoms symptoms = new CovidSymptoms();
    public static DatabaseHelper databaseHelper;
    private CovidSymptomsMainPage.MainBroadcastReceiver broadcastReceiver = new CovidSymptomsMainPage.MainBroadcastReceiver();

    private String key_heartRateReading = "HeartRateReading";

    private String key_respiratoryRateReading = "RespiratoryRateReading";
    public static HeartRate heartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SQLiteDatabase.loadLibs(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        //SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getExternalFilesDir(null)+"/covid_symptoms",null);
        databaseHelper.getWritableDatabase(databaseHelper.password);
        databaseHelper.insertCovidSymptomsToDB(symptoms,CovidSymptomsMainPage.this);

        TextView heartRateValue = (TextView)findViewById(R.id.heartRateValue);
        TextView respiratoryRateValue = (TextView) findViewById(R.id.respiratoryRateValue);


        callRespiratoryService();
        callHeartRateService(heartRateValue);

        //uploadtoserver
        Button server_uplo= findViewById(R.id.Databasebutton);
        server_uplo.setOnClickListener((v)-> {
            Intent databaseServer = new Intent(CovidSymptomsMainPage.this, UploadToServer.class);
            startActivity(databaseServer);
        });

        //       upload signs
        Button uploadSignsButton = (Button)findViewById(R.id.uploadSign);
        uploadSignsButton.setOnClickListener((v)-> {
            boolean isUpdated =databaseHelper.getInstance(CovidSymptomsMainPage.this).insertCovidSymptomsToDB(symptoms,CovidSymptomsMainPage.this);
            if(isUpdated) {
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_LONG).show();
                respiratoryRateValue.setText("Respiratory rate :");
                heartRateValue.setText("Heart rate :");
            }
            else Toast.makeText(this,"Data update failed", Toast.LENGTH_LONG).show();
        });

        Button symptomsButton = (Button) findViewById(R.id.Symptoms);
        symptomsButton.setOnClickListener((v)-> {
            Intent symptomsActivity = new Intent(CovidSymptomsMainPage.this, SymptomsActivity.class);
            startActivity(symptomsActivity);
        });

        //Location
        Button locButton=(Button) findViewById(R.id.locationButton);
        locButton.setOnClickListener((v)->{
            Intent locActivity = new Intent(CovidSymptomsMainPage.this, LocationFinder.class);
            startActivity(locActivity);
        });




    }

    private void callRespiratoryService(){
        Button respiratoryRateButton = (Button)findViewById(R.id.respiratoryRate);
        respiratoryRateButton.setOnClickListener((v)-> {
            Intent i = new Intent(CovidSymptomsMainPage.this, RespiratoryRate.class);
            startService(i);
        });
        registerReceiver(broadcastReceiver,new IntentFilter(key_respiratoryRateReading));
    }

    private void callHeartRateService(TextView heartRateValue){

        if (ContextCompat.checkSelfPermission(CovidSymptomsMainPage.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CovidSymptomsMainPage.this,
                    new String[]{Manifest.permission.CAMERA},
                    0 );
        }

        Button heartRateButton = (Button)findViewById(R.id.heartRate);
        TextView heartRateTimer = (TextView)findViewById(R.id.heartRateTimer);
        heartRateButton.setOnClickListener((v)-> {
            heartRate = new HeartRate(CovidSymptomsMainPage.this, heartRateValue, heartRateTimer);
            TextureView cameraTextureView = findViewById(R.id.textureView);
            SurfaceTexture surfaceTexture = cameraTextureView.getSurfaceTexture();

            if((surfaceTexture != null)) {
                Surface surface = new Surface(surfaceTexture);
                if (!CovidSymptomsMainPage.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(CovidSymptomsMainPage.this, "Turn on the flash for accurate measurement!", Toast.LENGTH_LONG).show();
                }
                VideoService videoService = new VideoService(CovidSymptomsMainPage.this);
                videoService.startVideo(surface);
                heartRate.measureRate(cameraTextureView, videoService);
            }
        });
        registerReceiver(broadcastReceiver,new IntentFilter(key_heartRateReading));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Need Camera permissions!", Toast.LENGTH_LONG).show();
            }
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public class MainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(key_respiratoryRateReading)){
                float reading = intent.getFloatExtra("reading",0);
                TextView respiratoryResult = (TextView) findViewById(R.id.respiratoryRateValue);
                respiratoryResult.setText("Respiratory rate :"+ reading);
                symptoms.respiratoryRate = reading;
            }
            else if(intent.getAction().equals(key_heartRateReading)){
                float reading = intent.getFloatExtra("reading",0);
                TextView heartRateResult = (TextView) findViewById(R.id.heartRateValue);
                heartRateResult.setText("Heart rate :"+ reading);
                symptoms.heartRate = reading;
            }
        }
    }
}
