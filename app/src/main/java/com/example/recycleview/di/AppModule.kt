package com.example.recycleview.di

import android.app.Application
import androidx.room.Room
import com.example.recycleview.data.PlantDatabase
import com.example.recycleview.repo.PlantRepository
import com.example.recycleview.repo.PlantRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application) =
        Room.databaseBuilder(app, PlantDatabase::class.java, "plant_database")
        .build()

    @Provides
    fun providePlantDao(db: PlantDatabase) = db.plantDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    fun provideRepo(context: Application, db: PlantDatabase): PlantRepository {
        return PlantRepositoryImpl(context, db)
    }

}
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope