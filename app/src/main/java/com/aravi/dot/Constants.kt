/*
 * Copyright (C) 2022.  Aravind Chowdary (@kamaravichow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aravi.dot

object Constants {
    const val SHARED_PREFERENCE_NAME = "${BuildConfig.APPLICATION_ID}.PREFERENCES_CUSTOMISATIONS"
    const val DEFAULT_NOTIFICATION_CHANNEL = "SAFE_DOT_PRO_NOTIFICATION"
    const val SERVICE_NOTIFICATION_CHANNEL = "SAFE_DOT_PRO_SERVICE_NOTIFICATION"
    const val NOTIFICATION_ID = 256


    const val EXTRA_APP = "app.extra"
    const val EXTRA_PERMISSION = "permission.extra"
    const val PERMISSION_CAMERA = "permission.camera"
    const val PERMISSION_LOCATION = "permission.location"
    const val PERMISSION_MICROPHONE = "permission.microphone"

    const val POSITION_LOCATION = 0
    const val POSITION_CAMERA = 1
    const val POSITION_MICROPHONE = 2

    const val STATE_ON = 1
    const val STATE_OFF = 0
    const val STATE_INVALID = -1

    const val DOTS_TYPE_ICON = "dots.type.icon"
    const val DOTS_TYPE_DOT = "dots.type.dot"
    const val DOTS_MARGIN = 0.01f

    const val DOTS_COLOR_BASIC = "dots.color.basic"
    const val DOTS_COLOR_CUSTOM = "dots.color.custom"
    const val DOTS_COLOR_DEFAULT = "#61B773"

    val BASIC_COLORS = arrayOf(
        "#89B4F7", "#C3A6A2", "#D7DEE6", "#84C188", "#27BDD7",
        "#98ACCC", "#E68AEC", "#B5A9FC", "#BD78FF", "#61B773",
        "#C8AC94", "#F19D7D", "#17FFCB", "#FFB6D9", "#3DDCFE", "#FFEE58"
    )

    val isDebug: Boolean get() = BuildConfig.DEBUG
}
