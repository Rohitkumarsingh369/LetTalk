package com.example.upproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class viewPagerAdapter extends FragmentPagerAdapter {


    public viewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        AlphabetsFragment alphabetsFragment=new AlphabetsFragment();
        Bundle bundle=new Bundle();

        position=position+1;
        bundle.putString("key",""+ position);
        alphabetsFragment.setArguments(bundle);
        return alphabetsFragment;
    }

    @Override
    public int getCount() {
        return 10;
    }
}
