package com.example.hush.Admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapterAdmin extends FragmentStatePagerAdapter {
    int noOfTabs;


    public ViewPagerAdapterAdmin(@NonNull FragmentManager fm, int noOfTabs) {
        super(fm);
        this.noOfTabs = noOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeAdmin();
            case 1:
                return new UsersAdmin();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}