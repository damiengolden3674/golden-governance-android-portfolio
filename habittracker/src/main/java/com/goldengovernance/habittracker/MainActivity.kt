package com.goldengovernance.habittracker

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.habitStore by preferencesDataStore(name="habit_preferences")
private val DAYS=intPreferencesKey("completed_days")
fun completionPercent(done:Int,goal:Int):Int=if(goal<=0)0 else (100*done/goal).coerceIn(0,100)
class HabitRepository(private val context:Context) {
 val completed=context.habitStore.data.map{it[DAYS]?:0}
 suspend fun increment() { context.habitStore.edit { it[DAYS]=(it[DAYS]?:0)+1 } }
 suspend fun reset() { context.habitStore.edit { it[DAYS]=0 } }
}
class HabitViewModel(app:Application):AndroidViewModel(app) {
 private val repository=HabitRepository(app.applicationContext)
 val days=repository.completed.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),0)
 fun completeToday()=viewModelScope.launch { repository.increment() }
 fun reset()=viewModelScope.launch { repository.reset() }
}
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?) {super.onCreate(savedInstanceState);setContent { HabitScreen() }}
}
@Composable fun HabitScreen(vm:HabitViewModel=viewModel()){
 val days by vm.days.collectAsStateWithLifecycle()
 val goal=21
 val context=LocalContext.current
 val requestPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
  if(granted) scheduleHabitReminder(context)
 }
 MaterialTheme {
  Scaffold(topBar={Text("Golden Governance • Habits",modifier=Modifier.padding(20.dp),style=MaterialTheme.typography.titleLarge)}) { padding ->
   Column(Modifier.fillMaxSize().padding(padding).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
    Text("Daily habit progress",style=MaterialTheme.typography.headlineSmall)
    Text("${days} completed sessions • goal ${goal}")
    LinearProgressIndicator(progress={completionPercent(days,goal)/100f},modifier=Modifier.fillMaxWidth())
    Button(onClick={vm.completeToday()}){Text("Log one completed session")}
    OutlinedButton(onClick={vm.reset()}){Text("Reset progress")}
    Button(onClick={
      if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
       requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else scheduleHabitReminder(context)
    }) { Text("Enable daily reminders") }
    OutlinedButton(onClick={cancelHabitReminder(context)}) { Text("Disable reminders") }
    Text("Progress is stored locally using DataStore. Reminders use WorkManager and may run later than scheduled under system constraints.")
   }
  }
 }
}
