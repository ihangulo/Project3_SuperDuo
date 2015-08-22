package barqsoft.footballscores;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       PagerFragment.java
 *
 *       Pager process
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *      Original version Created by yehya khaled on 2/27/2015.
 **/

public class PagerFragment extends Fragment
{
    private static final String LOG_TAG = "PagerFragment:hangulo";

    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    public myPageAdapter mPagerAdapter;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];
    private int ltrRtl = 1; // if LTR mode then 1, RTL mode -1

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

//        Log.v(LOG_TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());

        // Support RTL
        if (Utility.isRtlMode(getActivity()))
            ltrRtl = -1;

        for (int i = 0; i < NUM_PAGES; i++) {
            Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * ltrRtl * Utility.ONEDAY_MS)); // support RTL mode
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); // Locale.ENGLISH --> prohibit other language number problem
            String matchDate = mFormat.format(fragmentDate);

            Bundle args = new Bundle(); // hangulo edited routain
            args.putString("MATCH_DATE", matchDate); // matchDate is locale date, so it must be some action

            viewFragments[i] = new MainScreenFragment(); // get new fragmnet with Bundle data
            viewFragments[i].setArguments(args); // set argument

        }

        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.current_fragment); // set current fragment
//        mPagerHandler.setOffscreenPageLimit(2); // preload
        return rootView;
    }

    // return fragment
    public Fragment getFragmentItem(int i) {
        return mPagerAdapter.getItem(i);
    }


    private class myPageAdapter extends FragmentStatePagerAdapter
    {
        @Override
        public Fragment getItem(int i)
        {
            return viewFragments[i];
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm)
        {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return Utility.getDayNameFromMillis(getActivity(), System.currentTimeMillis() + ((position - 2) * ltrRtl * Utility.ONEDAY_MS)); // supprt RTL mode

        }

    }
}
