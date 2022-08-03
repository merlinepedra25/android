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
import android.graphics.Color
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.get
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.elyeproj.loaderviewlibrary.LoaderImageView
import com.owncloud.android.R
import com.owncloud.android.databinding.GalleryRowBinding
import com.owncloud.android.datamodel.FileDataStorageManager
import com.owncloud.android.datamodel.GalleryRow
import com.owncloud.android.datamodel.ImageDimension
import com.owncloud.android.datamodel.ThumbnailsCacheManager
import com.owncloud.android.utils.BitmapUtils
import com.owncloud.android.utils.DisplayUtils

class GalleryRowHolder(
    val binding: GalleryRowBinding,
    private val defaultThumbnailSize: Float,
    private val ocFileListDelegate: OCFileListDelegate,
    val context: Context,
    val storageManager: FileDataStorageManager,
    private val galleryAdapter: GalleryAdapter
) : SectionedViewHolder(binding.root) {

    lateinit var currentRow: GalleryRow

    fun bind(row: GalleryRow) {
        currentRow = row

        // re-use existing ones
        while (binding.rowLayout.childCount < row.files.size) {
            val shimmer = LoaderImageView(context).apply {
                setBackgroundColor(Color.RED)
                setImageResource(R.drawable.background)
                resetLoader()
                invalidate()
            }

            val imageView = ImageView(context).apply {
                setImageDrawable(
                    ThumbnailsCacheManager.AsyncGalleryImageDrawable(
                        context.resources,
                        BitmapUtils.drawableToBitmap(
                            resources.getDrawable(R.drawable.file_image, null),
                            defaultThumbnailSize.toInt(),
                            defaultThumbnailSize.toInt()
                        ),
                        null
                    )
                )
            }

            LinearLayout(context).apply {
                addView(shimmer)
                addView(imageView)

                binding.rowLayout.addView(this)
            }
        }

        if (binding.rowLayout.childCount > row.files.size) {
            binding.rowLayout.removeViewsInLayout(row.files.size - 1, (binding.rowLayout.childCount - row.files.size))
        }

        val screenWidth =
            DisplayUtils.convertDpToPixel(context.resources.configuration.screenWidthDp.toFloat(), context)
                .toFloat()

        val shrinkRatio: Float
        if (row.files.size > 1) {
            var newSummedWidth = 0f
            for (file in row.files) {
                // first adjust all thumbnails to max height
                val thumbnail1 = file.imageDimension ?: ImageDimension(defaultThumbnailSize, defaultThumbnailSize)

                val height1 = thumbnail1.height
                val width1 = thumbnail1.width

                val scaleFactor1 = row.getMaxHeight() / height1
                val newHeight1 = height1 * scaleFactor1
                val newWidth1 = width1 * scaleFactor1

                file.setImageDimension(ImageDimension(newWidth1, newHeight1))

                newSummedWidth += newWidth1
            }

            var c = 1f
            if (galleryAdapter.columns == 5) {
                when (row.files.size) {
                    2 -> {
                        c = 5 / 2f
                    }
                    3 -> {
                        c = 4 / 3f
                    }
                    4 -> {
                        c = 4 / 5f
                    }
                    5 -> {
                        c = 1f
                    }
                }
            }

            shrinkRatio = (screenWidth / c) / newSummedWidth
        } else {
            val thumbnail1 = row.files[0].imageDimension ?: ImageDimension(defaultThumbnailSize, defaultThumbnailSize)
            shrinkRatio = (screenWidth / galleryAdapter.columns) / thumbnail1.width
        }

        for (indexedFile in row.files.withIndex()) {
            val file = indexedFile.value
            val index = indexedFile.index

            val adjustedHeight1 = ((file.imageDimension?.height ?: defaultThumbnailSize) * shrinkRatio).toInt()
            val adjustedWidth1 = ((file.imageDimension?.width ?: defaultThumbnailSize) * shrinkRatio).toInt()

            // re-use existing one
            val linearLayout = binding.rowLayout[index] as LinearLayout
            val shimmer = linearLayout[0] as LoaderImageView

            val thumbnail = linearLayout[1] as ImageView
            thumbnail.adjustViewBounds = true
            thumbnail.scaleType = ImageView.ScaleType.CENTER


            ocFileListDelegate.bindGalleryRowThumbnail(
                shimmer,
                thumbnail,
                file,
                this,
                adjustedWidth1
            )

            val params = LinearLayout.LayoutParams(adjustedWidth1, adjustedHeight1)

            if (index < (row.files.size - 1)) {
                params.setMargins(0, 0, 5, 5)
            } else {
                params.setMargins(0, 0, 0, 5)
            }

            thumbnail.layoutParams = params
            thumbnail.layoutParams.height = adjustedHeight1
            thumbnail.layoutParams.width = adjustedWidth1

            shimmer.layoutParams = params
            shimmer.layoutParams.height = adjustedHeight1
            shimmer.layoutParams.width = adjustedWidth1
        }
    }

    fun redraw() {
        bind(currentRow)
    }
}
