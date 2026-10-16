package com.github.nrfr.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.nrfr.manager.SavedConfigManager
import rikka.shizuku.Shizuku

/**
 * 开机后尝试重应用保存的国家码覆盖。
 * 注意：Shizuku 本身需要用户手动启动（无线调试配对），
 * 这里会在 Shizuku 就绪时自动重应用，无需用户再进 App 点。
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NrfrBoot"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Log.i(TAG, "Boot completed, checking saved config")
        if (!SavedConfigManager.hasSaved(context)) {
            Log.i(TAG, "No saved config, skipping")
            return
        }

        // 如果 Shizuku 已经就绪，直接重应用；否则注册监听等它就绪
        if (Shizuku.pingBinder()) {
            val ok = SavedConfigManager.reapplyIfNeeded(context.applicationContext)
            Log.i(TAG, "Reapply on boot: $ok")
        } else {
            Log.i(TAG, "Shizuku not ready, waiting for binder")
            Shizuku.addBinderReceivedListenerSticky(object : Shizuku.OnBinderReceivedListener {
                override fun onBinderReceived() {
                    val ok = SavedConfigManager.reapplyIfNeeded(context.applicationContext)
                    Log.i(TAG, "Reapply on Shizuku ready: $ok")
                    Shizuku.removeBinderReceivedListener(this)
                }
            })
        }
    }
}
