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

import androidx.recyclerview.widget.RecyclerView
import com.nextcloud.client.widget.DashboardWidget
import com.nextcloud.client.widget.DashboardWidgetConfigurationInterface
import com.owncloud.android.R
import com.owncloud.android.databinding.WidgetListItemBinding
import com.owncloud.android.utils.theme.ThemeDrawableUtils

class WidgetListItemViewHolder(val binding: WidgetListItemBinding, val themeDrawableUtils: ThemeDrawableUtils) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        dashboardWidget: DashboardWidget,
        dashboardWidgetConfigurationInterface: DashboardWidgetConfigurationInterface
    ) {
        binding.layout.setOnClickListener { dashboardWidgetConfigurationInterface.onItemClicked(dashboardWidget) }
        binding.icon.setImageDrawable(themeDrawableUtils.tintDrawable(dashboardWidget.icon, R.color.black))
        binding.name.text = dashboardWidget.title
    }
}
