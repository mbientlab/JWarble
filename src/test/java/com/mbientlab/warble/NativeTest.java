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

import jnr.ffi.annotations.*;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

import org.testng.annotations.Test;
import java.nio.charset.Charset;
//import static org.testng.Assert.*;

public class NativeTest {
    @Test public void someLibraryMethodReturnsTrue() throws InterruptedException {
        final Native libwarble = LibraryLoader.create(Native.class).load("warble");

        Native.FnVoid_VoidP_WarbleScanResultP handler = new Native.FnVoid_VoidP_WarbleScanResultP() {
            @Override
            @Delegate public void apply(Pointer context, Pointer pointer) {
                System.out.println("Hey, ho! hey ho!");

                Native.ScanResult result = new Native.ScanResult(pointer.getRuntime());
                result.useMemory(pointer);
                System.out.printf("mac: %s, name: %s%n", result.mac.get().getString(0L), result.name.get().getString(0L));

                System.out.printf("java callback parameter= %x%n", pointer.address());
            }
        };
        libwarble.warble_scanner_set_handler(null, handler);
        libwarble.warble_scanner_start(0, null);

        Thread.sleep(10000);

        libwarble.warble_scanner_stop();
    }
}