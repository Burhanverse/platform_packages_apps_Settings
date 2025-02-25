/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2020 The "Best Improved Cherry Picked Rom" Project
 * Copyright (C) 2020 Project Fluid
 * Copyright (C) 2021 ShapeShiftOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.utils;

import android.os.SystemProperties;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.view.Display;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.graphics.Point;
import androidx.annotation.VisibleForTesting;

import com.android.internal.os.PowerProfile;
import com.android.internal.util.MemInfoReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.settings.R;

public class BlissSpecUtils {
    private static final String DEVICE_NAME_MODEL_PROPERTY = "ro.product.system.model";
    private static final String BLISS_CPU_MODEL_PROPERTY = "ro.soc.model";
    private static final String FALLBACK_CPU_MODEL_PROPERTY = "ro.board.platform";
    private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    static String aproxStorage;

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        double total = (totalBlocks * blockSize)/ 1073741824;
        int lastval = (int) Math.round(total);
            if ( lastval > 0  && lastval <= 16){
                aproxStorage = "16";
            } else if (lastval > 16 && lastval <=32) {
                aproxStorage = "32";
            } else if (lastval > 32 && lastval <=64) {
                aproxStorage = "64";
            } else if (lastval > 64 && lastval <=128) {
                aproxStorage = "128";
            } else if (lastval > 128 && lastval <= 256) {
                aproxStorage = "256";
            } else if (lastval > 256 && lastval <= 512) {
                aproxStorage = "512";
            } else if (lastval > 512) {
                aproxStorage = "512+";
            } else aproxStorage = "null";
        return aproxStorage;
    }

    public static int getTotalRAM() {
        MemInfoReader memReader = new MemInfoReader();
        memReader.readMemInfo();
        String aproxStorage;
        double totalmem = memReader.getTotalSize();
        double gb = (totalmem / 1073741824) + 0.3f; // Cause 4gig devices show memory as 3.48 .-.
        int gigs = (int) Math.round(gb);
        return gigs;
    }

    public static String getDeviceName() {
        String deviceModel = SystemProperties.get(DEVICE_NAME_MODEL_PROPERTY);
        if (!deviceModel.isEmpty())
            return deviceModel;
        else
            return "unknown";
    }

    public static String getProcessorModel() {
        String cpuModelBliss = SystemProperties.get(BLISS_CPU_MODEL_PROPERTY);
        String cpuModelFallback = SystemProperties.get(FALLBACK_CPU_MODEL_PROPERTY);
        if (!cpuModelBliss.isEmpty()) {
            return cpuModelBliss;
        } else if (!cpuModelFallback.isEmpty()) {
            return cpuModelFallback;
        } else {
            return "unknown";
        }
    }

    public static String getScreenRes(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        int width = size.x;
        int height = size.y;
        int rot = windowManager.getDefaultDisplay().getRotation();
        int dpi = metrics.densityDpi;
        int rotation = 0;

        // Show the screen rotation degree
        if (rot == 1){
            rotation = 90;
        } else if (rot == 2){
            rotation = 180;
        } else if (rot == 3){
            rotation = 270;
        } else rotation = 0;

        // Return the screen resolution
        // Swap Width and Height in case w < h
        if (width < height){
        return String.format("%dx%d, Rotation: %d, DPI: %d", height, width, rotation, dpi);
        } else {
        return String.format("%dx%d, Rotation: %d, DPI: %d", width, height, rotation, dpi);}
    }

    public static int getBatteryCapacity(Context context) {
        Object powerProfile = null;

        double batteryCapacity = 0;
        try {
            powerProfile = Class.forName(POWER_PROFILE_CLASS)
                            .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            batteryCapacity = (Double) Class
                            .forName(POWER_PROFILE_CLASS)
                            .getMethod("getAveragePower", java.lang.String.class)
                            .invoke(powerProfile, "battery.capacity");

        } catch (Exception e) {
            e.printStackTrace();
        }

        String str = Double.toString(batteryCapacity);
        String strArray[] = str.split("\\.");
        int batteryCapacityInt = Integer.parseInt(strArray[0]);

        return batteryCapacityInt;
    }
}
