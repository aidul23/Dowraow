package com.aidul23.dowraow.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(
    Convertors::class
)
abstract class RunDatabase : RoomDatabase(){
    abstract fun getRunDao() : RunDAO
}