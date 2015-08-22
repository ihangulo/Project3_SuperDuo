package it.jaschke.alexandria;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/*
*   ================================================
*        SuperDuo: Alexandria
*        Utility.java
*   ================================================
*
*    Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*
*/

public class Utility {

    // Check internet connection
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public static void showAlertDialog(Context context, String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setTitle(title)
                .setPositiveButton(context.getString(R.string.msg_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
