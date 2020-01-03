package com.scheider.kyle.maintenanceminder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainScreen extends AppCompatActivity implements UpdateMileageAfterMaintenanceDialogFragment.UpdateMileageListener, NavigationView.OnNavigationItemSelectedListener, LogMilesDialogFragment.UpdateMileageListener {

    DBHandler dbHandler;

    static ArrayList<String> carInfo;

    String car;
    public static String infoLineOne;
    String infoLineTwo;
    String maintenenceDone;
    String mCurrentPhotoPath;
    // String action;

    TextView carInfoTextView;
    TextView mileageInfoTextView;
    TextView mileageSinceTextView;
    TextView oilChangeMileageTextView;
    TextView tireRotationMileageTextView;

    public static int nextOilChange;
    public static int nextTireRotation;
    public static int initialMiles;

    MaintenanceButtonHandler buttonHandler;

    UpdateMileageAfterMaintenanceDialogFragment updateMileageDialog;
    LogMilesDialogFragment logMilesDialogFragment;

    NotificationCompat.Builder mNotificationBuilder;

    public static final int CAMERA_CAPTURE = 1;
    public static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    // public static final int MY_PERMISSION_REQUEST_WRITE_EXTERNAL = 1;
    // public static Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dbHandler = new DBHandler(this);

        updateMileageDialog = new UpdateMileageAfterMaintenanceDialogFragment();
        logMilesDialogFragment = new LogMilesDialogFragment();

        carInfo = dbHandler.read_all_car_info(dbHandler.getReadableDatabase());
        carInfoTextView = (TextView) findViewById(R.id.carInfoTextView);
        mileageInfoTextView = (TextView) findViewById(R.id.mileageInfoTextView);
        mileageSinceTextView = (TextView) findViewById(R.id.mileageSinceTextView);
        oilChangeMileageTextView = (TextView) findViewById(R.id.oilChangeMileageTextView);
        tireRotationMileageTextView = (TextView) findViewById(R.id.tireRotationMileageTextView);

        buttonHandler = new MaintenanceButtonHandler(mileageInfoTextView);

        // Check if its the first run
        if (carInfo.size() < 6){
            initialMiles = Integer.parseInt(carInfo.get(3));
            // Set initial oil change
            nextOilChange = Integer.parseInt(carInfo.get(3)) + MaintenanceButtonHandler.oilChangeInterval;
            dbHandler.setNextOIlChange(dbHandler.getWritableDatabase(), "create", String.valueOf(nextOilChange), String.valueOf(nextOilChange));

            // Set initial tire rotation
            nextTireRotation = Integer.parseInt(carInfo.get(3)) + MaintenanceButtonHandler.tireRotationInterval;
            dbHandler.setNextTireRotation(dbHandler.getWritableDatabase(), "create", String.valueOf(nextTireRotation), String.valueOf(nextTireRotation));
        }
        else{
            nextOilChange = Integer.parseInt(carInfo.get(5));
            nextTireRotation = Integer.parseInt(carInfo.get(6));
        }

        // Set all the initial TextView values
        car = carInfo.get(0) + " " + carInfo.get(1) + " " + carInfo.get(2);
        infoLineOne = "Mileage: " + carInfo.get(3);
        infoLineTwo =  /*"Miles since service: " + carInfo.get(4);*/" ";
        carInfoTextView.setText(car);
        mileageSinceTextView.setText(infoLineTwo);
        mileageInfoTextView.setText(infoLineOne);
        if (Integer.parseInt(carInfo.get(3)) > nextOilChange){
            oilChangeMileageTextView.setText(String.valueOf(nextOilChange) + " Overdue!!");

        }else {
            oilChangeMileageTextView.setText(String.valueOf(nextOilChange));
        }

        if (Integer.parseInt(carInfo.get(3)) > nextTireRotation) {
            tireRotationMileageTextView.setText(String.valueOf(nextTireRotation) + " Overdue!!");

        } else{
            tireRotationMileageTextView.setText(String.valueOf(nextTireRotation));

        }

        maintenenceDone = "";


/*
        // Build Notification to remind you of oil changes
        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("First Notification")
                .setContentText("Information");
        Intent resultIntent = new Intent(this, MainScreen.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainScreen.class);
        taskStackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mNotificationBuilder.build());
*/
        // Intent intent = getIntent();
        // String message = intent.getStringExtra()
    }

    // Oil change button
    public void oil_change_done(View view) {
        maintenenceDone = "oil change*";
        updateMileageDialog.show(getSupportFragmentManager(), "newOilMileage");
    }
    // Tire rotation button
    public void tire_rotation_done(View view) {
        maintenenceDone = "tire rotation*";
        updateMileageDialog.show(getSupportFragmentManager(), "newTireRotationMileage");
    }

    // Called after the dialog is exited from the button
    @Override
    public void updateMileageOnCLick(DialogFragment dialog, String newMileage, String cost) {
        String[] update = buttonHandler.setInfo(newMileage, maintenenceDone, dbHandler, cost);
        // 0 = type
        // 1 = previous
        // 2 = new

        if (update != null) {
            if (update[0].equals("oil")) {
                oilChangeMileageTextView.setText(update[2]);
            } else if (update[0].equals("tire")) {
                tireRotationMileageTextView.setText(update[2]);
            }
        }
    }

    // Log miles button
    public void log_miles(View view){
        maintenenceDone = "";
        logMilesDialogFragment.show(getSupportFragmentManager(), "logMiles");
    }

    // Navigation menu exited from back button
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Create navigation items in navigation menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Three dots menu item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);;
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Navigation Drawer item selected
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_basic_maintenance) {
            // Switch to activity_main_screen_drawer
                    } else if (id == R.id.nav_fluids){
            // Switch to fluids activity
            Intent intent = new Intent(this, FluidsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_maintenance_log) {
            // Switch to Log activity
            if (dbHandler.read_all_log_info(dbHandler.getReadableDatabase()) != null) {
                Intent intent = new Intent(this, LogScreen.class);
                startActivity(intent);
            }
            else{
                Snackbar.make(findViewById(R.id.MainConstraint), "There is no log to display", Snackbar.LENGTH_LONG)
                        .show();
            }
        } else if (id == R.id.nav_capture_picture){
            if (ContextCompat.checkSelfPermission(MainScreen.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainScreen.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSION_REQUEST_CAMERA);

            }
            else{
                takePic();
            }

        }
        else if (id == R.id.nav_tireLife){
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
            case MY_PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePic();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainScreen.this, "Sorry, cannot use this feature without permission", Toast.LENGTH_SHORT).show();
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
                startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK) {
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
}

