/*
 * Copyright (C) 2021.  Aravind Chowdary (@kamaravichow)
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

package com.aravi.dot.constant;

import com.aravi.dot.BuildConfig;

public class Constants {
    public static final String SHARED_PREFERENCE_NAME = "com.aravi.dot.PREFERENCES_CUSTOMISATIONS";
    public static final String DEFAULT_NOTIFICATION_CHANNEL = "SAFE_DOT_NOTIFICATION";
    public static final String SERVICE_NOTIFICATION_CHANNEL = "SAFE_DOT_SERVICE_NOTIFICATION";
    public static final int NOTIFICATION_ID = 256;

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

}
