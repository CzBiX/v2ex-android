package com.czbix.v2ex.ui.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;

import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.ui.adapter.StableArrayAdapter;
import com.czbix.v2ex.ui.widget.DragSortListView;

import java.util.List;

public class TabListPreference extends DialogPreference {
    public List<Tab> mTabsToShow;

    public TabListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mTabsToShow = PrefStore.getInstance().getTabsToShow();
        } else {
            final String string = (String) defaultValue;
            mTabsToShow = Tab.getTabsToShow(string);
        }
    }

    @Override
    protected View onCreateDialogView() {
        return new DragSortListView(getContext());
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        if (mTabsToShow == null) {
            onSetInitialValue(true, null);
        }

        final ArrayAdapter<Tab> adapter = new StableArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, mTabsToShow);
        final DragSortListView listView = (DragSortListView) view;
        listView.setDataList(mTabsToShow);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistString(Tab.getStringToSave(mTabsToShow));
        }
        mTabsToShow = null;
    }
}
