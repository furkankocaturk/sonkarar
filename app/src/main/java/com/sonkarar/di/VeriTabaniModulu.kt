package com.sonkarar.di

import android.content.Context
import androidx.room.Room
import com.sonkarar.data.yerel.HavuzDao
import com.sonkarar.data.yerel.KararGecmisiDao
import com.sonkarar.data.yerel.SonKararVeriTabani
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VeriTabaniModulu {

    @Provides
    @Singleton
    fun veriTabaniSagla(@ApplicationContext baglam: Context): SonKararVeriTabani =
        Room.databaseBuilder(
            baglam,
            SonKararVeriTabani::class.java,
            "sonkarar_veritabani"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun havuzDaoSagla(veriTabani: SonKararVeriTabani): HavuzDao =
        veriTabani.havuzDao()

    @Provides
    fun kararGecmisiDaoSagla(veriTabani: SonKararVeriTabani): KararGecmisiDao =
        veriTabani.kararGecmisiDao()
}
