package com.goldengovernance.offlinetracker

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class GoldenTrackerApplication: Application()

@Module
@InstallIn(SingletonComponent::class)
object TrackerModule {
 @Provides @Singleton
 fun provideDatabase(@ApplicationContext context:Context): TaskRoomDatabase =
   Room.databaseBuilder(context,TaskRoomDatabase::class.java,"golden_task_room_v1.db").build()
 @Provides
 fun provideDao(db:TaskRoomDatabase):TaskDao=db.taskDao()
}
