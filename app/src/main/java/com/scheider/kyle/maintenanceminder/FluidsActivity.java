package com.scheider.kyle.maintenanceminder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FluidsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UpdateMileageAfterMaintenanceDialogFragment.UpdateMileageListener, LogMilesDialogFragment.UpdateMileageListener {

    // Local database handler
    DBHandler dbHandler;

    // Necessary textViews to update the user interface after maintenance
    TextView carInfoTextView;
    TextView mileageInfoTextView;
    TextView mileageSinceTextView;
    TextView transFluidTextView;

    // Store the next mileage value of your trans fluid change
    public static int nextTransFluid;

    // Store what type of maintenance was done
    String maintenenceDone;
    String mCurrentPhotoPath;
    // Value to set new mileage as
    public static String infoLineOne;

    // Update mileage dialog after maintenance is done or miles are logged
    UpdateMileageAfterMaintenanceDialogFragment updateMileageAfterMaintenanceDialogFragment;
    LogMilesDialogFragment logMilesDialogFragment;

    // Handle button clicks
    MaintenanceButtonHandler buttonHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fluids);

        // Set custom app Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Set the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize variables and objects
        dbHandler = new DBHandler(this);
        updateMileageAfterMaintenanceDialogFragment = new UpdateMileageAfterMaintenanceDialogFragment();
        logMilesDialogFragment = new LogMilesDialogFragment();

        maintenenceDone = "";
        infoLineOne = "";

        carInfoTextView = (TextView) findViewById(R.id.carInfoTextView);
        transFluidTextView = (TextView) findViewById(R.id.transmissionFluidMilesTextView);
        mileageInfoTextView = (TextView) findViewById(R.id.mileageInfoTextView);
        mileageSinceTextView = (TextView) findViewById(R.id.mileageSinceTextView);

        buttonHandler = new MaintenanceButtonHandler(mileageInfoTextView);

        String car = MainScreen.carInfo.get(0) + " " + MainScreen.carInfo.get(1) + " " + MainScreen.carInfo.get(2);
        String infoLineOne = "Mileage: " + MainScreen.carInfo.get(3);

        carInfoTextView.setText(car);
        mileageInfoTextView.setText(infoLineOne);
        mileageSinceTextView.setText(" ");

        // Check if this is the first time the activity was opened
        if (MainScreen.carInfo.size() < 8){
            // Set initial trans fluid change
            nextTransFluid = MainScreen.initialMiles + MaintenanceButtonHandler.transFluidInterval;
            dbHandler.setNextTransFluidChange(dbHandler.getWritableDatabase(), "create", String.valueOf(nextTransFluid), String.valueOf(nextTransFluid));
        }
        else{
            nextTransFluid = Integer.parseInt(MainScreen.carInfo.get(7));
        }


        if (Integer.parseInt(MainScreen.carInfo.get(3)) > nextTransFluid){
            transFluidTextView.setText(String.valueOf(nextTransFluid) + " Overdue!!");

        } else {
            transFluidTextView.setText(String.valueOf(nextTransFluid));
        }
    }

    // Close drawer after back button
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Create the action bar menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Three dots menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Navigation Drawer onclicks
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_basic_maintenance) {
            // Switch to activity_main_screen_drawer
            // setContentView(R.layout.activity_main_screen_drawer);
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
        } else if (id == R.id.nav_fluids){
            // Switch to fluids activity
        } else if (id == R.id.nav_maintenance_log) {
            // Switch to log_layout
            // setContentView(R.layout.log_layout);
            if (dbHandler.read_all_log_info(dbHandler.getReadableDatabase()) != null) {
                Intent intent = new Intent(this, LogScreen.class);
                startActivity(intent);
            }
            else{
                Snackbar.make(findViewById(R.id.fluidConstraint), "There is no log to display", Snackbar.LENGTH_LONG)
                        .show();
            }
        }else if (id == R.id.nav_capture_picture){
            if (ContextCompat.checkSelfPermission(FluidsActivity.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FluidsActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MainScreen.MY_PERMISSION_REQUEST_CAMERA);
            }
            else{
                takePic();
            }
        } else if (id == R.id.nav_tireLife){
            // Open tire life activity

        } else if (id == R.id.nav_garage){
            // Open garage activity
            Intent intent = new Intent(this, GarageActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MainScreen.MY_PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(FluidsActivity.this, "Sorry, cannot use this feature without permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void takePic(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.scheider.kyle.maintenanceminder.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, MainScreen.CAMERA_CAPTURE);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainScreen.CAMERA_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            // mImageView.setImageBitmap(imageBitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Transmission Fluid "Completed" Button
    public void trans_fluid_button(View view){
        maintenenceDone = "transmission fluid*";
        updateMileageAfterMaintenanceDialogFragment.show(getSupportFragmentManager(), "newTransMileage");
    }

    // Update mileage dialog has closed from the update button
    @Override
    public void updateMileageOnCLick(DialogFragment dialog, String newMileage, String cost) {
        String[] update = buttonHandler.setInfo(newMileage, maintenenceDone, dbHandler, cost);

        if (update != null) {
            if (update[0].equals("trans fluid")) {
                transFluidTextView.setText(update[2]);
            }
        }
    }

    // Log miles button
    public void log_miles(View view){
        maintenenceDone = "";
        logMilesDialogFragment.show(getSupportFragmentManager(), "logMiles");
    }
}
