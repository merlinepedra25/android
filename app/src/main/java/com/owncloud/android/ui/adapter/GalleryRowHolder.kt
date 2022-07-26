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

import android.content.Context
import android.widget.ImageView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.owncloud.android.R
import com.owncloud.android.databinding.GalleryRowBinding
import com.owncloud.android.datamodel.GalleryRow
import com.owncloud.android.lib.common.network.ImageDimension
import com.owncloud.android.utils.DisplayUtils

class GalleryRowHolder(
    val binding: GalleryRowBinding,
    private val defaultThumbnailSize: Float,
    private val ocFileListDelegate: OCFileListDelegate,
    val context: Context
) : SectionedViewHolder(binding.root) {
    fun bind(row: GalleryRow) {
        binding.rowLayout.removeAllViews()

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

        var newSummedWidth = 0f
        for (file in row.files) {
            // TODO change depending on screen orientation
            // first adjust all thumbnails to max height
            val height1 = thumbnail1.height
            val width1 = thumbnail1.width
            val oldAspect1 = height1 / width1.toFloat()

            val scaleFactor1 = row.getMaxHeight().toFloat() / height1
            val newHeight1 = height1 * scaleFactor1
            val newWidth1 = width1.toFloat() * scaleFactor1
            val newAspect1 = height1 / width1.toFloat() // must be same as oldAspect

            file.imageDimension.height = newHeight1
            file.imageDimension.width = newWidth1
                       

            newSummedWidth += newWidth1
        }

        val shrinkRatio = screenWidth / newSummedWidth

        for (file in row.files) {
            val adjustedHeight1 = (file.imageDimension.height * shrinkRatio).toInt()
            val adjustedWidth1 = (file.imageDimension.width * shrinkRatio).toInt()

            val thumbnail = ImageView(context)
            thumbnail.setImageDrawable(context.getDrawable(R.drawable.file_image))
            thumbnail.adjustViewBounds = true
            // thumbnail. = "5dp"

            binding.rowLayout.addView(thumbnail)

            ocFileListDelegate.bindGalleryRowThumbnail(
                thumbnail,
                file
            )

            thumbnail.layoutParams.height = adjustedHeight1
            thumbnail.layoutParams.width = adjustedWidth1
        }

        //
        // val height2 = thumbnail2.height
        // val width2 = thumbnail2.width
        // val oldAspect2 = height2 / width2.toFloat()
        //
        // val scaleFactor2 = row.getMaxHeight().toFloat() / height2
        // val newHeight2 = height2 * scaleFactor2
        // val newWidth2 = width2.toFloat() * scaleFactor2
        // val newAspect2 = height2 / width2.toFloat() // must be same as oldAspect
        //
        //
        // Log_OC.d(
        //     "Gallery_thumbnail",
        //     "old: $width2 x $height2 new: $newWidth2 x $newHeight2 aspectOld: $oldAspect2 aspectNew: $newAspect2"
        // )
        //
        // if (row.files.size > 1) {
        //     val adjustedHeight2 = (newHeight2 * shrinkRatio).toInt()
        //     val adjustedWidth2 = (newWidth2 * shrinkRatio).toInt()
        //
        //     val sumAdjustedWith = adjustedWidth1 + adjustedWidth1
        //
        //     ocFileListDelegate.bindGalleryRowThumbnail(
        //         binding.thumbnail2,
        //         row.files[1]
        //     )
        //     binding.thumbnail2.layoutParams.height = adjustedHeight2
        //     binding.thumbnail2.layoutParams.width = adjustedWidth2
        //     //rowHolder.binding.thumbnail2.invalidate()
        //
        //     Log_OC.d(
        //         "Gallery_thumbnail",
        //         "Screen width: $screenWidth shrinkRatio: $shrinkRatio maxHeight: ${row.getMaxHeight()}"
        //     )
        //     Log_OC.d(
        //         "Gallery_thumbnail",
        //         "file1: $adjustedWidth1 x $adjustedHeight1 aspectOld: $oldAspect1 aspectNew: $newAspect1"
        //     )
        //     Log_OC.d(
        //         "Gallery_thumbnail",
        //         "file2: $adjustedWidth2 x $adjustedHeight2 aspectOld: $oldAspect2 aspectNew: $newAspect2"
        //     )
        // } else {
        //     binding.thumbnail2.visibility = View.GONE
        // }
    }
}
