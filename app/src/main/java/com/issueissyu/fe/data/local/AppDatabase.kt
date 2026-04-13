package com.issueissyu.fe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IssueEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun issueDao(): IssueDao
}
