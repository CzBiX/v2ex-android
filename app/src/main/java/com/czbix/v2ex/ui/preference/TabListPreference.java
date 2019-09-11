package com.czbix.v2ex.ui.preference;

/*
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
 */
