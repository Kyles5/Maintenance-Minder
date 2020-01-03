package com.scheider.kyle.maintenanceminder;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InitialActivity extends AppCompatActivity {

    String make;
    String model;
    String year;
    String mileage;
    String milesSince;
    String selectedMake;

    int carID = 0;

    Spinner makeSpinner;
    Spinner modelSpinner;
    Spinner yearSpinner;

    EditText mileageEditText;
    EditText milesSinceEditText;

    DBHandler dbHandler;

    ResultSet cars;

    public static final int MY_PERMISSION_REQUEST_INTERNET = 0;
    public static final int MY_PERMISSION_REQUEST_NETWORK_STATE = 1;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> carsList;
    ArrayList<String> carMakes;
    ArrayList<String> carModels;

    // url to get all carsJSON list
    private static String url_get_make_school = "http://10.0.1.223/db_readMake.php";
    private static String url_get_model_school = "http://10.0.1.223/db_readModel.php";
    private static String url_get_make_home = "http://172.21.186.96/db_readMake.php";
    private static String url_get_model_home = "http://172.21.186.96/db_readModel.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID = "cars";
    private static final String TAG_MAKE = "Make";
    private static final String TAG_MODEL = "Model";
    private static final String TAG_MPG = "MPG";
    private static final String TAG_WEIGHT = "Weight";

    // carsJSON JSONArray
    JSONArray carsJSON = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dbHandler = new DBHandler(this);

        // Hashmap for ListView
        carsList = new ArrayList<HashMap<String, String>>();
        carMakes = new ArrayList<>();
        carModels = new ArrayList<>();

        // Loading carsJSON in Background Thread


        if (dbHandler.read_all_car_info(dbHandler.getReadableDatabase()) == null) {
            new LoadAllMakes().execute();
            makeSpinner = (Spinner) findViewById(R.id.makeSpinner);
            modelSpinner = (Spinner) findViewById(R.id.modelSpinner);
            yearSpinner = (Spinner) findViewById(R.id.yearSpinner);

            if (ContextCompat.checkSelfPermission(InitialActivity.this,
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InitialActivity.this,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSION_REQUEST_INTERNET);
            }else{

                set_initial_spinner_values();
            }

            mileageEditText = (EditText) findViewById(R.id.mileageEditText);
            milesSinceEditText = (EditText) findViewById(R.id.milesSinceEditText);

            modelSpinner.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    make = String.valueOf(makeSpinner.getSelectedItem());
                    selectedMake = make;
                    set_make_spinner_values();
                    return false;
                }
            });
        }
        else{
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
            this.finish();
        }
    }

    public void set_initial_values(View view){
        // Make sure all fields are complete
        // This is buggy, I don't know why
        if (makeSpinner.getSelectedItem() != null
                && String.valueOf(makeSpinner.getSelectedItem()) != "Please select a make..."
                && modelSpinner.getSelectedItem() != null
                && String.valueOf(modelSpinner.getSelectedItem()) != "Please select a model..."
                && yearSpinner.getSelectedItem() != null
                && String.valueOf(yearSpinner.getSelectedItem()) != "Please select a year..."
                && mileageEditText.getText() != null
                && String.valueOf(mileageEditText.getText()) != "") {
            make = String.valueOf(makeSpinner.getSelectedItem());
            model = String.valueOf(modelSpinner.getSelectedItem());
            year = String.valueOf(yearSpinner.getSelectedItem());
            mileage = String.valueOf(mileageEditText.getText());
            milesSince = String.valueOf(milesSinceEditText.getText()); // If there is no value, recommend immediate oil change
            dbHandler.addCar(dbHandler.getWritableDatabase(), make, model, year, mileage, "0", "create", String.valueOf(carID));

            // Start Basic Maintenance Activity
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
            this.finish();
        }
        else{
            Snackbar.make(findViewById(R.id.initial_CoordinatorLayout), "Please fill out all of the required fields.", Snackbar.LENGTH_LONG)
                    .show();
            Log.i("Info:", String.valueOf(mileageEditText.getText()));
        }
    }

    // Set all the values of the car makes and car years
    // Eventually link to database
    public void set_initial_spinner_values(){
        // Lists
        ArrayList<String> makeLabels = set_make_labels();
        ArrayList<String> yearLabels = set_year_labels();

        // Adapters
        ArrayAdapter<String> makeDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, makeLabels);
        ArrayAdapter<String> yearDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearLabels);

        makeDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        makeSpinner.setAdapter(makeDataAdapter);
        yearSpinner.setAdapter(yearDataAdapter);
    }

    public void set_make_spinner_values(){
        ArrayList<String> modelLabels = set_model_labels(make);
        ArrayAdapter<String> modelDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modelLabels);
        modelDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(modelDataAdapter);
    }

    public ArrayList<String> set_make_labels(){
        ArrayList<String> labels = carMakes;
        labels.add(0, "Please select a make...");
        return labels;
    }

    public ArrayList<String> set_model_labels(String make){
        if (selectedMake != "" && selectedMake != null) {
            new LoadAllModels().execute();

            ArrayList<String> labels = carModels;

            labels.add(0, "Please select a model...");
            Log.i("Query", "QUERY PERFORMED");
            return labels;
        }
        else{
            ArrayList<String> labels = new ArrayList<>();
            // labels.add("Please select a model...");
            Log.i("Query", "Query NOT performed");
            return labels;
        }



    }

    public ArrayList<String> set_year_labels(){
        ArrayList<String> labels = new ArrayList<>();
        labels.add("Please select a year...");
        for (int year = 1980; year < 2018; year++){
            labels.add(String.valueOf(year));
        }
        return labels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(InitialActivity.this, "Sorry, cannot use app without this permission", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                return;

            }
            case MY_PERMISSION_REQUEST_NETWORK_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(InitialActivity.this, "Sorry, cannot use app without this permission", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                return;

            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    class LoadAllMakes extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InitialActivity.this);
            pDialog.setMessage("Loading carsJSON. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            // pDialog.show();
        }

        /**
         * getting All carsJSON from url
         * */
        protected String doInBackground(String... args) {
            Log.i("LOADMAKES", "STARTED");
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_get_make_home, "GET", params);
            Log.i("JSON", "DONE");
            // Check your log cat for JSON reponse
            Log.i("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // carsJSON found
                    // Getting Array of Products
                    carsJSON = json.getJSONArray(TAG_ID);

                    // looping through All Products
                    for (int i = 0; i < carsJSON.length(); i++) {
                        JSONObject c = carsJSON.getJSONObject(i);

                        // Storing each json item in variable
                        String make = c.getString(TAG_MAKE);
                        // String model = c.getString(TAG_MODEL);
                        // String mpg = c.getString(TAG_MPG);
                        // String weight = c.getString(TAG_WEIGHT);

                        // creating new HashMap
                        // HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        // map.put(TAG_MAKE, make);
                        // map.put(TAG_MODEL, model);
                        // map.put(TAG_MPG, mpg);
                        // map.put(TAG_WEIGHT, weight);

                        // adding HashList to ArrayList
                        // carsList.add(map);
                        make = make.trim();
                        if (!(carMakes.contains(make))) {
                            carMakes.add(make);
                        }
                        for (int index = 0; index < carMakes.size(); index++){
                            Log.i("CARMAKES", carMakes.get(index));
                        }
                    }
                } else {
                    // no carsJSON found
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all carsJSON
            // pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                }
            });

        }

    }

    class LoadAllModels extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InitialActivity.this);
            pDialog.setMessage("Loading product details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            // pDialog.show();
        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("make", selectedMake));

                        // getting product details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jParser.makeHttpRequest(
                                url_get_model_home, "GET", params);

                        // check your log for json response
                        Log.d("Single Product Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            carsJSON = json.getJSONArray(TAG_ID);

                            for (int i = 0; i < carsJSON.length(); i++) {
                                // successfully received product details
                                JSONArray productObj = json
                                        .getJSONArray(TAG_ID); // JSON Array

                                // get first product object from JSON Array
                                JSONObject product = productObj.getJSONObject(i);
                                String modelJSON = product.getString(TAG_MODEL);
                                if (!(carModels.contains(modelJSON))) {
                                    carModels.add(modelJSON);
                                }
                                // product with this pid found
                            }


                        }else{
                            // product with pid not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }
    }

}



