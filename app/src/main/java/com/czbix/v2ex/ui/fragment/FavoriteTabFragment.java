package com.czbix.v2ex.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.ui.MainActivity;

public class FavoriteTabFragment extends BaseTabFragment {
    public static FavoriteTabFragment newInstance() {
        return new FavoriteTabFragment();
    }

    @Override
    protected FragmentPagerAdapter getAdapter(FragmentManager manager) {
        return new FavoriteFragmentAdapter(manager);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.drawer_favorite);
        activity.setNavSelected(R.id.drawer_favorite);
    }

    private class FavoriteFragmentAdapter extends FragmentPagerAdapter {
        public FavoriteFragmentAdapter(FragmentManager manager) {
            super(manager);
        }

        public Fragment getItem(int position) {
            return position == 0 ? FavNodeFragment.newInstance()
                    : TopicListFragment.Companion.newInstance(Page.PAGE_FAV_TOPIC);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(position == 0 ? R.string.title_fragment_nodes : R.string.title_fragment_topics);
        }
    }
}
