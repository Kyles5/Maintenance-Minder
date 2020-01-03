package com.scheider.kyle.maintenanceminder;

import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by kscheider17 on 3/27/2017.
 */



public class MaintenanceButtonHandler {

    public static int oilChangeInterval;
    public static int tireRotationInterval;
    public static int transFluidInterval;
    int previousTransFluid;
    int nextTransFluid;
    int nextOilChange;
    int previousOilChange;
    int nextTireRotation;
    int previousTireRotation;

    TextView mileageInfoTextView;

    public MaintenanceButtonHandler(TextView mileageInfoTextView){
        this.mileageInfoTextView = mileageInfoTextView;
        oilChangeInterval = 3000;
        tireRotationInterval = 5000;
        transFluidInterval = 50000;
    }

    // Set the new interval for maintenance after the completed buttons are pressed
    // Also called from the log miles button
    public String[] setInfo(String newMileage, String maintenanceDone, DBHandler dbHandler, String cost){
        dbHandler.addMileage(dbHandler.getWritableDatabase(), newMileage, "update", MainScreen.carInfo.get(3));

        String[] output = new String[3];
        if (maintenanceDone.endsWith("*")){
            String myDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            dbHandler.write_to_log(dbHandler.getWritableDatabase(), myDate, maintenanceDone, newMileage, cost);
        }
        switch (maintenanceDone){
            case "oil change*":
                output[0] = "oil";
                previousOilChange = MainScreen.nextOilChange;
                output[1] = String.valueOf(previousOilChange);
                nextOilChange = Integer.parseInt(newMileage) + oilChangeInterval;
                output[2] = String.valueOf(nextOilChange);

                dbHandler.setNextOIlChange(dbHandler.getWritableDatabase(), "update", String.valueOf(nextOilChange), String.valueOf(previousOilChange));

                break;
            case "tire rotation*":
                output[0] = "tire";
                previousTireRotation = MainScreen.nextTireRotation;
                output[1] = String.valueOf(previousTireRotation);
                nextTireRotation = Integer.parseInt(newMileage) + tireRotationInterval;
                output[2] = String.valueOf(nextTireRotation);

                dbHandler.setNextTireRotation(dbHandler.getWritableDatabase(), "update", String.valueOf(nextTireRotation), String.valueOf(previousTireRotation));

                break;
            case "transmission fluid*":
                output[0] = "trans fluid";
                previousTransFluid = FluidsActivity.nextTransFluid;
                output[1] = String.valueOf(previousTransFluid);
                nextTransFluid = Integer.parseInt(newMileage) + transFluidInterval;
                output[2] = String.valueOf(nextTransFluid);

                dbHandler.setNextTransFluidChange(dbHandler.getWritableDatabase(), "update", String.valueOf(nextTransFluid), String.valueOf(previousTransFluid));
                break;
            default:
                output = null;
                break;
        }
        // Update cra info the reflect the new mileage intervals
        MainScreen.carInfo = dbHandler.read_all_car_info(dbHandler.getReadableDatabase());
        mileageInfoTextView.setText("Mileage: " + newMileage);
        return output;
    }
}
