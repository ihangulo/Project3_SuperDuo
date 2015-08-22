package it.jaschke.alexandria;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/*
*   ================================================
*        SuperDuo: Alexandria
*       AboutFragment.java : shows information about this app
*   ================================================
*
*     Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*    Android Devlelopment Nanodegree Udacity
*    Original version :  Created by saj
*/

public class AboutFragment extends Fragment {

    public AboutFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.about);
    }

}
/*

 * https://github.com/dm77/barcodescanner
 * Apache License, Version 2.0

 */