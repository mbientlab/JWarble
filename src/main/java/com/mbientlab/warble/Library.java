/*
 * Copyright 2014-2019 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */
package com.mbientlab.warble;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

import java.util.Locale;

/**
 * General library level functions
 */
public class Library {
    static final Native WARBLE = LibraryLoader.create(Native.class).load("warble");

    /**
     * API log levels
     */
    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    private Library() {

    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");
    }

    /**
     * Checks the version of the native Warble C library used by the wrwapper
     * @return Warble C library version in x.y.z format
     */
    public static String version() {
        return WARBLE.warble_lib_version();
    }

    /**
     * Checks the build configuration of the native Warble C library used by the wrapper
     * @return Either 'Release' or 'Debug'
     */
    public static String config() {
        return WARBLE.warble_lib_config();
    }

    /**
     * Initializes the Warble library
     * @param bleppLogLevel libblepp log level, only available on Linux
     */
    public static void init(LogLevel bleppLogLevel) {
        Native.Option[] opts = Struct.arrayOf(Runtime.getRuntime(Library.WARBLE), Native.Option.class, 1);
        int i = 0;

        if (!isWindows() && bleppLogLevel != null) {
            opts[i++].set("log-level", bleppLogLevel.name().toLowerCase(Locale.US));
        }
        WARBLE.warble_lib_init(i, opts);
    }
}
