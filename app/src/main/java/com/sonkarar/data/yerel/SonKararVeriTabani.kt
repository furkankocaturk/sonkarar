package com.sonkarar.data.yerel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi

@Database(
    entities = [HavuzOgesiVarligi::class],
    version = 2,
    exportSchema = false
)
abstract class SonKararVeriTabani : RoomDatabase() {
    abstract fun havuzDao(): HavuzDao
}
