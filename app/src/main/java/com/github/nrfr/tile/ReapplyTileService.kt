package com.github.nrfr.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.github.nrfr.manager.SavedConfigManager
import rikka.shizuku.Shizuku

/**
 * 快捷设置磁贴：一键重应用保存的国家码覆盖。
 * 用户下拉通知栏点一下就行，不用进 App。
 */
@RequiresApi(Build.VERSION_CODES.N)
class ReapplyTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = if (SavedConfigManager.hasSaved(this)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (!Shizuku.pingBinder()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.updateTile()
            return
        }
        val ok = SavedConfigManager.reapplyIfNeeded(applicationContext)
        tile.state = if (ok) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
