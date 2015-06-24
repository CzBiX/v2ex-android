package com.czbix.v2ex.ui.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.czbix.v2ex.util.MiscUtils;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;

public class StableArrayAdapter<T> extends ArrayAdapter<T> {

    final int INVALID_ID = -1;

    HashMap<T, Integer> mIdMap = Maps.newHashMap();

    public StableArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        T item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        // see http://stackoverflow.com/questions/26648991/
        return !MiscUtils.HAS_L;
    }
}
