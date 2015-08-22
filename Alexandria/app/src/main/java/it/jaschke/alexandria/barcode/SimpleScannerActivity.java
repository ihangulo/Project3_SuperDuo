package it.jaschke.alexandria.barcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.zxing.Result;

import it.jaschke.alexandria.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*
*   ================================================
*        SuperDuo: Alexandria
*        SimpleScannerActivity.java - used by AddBookFragment. porvide internal barcode scanner
*   ================================================
*
*     Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*
*
*    Android Devlelopment Nanodegree Udacity
*    Original version :  https://github.com/dm77/barcodescanner
*    Apache License, Version 2.0
*/

public class SimpleScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    final String LOG_TAG= SimpleScannerActivity.class.getSimpleName();

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setTitle(getString(R.string.title_scan_msg));

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(LOG_TAG, rawResult.getText()); // Prints scan results
        Log.v(LOG_TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

            Intent intent=new Intent();
            intent.putExtra("SCAN_RESULT", rawResult.getText());
            setResult(RESULT_OK, intent);
            finish();
    }

    // when home icon pressed --> act as back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
