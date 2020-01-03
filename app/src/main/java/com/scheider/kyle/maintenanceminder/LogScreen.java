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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.scheider.kyle.maintenanceminder.MainScreen.MY_PERMISSION_REQUEST_CAMERA;

public class LogScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String mCurrentPhotoPath;

    boolean isPicLog = false;

    public static final int MY_PERMISSION_REQUEST_READ_EXTERNAL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar.make(view, "Switch to pictures", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
                switch_to_image_log();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        set_log_list_text();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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
        } else if (id == R.id.nav_maintenance_log) {
            // Switch to log_layout
            // setContentView(R.layout.log_layout);
        } else if (id == R.id.nav_fluids) {
            // Switch to fluids activity
            Intent intent = new Intent(this, FluidsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_capture_picture){
            if (ContextCompat.checkSelfPermission(LogScreen.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LogScreen.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSION_REQUEST_CAMERA);
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
            case MY_PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(LogScreen.this, "Sorry, cannot use this feature without permission", Toast.LENGTH_SHORT).show();
                }
                return;

                }
            case MY_PERMISSION_REQUEST_READ_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(LogScreen.this, "Sorry, cannot use this feature without permission", Toast.LENGTH_SHORT).show();
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

    public void switch_to_image_log(){
        if (!isPicLog) {
            isPicLog = true;
            if (check_permission()) {
                // setContentView(R.layout.content_log_screen_pics);
                // String path = "Android/data/com.scheider.kyle.maintenanceminder/files/Pictures";
                String path = "/storage/emulated/0/Android/data/com.scheider.kyle.maintenanceminder/files/Pictures";
                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                if (directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    Log.d("Files", "Size: " + files.length);
                    for (int i = 0; i < files.length; i++) {
                        Log.d("Files", "FileName:" + files[i].getName());
                    }
                    String[] mFileStrings = new String[files.length];

                    for (int i = 0; i < files.length; i++) {
                        mFileStrings[i] = files[i].getAbsolutePath();
                    }

                    ListView list = (ListView) findViewById(R.id.logListView);
                    LazyAdapter adapter = new LazyAdapter(this, mFileStrings);
                    list.setAdapter(adapter);

                    // String[] files = directory.list();

                } else {
                    Toast.makeText(LogScreen.this, "NO SUCH DIRECTORY", Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(LogScreen.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST_READ_EXTERNAL);
            }
        }
        else{
            set_log_list_text();
        }
    }

    public boolean check_permission(){
        if (ContextCompat.checkSelfPermission(LogScreen.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public void set_log_list_text(){
        isPicLog = false;
        DBHandler dbHandler = new DBHandler(this);
        ArrayList<String> logDatabaseList = new ArrayList<>();
        // ArrayList<String> logDisplayList = new ArrayList<>();
        ListView logListView = (ListView) findViewById(R.id.logListView);
        logDatabaseList = dbHandler.read_all_log_info(dbHandler.getReadableDatabase());

        if (logDatabaseList != null) {
            logListView.setAdapter(new ArrayAdapter<>(this,
                    R.layout.list_view_helper, logDatabaseList));
        }
    }
}
