package com.czbix.v2ex.inject

import android.content.Context
import com.czbix.v2ex.db.CommentDao
import com.czbix.v2ex.db.TopicRecordDao
import com.czbix.v2ex.db.V2Db
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class DbModule {
    @Singleton
    @Provides
    fun provideDb(context: Context): V2Db {
        return V2Db.create(context)
    }

    @Provides
    fun provideTopicRecordDao(db: V2Db): TopicRecordDao {
        return db.topicRecords()
    }

    @Provides
    fun provideComments(db: V2Db): CommentDao {
        return db.comments()
    }
}