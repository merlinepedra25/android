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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast

/**
 * Implementation of App Widget functionality.
 */
class DashboardWidgetProvider : AppWidgetProvider() {
    val TOAST_ACTION = "com.example.android.stackwidget.TOAST_ACTION"
    val EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val mgr = AppWidgetManager.getInstance(context)
        if (intent.action == TOAST_ACTION) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
            Toast.makeText(context, "Touched view $viewIndex", Toast.LENGTH_SHORT).show()
        }


        super.onReceive(context, intent)
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Set up the intent that starts the StackViewService, which will
    // provide the views for this collection.
    val intent = Intent(context, DashboardWidgetService::class.java).apply {
        // Add the widget ID to the intent extras.
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.dashboard_widget).apply {
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects to a RemoteViewsService through the
        // specified intent.
        // This is how you populate the data.
        setRemoteAdapter(R.id.list, intent)

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the
        // RemoteViews object.
        setEmptyView(R.id.list, R.id.empty_view)

        setTextViewText(R.id.title, "Talk mentions")

        //Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action//
        val intentUpdate = Intent(context, DashboardWidgetProvider::class.java)
        intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

//Update the current widget instance only, by creating an array that contains the widget’s unique ID// 

//Update the current widget instance only, by creating an array that contains the widget’s unique ID// 
        val idArray = intArrayOf(appWidgetId)
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray)

        setOnClickPendingIntent(
            R.id.reload,
            PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list)

    Toast.makeText(context, "Widget has been updated: $appWidgetId", Toast.LENGTH_SHORT).show();
}
