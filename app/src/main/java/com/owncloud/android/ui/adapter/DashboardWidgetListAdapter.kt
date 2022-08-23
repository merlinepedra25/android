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

package com.owncloud.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nextcloud.android.lib.resources.dashboard.DashboardWidget
import com.nextcloud.client.widget.DashboardWidgetConfigurationInterface
import com.owncloud.android.R
import com.owncloud.android.databinding.WidgetListItemBinding
import com.owncloud.android.utils.theme.ThemeDrawableUtils

class DashboardWidgetListAdapter(
    val themeDrawableUtils: ThemeDrawableUtils,
    private val dashboardWidgetConfigurationInterface: DashboardWidgetConfigurationInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val widgets = listOf(
        DashboardWidget("talk", "Talk mentions", R.drawable.ic_talk),
        DashboardWidget("mail", "Unread mail", R.drawable.ic_email),
        DashboardWidget("weather", "Weather", R.drawable.ic_dashboard),
        DashboardWidget("events", "Upcoming events", R.drawable.file_calendar)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WidgetListItemViewHolder(
            WidgetListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            themeDrawableUtils
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val widgetListItemViewHolder = holder as WidgetListItemViewHolder

        widgetListItemViewHolder.bind(widgets[position], dashboardWidgetConfigurationInterface)
    }

    override fun getItemCount(): Int {
        return widgets.size
    }
}
