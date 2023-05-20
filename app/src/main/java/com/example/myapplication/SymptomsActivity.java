package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class SymptomsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final HashMap<String, Float> covidSymptomsMap = new HashMap<String, Float>();
    private RatingBar ratingBar;
    private int currentSpinnerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.covid_symptoms_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean isFromUser) {
                String symptom = (String) spinner.getItemAtPosition(currentSpinnerPosition);
                covidSymptomsMap.put(symptom,rating);
            }});
    }


    public void uploadSymptomsIntoDB(View v) {
        CovidSymptoms symptomsEntity = CovidSymptomsMainPage.symptoms;
        symptomsEntity.nausea = covidSymptomsMap.getOrDefault("Nausea", 0f);
        symptomsEntity.headache = covidSymptomsMap.getOrDefault("Headache",0f);
        symptomsEntity.diarrhea = covidSymptomsMap.getOrDefault("Diarrhea", 0f);
        symptomsEntity.soarThroat = covidSymptomsMap.getOrDefault("Soar Throat", 0f);
        symptomsEntity.fever = covidSymptomsMap.getOrDefault("Fever", 0f);
        symptomsEntity.muscleAche = covidSymptomsMap.getOrDefault("Muscle Ache", 0f);
        symptomsEntity.lossOfSmellOrTaste = covidSymptomsMap.getOrDefault("Loss of Smell or Taste", 0f);
        symptomsEntity.cough = covidSymptomsMap.getOrDefault("Cough", 0f);
        symptomsEntity.shortnessOfBreath = covidSymptomsMap.getOrDefault("Shortness of Breath", 0f);
        symptomsEntity.feelingTired = covidSymptomsMap.getOrDefault("Feeling tired", 0f);
        DatabaseHelper database = CovidSymptomsMainPage.databaseHelper;
        boolean isUpdated = database.updateCovidSymptomsIntoDb(symptomsEntity);
        if(isUpdated) Toast.makeText(SymptomsActivity.this,"Data updated successfully", Toast.LENGTH_LONG).show();
        else Toast.makeText(SymptomsActivity.this,"Data update failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentSpinnerPosition = i;
        String item = (String) adapterView.getItemAtPosition(i);
        Float rating= covidSymptomsMap.get(item);
        if(rating !=null) {
            ratingBar.setRating(rating);
        } else ratingBar.setRating(0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
class CovidSymptoms {
    public float respiratoryRate;
    public float heartRate;
    public float nausea;
    public float headache;
    public float diarrhea;
    public float soarThroat;
    public float fever;
    public float muscleAche;
    public float lossOfSmellOrTaste;
    public float cough;
    public float shortnessOfBreath;
    public float feelingTired;
    public double latitude;
    public double longitude;
    public String timestamp;

    CovidSymptoms() {
        respiratoryRate = 0;
        heartRate = 0;
        nausea= 0;
        headache=0;
        diarrhea=0;
        soarThroat=0;
        fever=0;
        muscleAche=0;
        lossOfSmellOrTaste=0;
        cough=0;
        shortnessOfBreath=0;
        feelingTired=0;
        latitude=0;
        longitude=0;
        timestamp="0";
    }
}
