package com.example.hush.Admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.hush.R;
import com.google.android.material.tabs.TabLayout;

public class AdminMainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapterAdmin adapterAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        init();
        addTabs();
    }

    private void init() {
        tabLayout = findViewById(R.id.tabLayout_admin);
        viewPager = findViewById(R.id.viewPager_admin);
    }

    private void addTabs() {


        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home_admin));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_users_admin));

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        adapterAdmin = new ViewPagerAdapterAdmin(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapterAdmin);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_admin_fill);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_admin_fill);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_users_admin_fill);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_admin);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_users_admin);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_admin_fill);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_users_admin_fill);
                        break;
                }
            }
        });


    }
}