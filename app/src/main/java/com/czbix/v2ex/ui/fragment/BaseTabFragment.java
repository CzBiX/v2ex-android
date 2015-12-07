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
import com.google.common.base.Preconditions;

public abstract class BaseTabFragment extends Fragment {
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

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        ViewCompat.setElevation(tabLayout, getResources().getDimension(R.dimen.appbar_elevation));
        tabLayout.setupWithViewPager(viewPager);

        // XXX: TabLayout support tabContentStart, but no tabContentEnd, so set the padding manually
        final View tabStrip = tabLayout.getChildAt(0);
        Preconditions.checkNotNull(tabStrip, "tabStrip shouldn't be null");
        final int padding = getResources().getDimensionPixelSize(R.dimen.tab_layout_padding);
        ViewCompat.setPaddingRelative(tabStrip, padding, 0, padding, 0);

        return view;
    }

    protected abstract FragmentPagerAdapter getAdapter(FragmentManager manager);
}
