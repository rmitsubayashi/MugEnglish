package com.linnca.pelicann.userprofile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.GUIUtils;
import com.linnca.pelicann.mainactivity.ToolbarState;

public class UserProfile extends Fragment{
    public static final String TAG = "UserProfile";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Database db;

    private UserProfileListener userProfileListener;

    public interface UserProfileListener {
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            db = (Database)getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            db = new FirebaseDB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        tabLayout = view.findViewById(R.id.user_profile_tab_layout);
        viewPager = view.findViewById(R.id.user_profile_pager);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        userProfileListener.setToolbarState(
                new ToolbarState(getString(R.string.user_profile_app_bar_title),
                        false, false, null)
        );

        populateTabs();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    private void implementListeners(Context context){
        try {
            userProfileListener = (UserProfileListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private void populateTabs(){

        tabLayout.addTab(tabLayout.newTab().setText(R.string.user_profile_tab_hours_studied));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.user_profile_tab_report_card));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorHeight(GUIUtils.getDp(4,getContext()));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        UserProfilePagerAdapter adapter =
                new UserProfilePagerAdapter(getChildFragmentManager(), tabLayout.getTabCount(), db);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }
}
