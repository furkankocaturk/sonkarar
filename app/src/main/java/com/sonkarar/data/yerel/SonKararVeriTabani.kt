package com.sonkarar.data.yerel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonkarar.data.yerel.varlik.CarkVarligi
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import com.sonkarar.data.yerel.varlik.KararGecmisiVarligi

@Database(
    entities = [HavuzOgesiVarligi::class, KararGecmisiVarligi::class, CarkVarligi::class],
    version = 4,
    exportSchema = false
)
abstract class SonKararVeriTabani : RoomDatabase() {
    abstract fun havuzDao(): HavuzDao
    abstract fun kararGecmisiDao(): KararGecmisiDao
    abstract fun carkDao(): CarkDao
}
