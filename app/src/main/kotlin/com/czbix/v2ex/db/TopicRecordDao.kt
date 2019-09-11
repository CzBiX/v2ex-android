package com.czbix.v2ex.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface TopicRecordDao {
    @Query("SELECT * FROM TopicRecord WHERE id = :id")
    suspend fun getRecordById(id: Int): TopicRecord?

    @Query("SELECT lastReadComment FROM TopicRecord WHERE id = :id")
    fun getLastReadComment(id: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateRecord(record: TopicRecord)
}
