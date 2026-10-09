package com.goldengovernance.offlinetracker

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName="tasks")
data class TaskEntity(
 @PrimaryKey(autoGenerate=true) val id:Long=0L,
 val title:String,
 val done:Boolean=false
)
@Dao
interface TaskDao {
 @Query("SELECT * FROM tasks ORDER BY id DESC")
 fun observe(): Flow<List<TaskEntity>>
 @Insert
 suspend fun insert(entity:TaskEntity):Long
 @Query("UPDATE tasks SET done = CASE WHEN done=0 THEN 1 ELSE 0 END WHERE id=:id")
 suspend fun toggle(id:Long):Int
 @Query("DELETE FROM tasks WHERE id=:id")
 suspend fun remove(id:Long):Int
}
@Database(entities=[TaskEntity::class],version=1,exportSchema=true)
abstract class TaskRoomDatabase: RoomDatabase() {
 abstract fun taskDao():TaskDao
}
