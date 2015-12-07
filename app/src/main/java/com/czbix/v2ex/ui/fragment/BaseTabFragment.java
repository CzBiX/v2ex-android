package com.czbix.v2ex.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.czbix.v2ex.R;

public abstract class BaseTabFragment extends Fragment {
    protected TabLayout mTabLayout;

    public BaseTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_layout, container, false);
        ViewPager viewPager = ((ViewPager) view.findViewById(R.id.view_pager));
        FragmentPagerAdapter adapter = getAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));

        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        ViewCompat.setElevation(mTabLayout, getResources().getDimension(R.dimen.appbar_elevation));
        mTabLayout.setupWithViewPager(viewPager);

        return view;
    }

    protected abstract FragmentPagerAdapter getAdapter(FragmentManager manager);
}
