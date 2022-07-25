/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * @author TSI-mc
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 * Copyright (C) 2022 TSI-mc
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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.nextcloud.client.account.User
import com.nextcloud.client.preferences.AppPreferences
import com.owncloud.android.databinding.GalleryHeaderBinding
import com.owncloud.android.databinding.GalleryRowBinding
import com.owncloud.android.datamodel.FileDataStorageManager
import com.owncloud.android.datamodel.GalleryItems
import com.owncloud.android.datamodel.GalleryRow
import com.owncloud.android.datamodel.OCFile
import com.owncloud.android.datamodel.ThumbnailsCacheManager
import com.owncloud.android.lib.common.network.ImageDimension
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.ui.activity.ComponentsGetter
import com.owncloud.android.ui.fragment.GalleryFragment
import com.owncloud.android.ui.fragment.GalleryFragmentBottomSheetDialog
import com.owncloud.android.ui.fragment.SearchType
import com.owncloud.android.ui.interfaces.OCFileListFragmentInterface
import com.owncloud.android.utils.DisplayUtils
import com.owncloud.android.utils.FileSortOrder
import com.owncloud.android.utils.MimeTypeUtil
import com.owncloud.android.utils.theme.ThemeColorUtils
import com.owncloud.android.utils.theme.ThemeDrawableUtils
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.util.Calendar
import java.util.Date

