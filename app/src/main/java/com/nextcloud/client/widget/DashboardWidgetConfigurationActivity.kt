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
package com.nextcloud.client.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nextcloud.android.lib.resources.dashboard.DashboardListWidgetsRemoteOperation
import com.nextcloud.android.lib.resources.dashboard.DashboardWidget
import com.nextcloud.client.account.User
import com.nextcloud.client.account.UserAccountManager
import com.nextcloud.client.di.Injectable
import com.nextcloud.client.network.ClientFactory
import com.owncloud.android.R
import com.owncloud.android.databinding.DashboardWidgetConfigurationLayoutBinding
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.ui.adapter.DashboardWidgetListAdapter
import com.owncloud.android.ui.dialog.AccountChooserInterface
import com.owncloud.android.ui.dialog.MultipleAccountsDialog
import com.owncloud.android.utils.theme.ThemeDrawableUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DashboardWidgetConfigurationActivity : AppCompatActivity(), DashboardWidgetConfigurationInterface, Injectable,
    AccountChooserInterface {
    private lateinit var adapter: DashboardWidgetListAdapter
    private lateinit var binding: DashboardWidgetConfigurationLayoutBinding

    @Inject
    lateinit var themeDrawableUtils: ThemeDrawableUtils

    @Inject
    lateinit var accountManager: UserAccountManager

    @Inject
    lateinit var clientFactory: ClientFactory

    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = DashboardWidgetConfigurationLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        adapter = DashboardWidgetListAdapter(themeDrawableUtils, accountManager, clientFactory, this, this)
        binding.list.apply {
            setHasFooter(false)
            setAdapter(adapter)
            setLayoutManager(layoutManager)
            setEmptyView(binding.emptyView.emptyListView)
        }

        if (accountManager.allUsers.size > 2) {
            // show dropdown
            binding.account.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    val dialog = MultipleAccountsDialog()
                    dialog.show(supportFragmentManager, null)
                }
            }
        } else {
            loadWidgets(accountManager.user)
        }

        binding.close.setOnClickListener { finish() }

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    private fun loadWidgets(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.emptyView.root.visibility = View.GONE
            }

            try {
                val client = clientFactory.createNextcloudClient(user)
                val result = DashboardListWidgetsRemoteOperation().execute(client)

                withContext(Dispatchers.Main) {
                    adapter.setWidgetList(result.resultData)
                }
            } catch (e: Exception) {
                Log_OC.e(this, "Error loading widgets for user $user", e)

                withContext(Dispatchers.Main) {
                    binding.emptyView.emptyListIcon.apply {
                        setImageResource(R.drawable.ic_list_empty_error)
                        visibility = View.VISIBLE
                    }
                    binding.emptyView.emptyListViewText.apply {
                        setText(R.string.common_error)
                        visibility = View.VISIBLE
                    }
                    binding.emptyView.emptyListViewAction.apply {
                        visibility = View.VISIBLE
                        setText(R.string.reload)
                        setOnClickListener {
                            loadWidgets(user)
                        }
                    }
                }
            }
        }
    }

    override fun onItemClicked(dashboardWidget: DashboardWidget) {
        // ListSharedPrefsUtil.saveWidgetLayoutIdPref(this, appWidgetId, widgetLayoutResId)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        // ListAppWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun onAccountChosen(user: User) {
        loadWidgets(user)
    }
}
