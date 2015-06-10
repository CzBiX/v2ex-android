package com.czbix.v2ex.ui.loader;

import android.content.Context;

import com.czbix.v2ex.dao.TopicDao;
import com.czbix.v2ex.model.db.ViewHistory;

import java.util.List;

public class ViewHistoryLoader extends AsyncTaskLoader<List<ViewHistory>> {
    public ViewHistoryLoader(Context context) {
        super(context);
    }

    @Override
    public List<ViewHistory> loadInBackgroundWithException() throws Exception {
        return TopicDao.getViewHistory();
    }
}