@Suppress("LongParameterList")
class GalleryAdapter(
    val context: Context,
    user: User,
    ocFileListFragmentInterface: OCFileListFragmentInterface,
    preferences: AppPreferences,
    transferServiceGetter: ComponentsGetter,
    themeColorUtils: ThemeColorUtils,
    themeDrawableUtils: ThemeDrawableUtils
) : SectionedRecyclerViewAdapter<SectionedViewHolder>(), CommonOCFileListAdapterInterface, PopupTextProvider {
    var files: List<GalleryItems> = mutableListOf()
    private val ocFileListDelegate: OCFileListDelegate
    private var storageManager: FileDataStorageManager
    private val defaultThumbnailSize = ThumbnailsCacheManager.getThumbnailDimension()

    init {
        storageManager = transferServiceGetter.storageManager


        ocFileListDelegate = OCFileListDelegate(
            context,
            ocFileListFragmentInterface,
            user,
            storageManager,
            false,
            preferences,
            true,
            transferServiceGetter,
            showMetadata = false,
            showShareAvatar = false,
            themeColorUtils,
            themeDrawableUtils
        )
    }

    override fun showFooters(): Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            GalleryHeaderViewHolder(
                GalleryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            GalleryRowHolder(
                GalleryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {
        if (holder != null) {
            val rowHolder = holder as GalleryRowHolder
            val row = files[section].rows[relativePosition]

            val screenWidth =
                DisplayUtils.convertDpToPixel(context.resources.configuration.screenWidthDp.toFloat(), context)
                    .toFloat()
            val summedWidth = row.getSummedWidth().toFloat()

            val thumbnail1 = row.files[0].imageDimension ?: ImageDimension(defaultThumbnailSize, defaultThumbnailSize)

            val thumbnail2 = if (row.files.size > 1) {
                row.files[1].imageDimension ?: ImageDimension(defaultThumbnailSize, defaultThumbnailSize)
            } else {
                ImageDimension(defaultThumbnailSize, defaultThumbnailSize)
            }

            // first adjust all thumbnails to max height
            val height1 = thumbnail1.height
            val width1 = thumbnail1.width
            val oldAspect1 = height1 / width1.toFloat()

            val scaleFactor1 = row.getMaxHeight().toFloat() / height1
            val newHeight1 = height1 * scaleFactor1
            val newWidth1 = width1.toFloat() * scaleFactor1
            val newAspect1 = height1 / width1.toFloat() // must be same as oldAspect

            val height2 = thumbnail2.height
            val width2 = thumbnail2.width
            val oldAspect2 = height2 / width2.toFloat()

            val scaleFactor2 = row.getMaxHeight().toFloat() / height2
            val newHeight2 = height2 * scaleFactor2
            val newWidth2 = width2.toFloat() * scaleFactor2
            val newAspect2 = height2 / width2.toFloat() // must be same as oldAspect

            Log_OC.d(
                "Gallery_thumbnail",
                "old: $width1 x $height1 new: $newWidth1 x $newHeight1 aspectOld: $oldAspect1 aspectNew: $newAspect1"
            )
            Log_OC.d(
                "Gallery_thumbnail",
                "old: $width2 x $height2 new: $newWidth2 x $newHeight2 aspectOld: $oldAspect2 aspectNew: $newAspect2"
            )

            val newSummedWidth = newWidth1 + newWidth2
            val shrinkRatio = screenWidth / newSummedWidth

            val adjustedHeight1 = (newHeight1 * shrinkRatio).toInt()
            val adjustedWidth1 = (newWidth1 * shrinkRatio).toInt()

            ocFileListDelegate.bindGalleryRowThumbnail(
                rowHolder.binding.thumbnail1,
                row.files[0]
            )

            rowHolder.binding.thumbnail1.layoutParams.height = adjustedHeight1
            rowHolder.binding.thumbnail1.layoutParams.width = adjustedWidth1
            //rowHolder.binding.thumbnail1.invalidate()

            if (row.files.size > 1) {
                val adjustedHeight2 = (newHeight2 * shrinkRatio).toInt()
                val adjustedWidth2 = (newWidth2 * shrinkRatio).toInt()

                val sumAdjustedWith = adjustedWidth1 + adjustedWidth1

                ocFileListDelegate.bindGalleryRowThumbnail(
                    rowHolder.binding.thumbnail2,
                    row.files[1]
                )
                rowHolder.binding.thumbnail2.layoutParams.height = adjustedHeight2
                rowHolder.binding.thumbnail2.layoutParams.width = adjustedWidth2
                //rowHolder.binding.thumbnail2.invalidate()

                Log_OC.d(
                    "Gallery_thumbnail",
                    "Screen width: $screenWidth shrinkRatio: $shrinkRatio maxHeight: ${row.getMaxHeight()}"
                )
                Log_OC.d(
                    "Gallery_thumbnail",
                    "file1: $adjustedWidth1 x $adjustedHeight1 aspectOld: $oldAspect1 aspectNew: $newAspect1"
                )
                Log_OC.d(
                    "Gallery_thumbnail",
                    "file2: $adjustedWidth2 x $adjustedHeight2 aspectOld: $oldAspect2 aspectNew: $newAspect2"
                )
            } else {
                rowHolder.binding.thumbnail2.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(section: Int): Int {
        return files[section].rows.size
    }

    override fun getSectionCount(): Int {
        return files.size
    }

    override fun getPopupText(position: Int): String {
        return DisplayUtils.getDateByPattern(
            files[getRelativePosition(position).section()].date,
            context,
            DisplayUtils.MONTH_YEAR_PATTERN
        )
    }

    override fun onBindHeaderViewHolder(holder: SectionedViewHolder?, section: Int, expanded: Boolean) {
        if (holder != null) {
            val headerViewHolder = holder as GalleryHeaderViewHolder
            val galleryItem = files[section]

            headerViewHolder.binding.month.text = DisplayUtils.getDateByPattern(
                galleryItem.date,
                context,
                DisplayUtils.MONTH_PATTERN
            )
            headerViewHolder.binding.year.text = DisplayUtils.getDateByPattern(
                galleryItem.date,
                context, DisplayUtils.YEAR_PATTERN
            )
        }
    }

    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {
        TODO("Not yet implemented")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAllGalleryItems(
        remotePath: String,
        mediaState: GalleryFragmentBottomSheetDialog.MediaState,
        photoFragment: GalleryFragment
    ) {

        val items = storageManager.allGalleryItems

        val filteredList = items.filter { it != null && it.remotePath.startsWith(remotePath) }

        setMediaFilter(
            filteredList,
            mediaState,
            photoFragment
        )
    }

    // Set Image/Video List According to Selection of Hide/Show Image/Video
    @SuppressLint("NotifyDataSetChanged")
    private fun setMediaFilter(
        items: List<OCFile>,
        mediaState: GalleryFragmentBottomSheetDialog.MediaState,
        photoFragment: GalleryFragment
    ) {

        val finalSortedList: List<OCFile> = when (mediaState) {
            GalleryFragmentBottomSheetDialog.MediaState.MEDIA_STATE_PHOTOS_ONLY -> {
                items.filter { MimeTypeUtil.isImage(it.mimeType) }.distinct()
            }
            GalleryFragmentBottomSheetDialog.MediaState.MEDIA_STATE_VIDEOS_ONLY -> {
                items.filter { MimeTypeUtil.isVideo(it.mimeType) }.distinct()
            }
            else -> items
        }

        if (finalSortedList.isEmpty()) {
            photoFragment.setEmptyListMessage(SearchType.GALLERY_SEARCH)
        }

        files = finalSortedList
            .groupBy { firstOfMonth(it.modificationTimestamp) }
            .map { GalleryItems(it.key, map(it.value)) }
            .sortedBy { it.date }.reversed()

        Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
    }

    fun map(list: List<OCFile>): List<GalleryRow> {
        return list.withIndex()
            .groupBy { it.index / 2 }
            .map { entry -> GalleryRow(entry.value.map { it.value }, defaultThumbnailSize, defaultThumbnailSize) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        files = emptyList()
        Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
    }

    private fun firstOfMonth(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.time = Date(timestamp)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        return cal.timeInMillis
    }

    fun isEmpty(): Boolean {
        return files.isEmpty()
    }

    fun getItem(position: Int): OCFile? {
        return null
        // TODO
        // val itemCoord = getRelativePosition(position)
        // return files
        //     .getOrNull(itemCoord.section())?.files
        //     ?.getOrNull(itemCoord.relativePos())
    }

    override fun isMultiSelect(): Boolean {
        return ocFileListDelegate.isMultiSelect
    }

    override fun cancelAllPendingTasks() {
        ocFileListDelegate.cancelAllPendingTasks()
    }

    override fun getItemPosition(file: OCFile): Int {
        return 1
        // TODO
        // val item = files.find { it.rows.contains(file) }
        // return getAbsolutePosition(files.indexOf(item), item?.files?.indexOf(file) ?: 0)
    }

    override fun swapDirectory(
        user: User,
        directory: OCFile,
        storageManager: FileDataStorageManager,
        onlyOnDevice: Boolean,
        mLimitToMimeType: String
    ) {
        TODO("Not yet implemented")
    }

    override fun setHighlightedItem(file: OCFile) {
        TODO("Not yet implemented")
    }

    override fun setSortOrder(mFile: OCFile, sortOrder: FileSortOrder) {
        TODO("Not yet implemented")
    }

    override fun addCheckedFile(file: OCFile) {
        ocFileListDelegate.addCheckedFile(file)
    }

    override fun isCheckedFile(file: OCFile): Boolean {
        return ocFileListDelegate.isCheckedFile(file)
    }

    override fun getCheckedItems(): Set<OCFile> {
        return ocFileListDelegate.checkedItems
    }

    override fun removeCheckedFile(file: OCFile) {
        ocFileListDelegate.removeCheckedFile(file)
    }

    override fun notifyItemChanged(file: OCFile) {
        notifyItemChanged(getItemPosition(file))
    }

    override fun getFilesCount(): Int {
        return files.fold(0) { acc, item -> acc + item.rows.size }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setMultiSelect(boolean: Boolean) {
        ocFileListDelegate.isMultiSelect = boolean
        notifyDataSetChanged()
    }

    override fun clearCheckedItems() {
        ocFileListDelegate.clearCheckedItems()
    }

    @VisibleForTesting
    fun addFiles(items: List<GalleryItems>) {
        files = items
    }
}
