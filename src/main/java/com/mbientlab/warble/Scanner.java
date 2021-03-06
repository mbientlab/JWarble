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

import java.util.Locale;
import java.util.function.Consumer;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/**
 * Scans for nearby BLE devices
 */
public class Scanner {
    /**
     * BLE scan types
     */
    public enum ScanType {
        /** Only detect advertising data */
        PASSIVE,
        /** Requests scan response from the device in addition to the ad data */
        ACTIVE
    }

    /**
     * Sets a handler to process discovered devices
     * @param handler Consumer to forwarded scan results to
     */
    public static void onResultReceived(Consumer<ScanResult> handler) {
        Library.WARBLE.warble_scanner_set_handler(null, (context, pointer) -> handler.accept(new ScanResult(pointer)));
    }

    /**
     * Start the BLE scan with default options
     */
    public static void start() {
        Library.WARBLE.warble_scanner_start(0, null);
    }

    /**
     * Start the BLE scan
     * @param type   Type of BLE scan to perform, defaults to {@link ScanType#ACTIVE} if null
     * @param hciMac MAC address of the HCI device to use as a hex string, null to have the system pick one
     */
    public static void start(ScanType type, String hciMac) {
        Native.Option[] opts = Struct.arrayOf(Runtime.getRuntime(Library.WARBLE), Native.Option.class, 2);
        int i = 0;

        if (!Library.isWindows() && hciMac != null) {
            opts[i++].set("hci", hciMac);
        }
        if (type != null) {
            opts[i++].set("scan-type", type.name().toLowerCase(Locale.US));
        }

        Library.WARBLE.warble_scanner_start(i, opts);
    }

    /**
     * Stop the BLE scan
     */
    public static void stop() {
        Library.WARBLE.warble_scanner_stop();
    }
}