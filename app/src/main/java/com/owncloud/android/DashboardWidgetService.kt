/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.users.StatusType
import com.owncloud.android.ui.TextDrawable
import com.owncloud.android.utils.BitmapUtils
import java.text.DateFormat
import java.util.Date
import java.util.Random

class DashboardWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext, intent)
    }
}

private const val REMOTE_VIEW_COUNT: Int = 10

class StackRemoteViewsFactory(
    private val context: Context,
    val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var widgetItems: List<WidgetItem>

    // See the RemoteViewsFactory API reference for the full list of methods to
// implement.
    override fun onCreate() {
        Log_OC.d("WidgetService", "onCreate")

        widgetItems = List(REMOTE_VIEW_COUNT) { index ->
            WidgetItem(
                "$index!", "Subline: " + DateFormat.getTimeInstance(
                    DateFormat.SHORT
                ).format(Date())
            )
        }
    }

    override fun onDataSetChanged() {
        widgetItems = List(REMOTE_VIEW_COUNT) { index -> randomItem() }
        Log_OC.d("WidgetService", "onDataSetChanged")
    }

    override fun onDestroy() {
        Log_OC.d("WidgetService", "onDestroy")

        widgetItems = emptyList()
    }

    override fun getCount(): Int {
        return widgetItems.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_item).apply {
            val widgetItem = widgetItems[position]

            // icon
            if (widgetItem.userName != null) {
                val avatarRadius: Float = context.resources.getDimension(R.dimen.widget_avatar_icon_radius)
                val avatarBitmap =
                    BitmapUtils.drawableToBitmap(TextDrawable.createNamedAvatar(widgetItem.userName, avatarRadius))

                // val avatar = BitmapUtils.createAvatarWithStatus(
                //     avatarBitmap,
                //     widgetItem.statusType,
                //     widgetItem.icon ?: "",
                //     context
                // )

                setImageViewBitmap(R.id.icon, avatarBitmap)
            } else {
                setImageViewResource(R.id.icon, R.drawable.ic_group)
            }

            // text
            setTextViewText(R.id.headline, widgetItem.headline)
            setTextViewText(R.id.subline, widgetItem.subline)



            if (widgetItem.url != null) {
                val clickIntent = Intent(Intent.ACTION_VIEW, Uri.parse(widgetItem.url))
                setOnClickFillInIntent(R.id.text_container, clickIntent)
            }
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true // TODO check me
    }

    private fun randomItem(): WidgetItem {
        return when (Random().nextInt(10)) {
            0 -> WidgetItem("⚙️ Sysadmin", "You were mentioned", "https://sysadmin.de")
            1 -> WidgetItem("👩‍⚖️ Support!", "You were mentioned", "https://support.nextcloud.com")
            2 -> WidgetItem("Andy Scherzinger", "Please reply", null, "Andy", statusType = StatusType.DND)
            3 -> WidgetItem("Christoph Wurst", "See you next week!", null, "Christoph Wurst", "🌴️", StatusType.ONLINE)
            4 -> WidgetItem("\uD83D\uDEE0️ Engineering", "You were mentioned")
            5 -> WidgetItem("\uD83D\uDCF1 Mobile apps public", "Please see link above.")
            else -> WidgetItem(
                "Jos Poortvliet",
                "Haha, funny",
                null,
                "Jos Poortvliet",
                "\uD83D\uDC99",
                StatusType.ONLINE
            )

        }
    }
}
