package com.sonkarar.data.yerel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import com.sonkarar.data.yerel.varlik.KararGecmisiVarligi

@Database(
    entities = [HavuzOgesiVarligi::class, KararGecmisiVarligi::class],
    version = 3,
    exportSchema = false
)
abstract class SonKararVeriTabani : RoomDatabase() {
    abstract fun havuzDao(): HavuzDao
    abstract fun kararGecmisiDao(): KararGecmisiDao
}
