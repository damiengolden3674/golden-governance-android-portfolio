package com.goldengovernance.habittracker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val CHANNEL="habit-reminder"
private const val WORK="daily-habit-reminder"

/** Deferrable notification work: Android chooses execution time, not a guaranteed alarm. */
class HabitReminderWorker(context: Context,params: WorkerParameters): Worker(context,params) {
 override fun doWork(): Result {
  val context=applicationContext
  if(Build.VERSION.SDK_INT>=33 &&
     ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
   return Result.success()
  }
  val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26) {
   manager.createNotificationChannel(NotificationChannel(CHANNEL,"Habit reminders",NotificationManager.IMPORTANCE_DEFAULT))
  }
  val notification=Notification.Builder(context,CHANNEL)
   .setSmallIcon(android.R.drawable.ic_popup_reminder)
   .setContentTitle("Habit Tracker")
   .setContentText("Log a completed habit session when you are ready.")
   .setAutoCancel(true)
   .build()
  manager.notify(4001,notification)
  return Result.success()
 }
}
fun scheduleHabitReminder(context:Context) {
 val request=PeriodicWorkRequestBuilder<HabitReminderWorker>(1,TimeUnit.DAYS)
  .setConstraints(Constraints.Builder().build())
  .build()
 WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK,ExistingPeriodicWorkPolicy.KEEP,request)
}
fun cancelHabitReminder(context:Context) {
 WorkManager.getInstance(context).cancelUniqueWork(WORK)
}
