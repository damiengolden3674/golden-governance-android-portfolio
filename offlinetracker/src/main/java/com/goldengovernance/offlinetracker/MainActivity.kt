package com.goldengovernance.offlinetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskItem(val id: Long,val title: String,val done: Boolean)
fun remaining(tasks: List<TaskItem>)=tasks.count { !it.done }

// Room-backed local-first persistence. The legacy SQLite prototype database
// remains untouched; a production migration tool must be tested before upgrades.
@HiltViewModel
class TaskViewModel @Inject constructor(private val dao:TaskDao):ViewModel() {
 val tasks=dao.observe().map { entries -> entries.map { TaskItem(it.id,it.title,it.done) } }
  .stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
 fun add(title:String) {
  val value=title.trim()
  if(value.isNotBlank()) viewModelScope.launch { dao.insert(TaskEntity(title=value)) }
 }
 fun toggle(id:Long)=viewModelScope.launch { dao.toggle(id) }
 fun remove(id:Long)=viewModelScope.launch { dao.remove(id) }
}
@AndroidEntryPoint
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { TaskScreen() }}
}
@Composable fun TaskScreen(vm:TaskViewModel=viewModel()){
 val tasks by vm.tasks.collectAsStateWithLifecycle()
 var title by remember { mutableStateOf("") }
 MaterialTheme {
  Scaffold(topBar={Text("Golden Governance • Offline Tasks",modifier=Modifier.padding(20.dp),style=MaterialTheme.typography.titleLarge)}) { padding ->
   Column(Modifier.fillMaxSize().padding(padding).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
    Text("Local-only task tracking. No account or cloud upload.")
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
     OutlinedTextField(title,{title=it},label={Text("New task")},modifier=Modifier.weight(1f),singleLine=true)
     Button(onClick={vm.add(title);title=""}){Text("Add")}
    }
    Text("${remaining(tasks)} remaining • ${tasks.size} total")
    LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
     items(tasks,key={it.id}){task->
      Card(Modifier.fillMaxWidth()){
       Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween){
        Checkbox(checked=task.done,onCheckedChange={vm.toggle(task.id)})
        Text(task.title,modifier=Modifier.weight(1f).padding(10.dp))
        TextButton(onClick={vm.remove(task.id)}){Text("Delete")}
       }
      }
     }
    }
   }
  }
 }
}
