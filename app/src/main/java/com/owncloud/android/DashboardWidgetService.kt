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
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.owncloud.android.lib.common.utils.Log_OC
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
    intent: Intent
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
            setTextViewText(R.id.headline, widgetItems[position].headline)
            setTextViewText(R.id.subline, widgetItems[position].subline)
        }
        // TODO on click listener
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
            0 -> WidgetItem("⚙️ Sysadmin", "You were mentioned")
            1 -> WidgetItem("👩‍⚖️ Support!", "You were mentioned")
            2 -> WidgetItem("Andy Scherzinger", "Please reply")
            3 -> WidgetItem("Christoph Wurst", "See you next week!")
            4 -> WidgetItem("\uD83D\uDEE0️ Engineering", "You were mentioned")
            5 -> WidgetItem("\uD83D\uDCF1 Mobile apps public", "Please see link above.")
            else -> WidgetItem("Jos Poortvliet", "Haha, funny")

        }
    }
}
