package com.developer.alexandru.helpmeradar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GuideActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    // The URL to facebook page
    public static final String FB_URL = "https://www.facebook.com/HelpMeRadar/timeline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the actionbar and it's tabs
        setActionBar();

        //View pager page change listener - change action bar tab
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

    }

    //Click event on facebook address
    public void openFacebook(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(FB_URL));
        startActivity(intent);
    }

    //Click event on twitter address
    public void openTwitter(View view){

    }

    //Click event on email address
    public void openEmail(View view){

    }

    //Click event on "personalisation" text
    public void finish(View v){
        this.finish();
    }

    //Click event on "send suggestions" text
    public void goToContactTab(View view){
        mViewPager.setCurrentItem(2);
    }

    private void setActionBar(){
        //ActionBar tab listener - change page in view pager
        final android.app.ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);

        android.app.ActionBar.TabListener tabListener = new android.app.ActionBar.TabListener() {
            @Override
            public void onTabSelected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        //Tabs
        for (int i = 0; i < 3; i++) {
            String title = null;
            switch (i){
                case 0:
                    title = getString(R.string.tab_title_features);
                    break;
                case 1:
                    title = getString(R.string.tab_title_customize);
                    break;
                case 2:
                    title = getString(R.string.tab_title_contact);
                    break;
            }
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(title)
                            .setTabListener(tabListener));
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final int position = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;
            switch (position){
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_features, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_customize, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_contact_us, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_guide, container, false);
                    ((TextView) rootView.findViewById(R.id.section_label)).setText("Fragment " + position);
            }

            return rootView;
        }
    }

}
