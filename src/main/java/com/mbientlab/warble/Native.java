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
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.types.u_int8_t;
import jnr.ffi.types.u_int16_t;

public interface Native {
    final class ScanManufacturerData extends Struct {
        public final Pointer value = new Pointer();
        public final Unsigned8 value_size = new Unsigned8();

        public ScanManufacturerData(final Runtime runtime) {
            super(runtime);
        }
    }

    final class ScanResult extends Struct {
        public final Pointer mac = new Pointer(),
            name = new Pointer();
        public final Signed32 rssi = new Signed32();
        public final Pointer private_data = new Pointer();

        public ScanResult(final Runtime runtime) {
            super(runtime);
        }
    }

    final class Option extends Struct {
        public final AsciiStringRef key = new AsciiStringRef(16),
            value = new AsciiStringRef(24);
            
        public Option(final Runtime runtime) {
            super(runtime);
        }

        public void set(java.lang.String key, java.lang.String value) {
            this.key.set(key);
            this.value.set(value);
        }
    }

    interface FnVoid_IntPtr_WarbleGattP_CharP {
        @Delegate void apply(Pointer context, Pointer gatt, String err);
    }

    interface FnVoid_IntPtr_WarbleGattP_Int {
        @Delegate void apply(Pointer context, Pointer gatt, int value);
    }

    interface FnVoid_VoidP_WarbleScanResultP {
        @Delegate void apply(Pointer context, Pointer pointer);
    }

    interface FnVoid_VoidP_WarbleGattCharP_CharP {
        @Delegate void apply(Pointer context, Pointer gattchar, String err);
    }

    interface FnVoid_VoidP_WarbleGattCharP_UbyteP_Ubyte_CharP {
        @Delegate void apply(Pointer context, Pointer gattchar, Pointer value, @u_int8_t byte length, String err);
    }

    interface FnVoid_VoidP_WarbleGattCharP_UbyteP_Ubyte {
        @Delegate void apply(Pointer context, Pointer gattchar, Pointer value, @u_int8_t byte length);
    }

    String warble_lib_version();
    String warble_lib_config();
    void warble_lib_init(int length, Option[] options);

    void warble_scanner_stop();
    void warble_scanner_start(int length, Option[] options);
    void warble_scanner_set_handler(Pointer context, FnVoid_VoidP_WarbleScanResultP handler);

    ScanManufacturerData warble_scan_result_get_manufacturer_data(ScanResult result, @u_int16_t short companyId);
    int warble_scan_result_has_service_uuid(ScanResult result, String uuid);

    void warble_gatt_connect_async(Pointer gatt, Pointer context, FnVoid_IntPtr_WarbleGattP_CharP handler);
    void warble_gatt_disconnect(Pointer gatt);
    void warble_gatt_delete(Pointer gatt);
    void warble_gatt_on_disconnect(Pointer gatt, Pointer context, FnVoid_IntPtr_WarbleGattP_Int handler);
    int warble_gatt_is_connected(Pointer gatt);
    Pointer warble_gatt_create(String mac);
    Pointer warble_gatt_create_with_options(int length, Option[] options);

    Pointer warble_gatt_find_characteristic(Pointer gatt, String uuid);
    int warble_gatt_has_service(Pointer gatt, String uuid);

    void warble_gattchar_disable_notifications_async(Pointer gattchar, Pointer context, FnVoid_VoidP_WarbleGattCharP_CharP handler);
    void warble_gattchar_write_without_resp_async(Pointer gattchar, byte[] value, @u_int8_t byte value_size, Pointer context, FnVoid_VoidP_WarbleGattCharP_CharP handler);
    void warble_gattchar_read_async(Pointer gattchar, Pointer context, FnVoid_VoidP_WarbleGattCharP_UbyteP_Ubyte_CharP handler);
    void warble_gattchar_write_async(Pointer gattchar, byte[] value, @u_int8_t byte value_size, Pointer context, FnVoid_VoidP_WarbleGattCharP_CharP handler);
    void warble_gattchar_enable_notifications_async(Pointer gattchar, Pointer context, FnVoid_VoidP_WarbleGattCharP_CharP handler);
    void warble_gattchar_on_notification_received(Pointer gattchar, Pointer context, FnVoid_VoidP_WarbleGattCharP_UbyteP_Ubyte handler);
    String warble_gattchar_get_uuid(Pointer gattchar);
    Pointer warble_gattchar_get_gatt(Pointer gattchar);
}