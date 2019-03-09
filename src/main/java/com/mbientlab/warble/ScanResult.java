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

import jnr.ffi.Pointer;

public class ScanResult {
    private final Native.ScanResult nativeResult;

    public ScanResult(Pointer address) {
        nativeResult = new Native.ScanResult(address.getRuntime());
        nativeResult.useMemory(address);
    }

    public String getMac() {
        return nativeResult.mac.get().getString(0L);
    }

    public String getName() {
        return nativeResult.name.get().getString(0L);
    }

    public int getRssi() {
        return nativeResult.rssi.get();
    }

    public boolean hasServiceUuid(String uuid) {
        return Library.WARBLE.warble_scan_result_has_service_uuid(nativeResult, uuid) != 0;
    }

    public byte[] getManufacturerData(int companyId) {
        Native.ScanManufacturerData data = Library.WARBLE.warble_scan_result_get_manufacturer_data(nativeResult, (short) (companyId & 0xffff));
        if (data != null) {
            byte[] jvm_array = new byte[data.value_size.get()];
            for(byte i = 0; i < jvm_array.length; i++) {
                jvm_array[i] = data.value.get().getByte(i);
            }

            return jvm_array;
        }

        return null;
    }
}