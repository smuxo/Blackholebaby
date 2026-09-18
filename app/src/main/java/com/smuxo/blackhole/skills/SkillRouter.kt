package com.smuxo.blackhole.skills

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.widget.Toast

class SkillRouter(private val context: Context) {

    fun execute(action: String?, value: String?) {
        when (action) {
            "open_url" -> openUrl(value)
            "open_app" -> openApp(value)
            "set_timer" -> setTimer(value)
            "smalltalk" -> {}
            null -> {}
            else -> {}
        }
    }

    private fun openUrl(value: String?) {
        if (value.isNullOrBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    private fun openApp(value: String?) {
        if (value.isNullOrBlank()) return
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(value)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
        }
    }

    private fun setTimer(value: String?) {
        val seconds = value?.toLongOrNull() ?: return
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = SystemClock.elapsedRealtime() + seconds * 1000L
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAt,
                pendingIntent
            )
            Toast.makeText(context, "Таймер на " + seconds + " сек", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
        }
    }
}
