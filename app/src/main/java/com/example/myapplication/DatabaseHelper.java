package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="vaka";

    public static DatabaseHelper instance;
    public static String password;

    public static final String TABLE_NAME = "covid_symptoms";
    public static final String RESPIRATORY_RATE = "respiratory_rate";
    public static final String HEART_RATE = "heart_rate";
    public static final String NAUSEA = "nausea";
    public static final String HEADACHE = "headache";
    public static final String DIARRHEA = "diarrhea";
    public static final String SOAR_THROAT = "soar_throat";
    public static final String FEVER = "fever";
    public static final String MUSCLE_ACHE = "muscle_ache";
    public static final String LOSS_OF_SMELL_OR_TASTE = "loss_of_smell_or_taste";
    public static final String COUGH = "cough";
    public static final String SHORTNESS_OF_BREATH = "shortness_of_breath";
    public static final String FEELING_TIRED = "feeling_tired";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE= "longitude";
    public static final String TIME_STAMP = "time_stamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

           createCovidSymptomsDatabase(sqLiteDatabase);
    }

    private void createCovidSymptomsDatabase(SQLiteDatabase sqLiteDatabase) {
         String createSqlTable="create table "+TABLE_NAME+" ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                               RESPIRATORY_RATE+" FLOAT DEFAULT 0,"+
                                               HEART_RATE+" FLOAT DEFAULT 0,"+
                                               NAUSEA+" FLOAT DEFAULT 0,"+
                                               HEADACHE+" FLOAT DEFAULT 0,"+
                                               DIARRHEA+" FLOAT DEFAULT 0,"+
                                               SOAR_THROAT+" FLOAT DEFAULT 0,"+
                                               FEVER+" FLOAT DEFAULT 0,"+
                                               MUSCLE_ACHE+" FLOAT DEFAULT 0,"+
                                               LOSS_OF_SMELL_OR_TASTE+" FLOAT DEFAULT 0,"+
                                               COUGH+" FLOAT DEFAULT 0,"+
                                               SHORTNESS_OF_BREATH+" FLOAT DEFAULT 0,"+
                                               FEELING_TIRED+" FLOAT DEFAULT 0,"+
                                               LATITUDE + " REAL DEFAULT 0,"+
                                               LONGITUDE + " REAL DEFAULT 0,"+
                                               TIME_STAMP + " REAL DEFAULT 0"+")" ;

          sqLiteDatabase.execSQL(createSqlTable);
    }

    static public synchronized DatabaseHelper getInstance(Context context)
    {
        if(instance==null)
            instance=new DatabaseHelper(context);
        return instance;
    }

    public boolean insertCovidSymptomsToDB(CovidSymptoms symptoms,Context context){

        if(instance==null)
            instance=new DatabaseHelper(context);
        SQLiteDatabase database = instance.getWritableDatabase(password);

        ContentValues contentValues = new ContentValues();

        contentValues.put(RESPIRATORY_RATE,symptoms.respiratoryRate);
        contentValues.put(HEART_RATE,symptoms.heartRate);
        contentValues.put(COUGH,symptoms.cough);
        contentValues.put(DIARRHEA,symptoms.diarrhea);
        contentValues.put(FEELING_TIRED,symptoms.feelingTired);
        contentValues.put(FEVER,symptoms.fever);
        contentValues.put(HEADACHE,symptoms.headache);
        contentValues.put(LOSS_OF_SMELL_OR_TASTE,symptoms.lossOfSmellOrTaste);
        contentValues.put(MUSCLE_ACHE,symptoms.muscleAche);
        contentValues.put(NAUSEA,symptoms.nausea);
        contentValues.put(SHORTNESS_OF_BREATH,symptoms.shortnessOfBreath);
        contentValues.put(SOAR_THROAT,symptoms.soarThroat);
        contentValues.put(LATITUDE , symptoms.latitude);
        contentValues.put(LONGITUDE , symptoms.longitude);
        contentValues.put(TIME_STAMP , symptoms.timestamp);

        long result = database.insert(TABLE_NAME , null, contentValues);
        database.close();
        if(result == -1) return false;
        return true;
    }

    public boolean updateCovidSymptomsIntoDb(CovidSymptoms symptoms){

        SQLiteDatabase database = this.getWritableDatabase(password);

        ContentValues values = new ContentValues();
        values.put(RESPIRATORY_RATE, symptoms.respiratoryRate);
        values.put(HEART_RATE , symptoms.heartRate);
        values.put(NAUSEA , symptoms.nausea);
        values.put(HEADACHE , symptoms.headache);
        values.put(DIARRHEA , symptoms.diarrhea);
        values.put(SOAR_THROAT , symptoms.soarThroat);
        values.put(FEVER , symptoms.fever);
        values.put(MUSCLE_ACHE , symptoms.muscleAche);
        values.put(LOSS_OF_SMELL_OR_TASTE , symptoms.lossOfSmellOrTaste);
        values.put(COUGH , symptoms.cough);
        values.put(SHORTNESS_OF_BREATH , symptoms.shortnessOfBreath);
        values.put(FEELING_TIRED , symptoms.feelingTired);
        values.put(LONGITUDE,symptoms.longitude);
        values.put(LATITUDE,symptoms.latitude);
        values.put(TIME_STAMP,symptoms.timestamp);
        Cursor cursor = database.rawQuery("Select * from "+TABLE_NAME+ " where _id=?", new String[] {String.valueOf(1)});
        if(cursor.getCount() > 0) {
            long result = database.update(TABLE_NAME, values, "_id=?", new String[]{String.valueOf(1)});
            if(result ==-1){
                cursor.close();
                database.close();
                return false;
            }
            else{
                cursor.close();
                database.close();
                return true;
            }
        }
        cursor.close();
        database.close();
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
