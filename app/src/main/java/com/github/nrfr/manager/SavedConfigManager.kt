package com.github.nrfr.manager

import android.content.Context
import android.content.SharedPreferences

/**
 * 持久化保存用户设置的国家码覆盖，开机/Shizuku就绪后自动重应用
 */
object SavedConfigManager {
    private const val PREFS = "nrfr_saved_config"
    private const val KEY_SUB_ID = "sub_id"
    private const val KEY_COUNTRY = "country_code"
    private const val KEY_CARRIER = "carrier_name"
    private const val KEY_ENABLED = "override_enabled"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, subId: Int, countryCode: String?, carrierName: String?) {
        prefs(context).edit()
            .putInt(KEY_SUB_ID, subId)
            .putString(KEY_COUNTRY, countryCode)
            .putString(KEY_CARRIER, carrierName)
            .putBoolean(KEY_ENABLED, true)
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, false).apply()
    }

    fun hasSaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    data class SavedConfig(val subId: Int, val countryCode: String?, val carrierName: String?)

    fun getSaved(context: Context): SavedConfig? {
        val p = prefs(context)
        if (!p.getBoolean(KEY_ENABLED, false)) return null
        return SavedConfig(
            subId = p.getInt(KEY_SUB_ID, -1).takeIf { it >= 0 } ?: return null,
            countryCode = p.getString(KEY_COUNTRY, null),
            carrierName = p.getString(KEY_CARRIER, null),
        )
    }

    /**
     * 尝试重应用已保存的配置。返回 true 表示已执行重应用。
     * 必须在 Shizuku 可用时调用。
     */
    fun reapplyIfNeeded(context: Context): Boolean {
        val saved = getSaved(context) ?: return false
        return try {
            CarrierConfigManager.setCarrierConfig(saved.subId, saved.countryCode, saved.carrierName)
            true
        } catch (e: Exception) {
            false
        }
    }
}
