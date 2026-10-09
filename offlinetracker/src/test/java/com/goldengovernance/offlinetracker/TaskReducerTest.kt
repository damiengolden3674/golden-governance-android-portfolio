package com.goldengovernance.offlinetracker
import org.junit.Assert.assertEquals
import org.junit.Test
class TaskReducerTest {
 @Test fun remainingIgnoresCompleted(){
  assertEquals(1,remaining(listOf(TaskItem(1,"One",true),TaskItem(2,"Two",false))))
 }
 @Test fun noTasks(){ assertEquals(0,remaining(emptyList<TaskItem>())) }
}
