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
import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.ui.MainActivity;

import java.util.List;

public class CategoryTabFragment extends Fragment {
    public static CategoryTabFragment newInstance() {
        return new CategoryTabFragment();
    }

    public CategoryTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_layout, container, false);
        ViewPager viewPager = ((ViewPager) view.findViewById(R.id.view_pager));
        FragmentPagerAdapter adapter = new CategoryFragmentAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        ViewCompat.setElevation(tabLayout, getResources().getDimension(R.dimen.appbar_elevation));
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.drawer_explore);
        activity.setNavSelected(R.id.drawer_explore);
    }

    private class CategoryFragmentAdapter extends FragmentPagerAdapter {
        private final List<Tab> mTabs;

        public CategoryFragmentAdapter(FragmentManager manager) {
            super(manager);

            mTabs = PrefStore.getInstance().getTabsToShow();
        }

        public Fragment getItem(int position) {
            return TopicListFragment.newInstance(mTabs.get(position));
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }
    }
}
