package com.scheider.kyle.maintenanceminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static java.security.AccessController.getContext;

/**
 * Created by kyles on 6/16/2017.
 */

public class LogMilesDialogFragment extends DialogFragment {
    String newMileage;
    String cost;
    // String action;
    // MainScreen mainScreen;

    EditText newMileageEditText;
    EditText costEditText;

    boolean validMileage;

    public interface UpdateMileageListener{
        void updateMileageOnCLick(DialogFragment dialog, String newMileage, String cost);
    }

    UpdateMileageListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        validMileage = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // mainScreen = new MainScreen();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View myView = inflater.inflate(R.layout.log_miles_dialog, null);
        newMileageEditText = (EditText) myView.findViewById(R.id.newMileageEditText);
        // costEditText = (EditText) myView.findViewById(R.id.costEditText);
        builder.setView(myView)
                .setMessage("New Current Mileage")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // newMileage = String.valueOf(newMileageEditText.getText());
                        if (mListener != null) {
                            newMileage = String.valueOf(newMileageEditText.getText());
                            cost = "0";
                            if (newMileage.equals("")|| Integer.parseInt(newMileage) < Integer.parseInt(MainScreen.carInfo.get(3))){
                                Toast invalidMiles = Toast.makeText(getContext(), "Invalid Miles", Toast.LENGTH_LONG);
                                invalidMiles.show();
                            }
                            else{
                                validMileage = true;
                            }
                        }
                        if (validMileage) {
                            mListener.updateMileageOnCLick(LogMilesDialogFragment.this, newMileage, cost);
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (UpdateMileageListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
