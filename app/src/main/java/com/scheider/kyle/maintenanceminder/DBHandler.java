package com.scheider.kyle.maintenanceminder;

/**
 * Created by Ychen17 on 10/18/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    //Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "MaintenanceMinder.db";

    // Table names
    private static final String TABLE_CAR = "CarInfo";
    private static final String TABLE_LOG = "MaintenanceLog";

    // CarInfo table column names
    private static final String KEY_MAKE = "make";
    private static final String KEY_MODEL = "model";
    private static final String KEY_YEAR = "year";
    private static final String KEY_MILEAGE = "mileage";
    private static final String KEY_LAST_MILEAGE = "lastMileage";
    private static final String KEY_NEXT_OIL_CHANGE = "nextOilChange";
    private static final String KEY_NEXT_TIRE_ROTATION = "nextTireRotation";
    private static final String KEY_NEXT_TRANS_FLUID_CHANGE = "nextTransFluid";
    private static final String KEY_CAR_ID = "carID";

    // MaintenanceLog table column names
    private static final String KEY_DATE = "date";
    private static final String KEY_MAINTENANCE_TYPE = "type";
    private static final String KEY_MILEAGE_BEFORE = "mileage";
    private static final String KEY_MAINTENANCE_COST = "cost";
    private static final String KEY_CAR_ID_LOG = "carID";

    // Constructor
    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabase();
    }

    // Create initial tables if they aren't present (empty)
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL commands to create tables
        String CREATE_CAR_TABLE = "CREATE TABLE " + TABLE_CAR + "(" + KEY_MAKE + " TEXT, " + KEY_MODEL + " TEXT, " + KEY_YEAR + " TEXT, " + KEY_MILEAGE + " TEXT, " + KEY_LAST_MILEAGE + " TEXT, " + KEY_NEXT_OIL_CHANGE + " TEXT, " + KEY_NEXT_TIRE_ROTATION + " TEXT, " + KEY_NEXT_TRANS_FLUID_CHANGE + " TEXT, " + KEY_CAR_ID + " TEXT" +")";
        String CREATE_LOG_TABLE = "CREATE TABLE " + TABLE_LOG + "(" + KEY_DATE + " TEXT, " + KEY_MAINTENANCE_TYPE + " TEXT, " + KEY_MILEAGE_BEFORE + " TEXT, " + KEY_MAINTENANCE_COST + " TEXT, " + KEY_CAR_ID_LOG + " TEXT" + ")";

        // Execute SQL to create tables
        db.execSQL(CREATE_CAR_TABLE);
        db.execSQL(CREATE_LOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_CAR);
        // Creating tables again
        onCreate(db);
    }

    // Adding Car
    public void addCar(SQLiteDatabase db, String make, String model, String year, String mileage, String lastMileage, String updateOrCreate, String carID) {
        addMake(db, make);
        addModel(db, model);
        addYear(db, year);
        addMileage(db, mileage, updateOrCreate, mileage);
        addLastMileage(db, lastMileage);
        addCarIDInfo(db, carID);
    }

    private void addMake(SQLiteDatabase db, String make){
        ContentValues makeValue = new ContentValues();
        makeValue.put(KEY_MAKE, make);
        db.insert(TABLE_CAR, null, makeValue);
    }

    private void addModel(SQLiteDatabase db, String model){
        ContentValues modelValue = new ContentValues();
        modelValue.put(KEY_MODEL, model);
        db.insert(TABLE_CAR, null, modelValue);
    }

    private void addYear(SQLiteDatabase db, String year){
        ContentValues yearValue = new ContentValues();
        yearValue.put(KEY_YEAR, year);
        db.insert(TABLE_CAR, null, yearValue);
    }

    public void addMileage(SQLiteDatabase db, String newMileage, String updateOrCreate, String oldMileage){
        ContentValues mileageValue = new ContentValues();
        mileageValue.put(KEY_MILEAGE, newMileage);
        if (updateOrCreate.equals("create")) {
            db.insert(TABLE_CAR, null, mileageValue);
        }
        else if (updateOrCreate.equals("update")){
            db.update(TABLE_CAR, mileageValue, KEY_MILEAGE + "=" + oldMileage, null);
        }
    }

    private void addLastMileage(SQLiteDatabase db, String lastMileage){
        ContentValues lastMileageValue = new ContentValues();
        lastMileageValue.put(KEY_LAST_MILEAGE, lastMileage);
        db.insert(TABLE_CAR, null, lastMileageValue);
    }

    private void addCarIDInfo (SQLiteDatabase db, String ID){

    }

    // Set next maintenance intervals
    public void setNextOIlChange(SQLiteDatabase db, String updateOrCreate, String miles, String oldMiles){
        ContentValues nextOilChange = new ContentValues();
        nextOilChange.put(KEY_NEXT_OIL_CHANGE, miles);
        if (updateOrCreate.equals("create")) {
            db.insert(TABLE_CAR, null, nextOilChange);
        }
        else if (updateOrCreate.equals("update")){
            db.update(TABLE_CAR, nextOilChange, KEY_NEXT_OIL_CHANGE + "=" + oldMiles, null);
        }
    }

    public void setNextTireRotation(SQLiteDatabase db, String updateOrCreate, String miles, String oldMiles){
        ContentValues nextTireRotation = new ContentValues();
        nextTireRotation.put(KEY_NEXT_TIRE_ROTATION, miles);
        if (updateOrCreate.equals("create")) {
            db.insert(TABLE_CAR, null, nextTireRotation);
        }
        else if (updateOrCreate.equals("update")){
            db.update(TABLE_CAR, nextTireRotation, KEY_NEXT_TIRE_ROTATION + "=" + oldMiles, null);
        }
    }

    public void setNextTransFluidChange(SQLiteDatabase db, String updateOrCreate, String miles, String oldMiles){
        ContentValues nextTransFluidChange = new ContentValues();
        nextTransFluidChange.put(KEY_NEXT_TRANS_FLUID_CHANGE, miles);
        if (updateOrCreate.equals("create")) {
            db.insert(TABLE_CAR, null, nextTransFluidChange);
        }
        else if (updateOrCreate.equals("update")){
            db.update(TABLE_CAR, nextTransFluidChange, KEY_NEXT_TRANS_FLUID_CHANGE + "=" + oldMiles, null);
        }
    }

    // Get all the info about the car (including maintenance intervals)
    public ArrayList<String> read_all_car_info(SQLiteDatabase db){
        ArrayList<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT * " + "FROM " + TABLE_CAR;
        Log.i("Info:", selectQuery);

        // SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(cursor!=null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getPosition() < cursor.getColumnCount()) {
                        labels.add(cursor.getString(cursor.getPosition()));
                    }
                } while (cursor.moveToNext());
            }
        }

        // closing connection
        cursor.close();
        db.close();

        for (int index = 0; index < labels.size(); index++){
            if (labels.get(index) != null) {
                Log.i("Info:", labels.get(index));
            }
            else{
                Log.i("Info: ", "null");
            }
        }

        // returning lables
        if (labels.size() == 0){
            return null;
        }
        else {
            return labels;
        }
        /*
        0 = make
        1 = model
        2 = year
        3 = mileage
        4 = lastmileage
        5 = oil change
        6 = tire rotation
        7 = trans fluid
         */
    }

    // Write maintenance to the log
    public void write_to_log(SQLiteDatabase db, String date, String maintenance_done, String mileage, String cost){
        write_to_log_date(db, date);
        write_to_log_type(db, maintenance_done);
        write_to_log_mileage(db, mileage);
        write_to_log_cost(db, cost);
        // db.close();
    }

    private void write_to_log_date(SQLiteDatabase db, String date){
        ContentValues logValues = new ContentValues();
        logValues.put(KEY_DATE, date);
        db.insert(TABLE_LOG, null, logValues);
    }

    private void write_to_log_type(SQLiteDatabase db, String maintenance_done){
        ContentValues logValues = new ContentValues();
        logValues.put(KEY_MAINTENANCE_TYPE, maintenance_done);
        db.insert(TABLE_LOG, null, logValues);
    }

    private void write_to_log_mileage(SQLiteDatabase db, String mileage){
        ContentValues logValues = new ContentValues();
        logValues.put(KEY_MILEAGE_BEFORE, mileage);
        db.insert(TABLE_LOG, null, logValues);
    }

    private void write_to_log_cost(SQLiteDatabase db, String cost){
        Log.i("COST", cost);
        ContentValues logValues = new ContentValues();
        if (cost.equals("")){
            cost = "0";
        }
        logValues.put(KEY_MAINTENANCE_COST, cost);
        db.insert(TABLE_LOG, null, logValues);
        Log.i("COST", "COST WRITTEN");
        // read_log_cost(db);
    }

    // Read the entire log. Return null is there is no log
    public ArrayList<String> read_all_log_info(SQLiteDatabase db){
        ArrayList<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT * " + "FROM " + TABLE_LOG;
        Log.i("Info:", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.i("Info: ", String.valueOf(cursor.getCount()));

        String line = "";
        if (cursor != null ) {
            if  (cursor.moveToFirst()) {
                do {
                    boolean add = false;
                    String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                    if (date != null) {
                        date = kill_asterisk(date);
                        Log.i("CURSOR: ", date);
                        line = date;
                        add = false;
                    }
                    else{
                        Log.i("CURSOR: ", "date = null");
                    }
                    String type = cursor.getString(cursor.getColumnIndex(KEY_MAINTENANCE_TYPE));
                    if (type != null) {
                        type = kill_asterisk(type);
                        Log.i("CURSOR: ", type);
                        line = line + "     " + type;
                        add = false;
                    }
                    else{
                        Log.i("CURSOR: ", "type = null");
                    }
                    String miles = cursor.getString(cursor.getColumnIndex(KEY_MILEAGE_BEFORE));
                    if (miles != null) {
                        miles = kill_asterisk(miles);
                        Log.i("CURSOR: ", miles);
                        line = line + " at: " + miles + " miles.";
                        add = false;
                    }
                    else{
                        Log.i("CURSOR: ", "miles = null");
                    }
                    String cost = cursor.getString(cursor.getColumnIndex(KEY_MAINTENANCE_COST));
                    if (cost != null) {
                        cost = kill_asterisk(cost);
                        Log.i("CURSOR: ", cost);
                        line = line + "     $" + cost;
                        add = true;
                    }
                    else{
                        // add = false;
                        Log.i("CURSOR: ", "cost = null");
                    }
                    if (add) {
                        labels.add(line);
                    }
                }while (cursor.moveToNext());
            }
        }
        // closing connection
        cursor.close();
        db.close();

        // returning lables
        if (labels.size() == 0){
            return null;
        }
        else {
            return labels;
        }
    }

    private String kill_asterisk(String str){
        if (str.contains("*")){
            str = str.substring(0, str.indexOf("*"));
        }
        return str;
    }
}
