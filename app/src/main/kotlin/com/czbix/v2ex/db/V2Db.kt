package com.czbix.v2ex.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
        entities = [
            TopicRecord::class,
            Comment::class,
            Member::class
        ],
        version = 1
)
abstract class V2Db : RoomDatabase() {
    companion object {
        fun create(context: Context): V2Db {
            val builder = Room.databaseBuilder(context, V2Db::class.java, "v2db.db")
            return builder.build()
        }
    }

    abstract fun topicRecords(): TopicRecordDao

    abstract fun comments(): CommentDao
}