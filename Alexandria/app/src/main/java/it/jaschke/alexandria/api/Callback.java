package it.jaschke.alexandria.api;

/*
*   ================================================
*        SuperDuo: Alexandria
*       Callback.java - used by MainActivity & BookDetailFragment
*   ================================================
*
*     Kwanghyun JUNG   ihangulo@gmail.com  2015.AUG.
*
*
*    Android Devlelopment Nanodegree Udacity
*
*    Original version :  Created by saj on Created by saj on 25/01/15..
*/

public interface Callback {
    public void onListItemSelected(String ean);
    public void onListItemUpdated();
}
