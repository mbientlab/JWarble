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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;

public class Gatt {
    static final Native LIB_WARBLE = LibraryLoader.create(Native.class).load("warble");

    public final String mac;
    public Consumer<Integer> onDisconnect = (status) -> {

    };

    private final Pointer warbleGatt;
    private final Map<String, GattCharacteristic> characteristics = new HashMap<>();

    public Gatt(String mac) {
        this.mac = mac;

        this.warbleGatt = LIB_WARBLE.warble_gatt_create(this.mac);
        LIB_WARBLE.warble_gatt_on_disconnect(warbleGatt, null, (ctx, caller, status) -> onDisconnect.accept(status));
    }

    public CompletableFuture<Void> connectAsync() {
        final CompletableFuture<Void> asyncTask = new CompletableFuture<>();

        LIB_WARBLE.warble_gatt_connect_async(warbleGatt, null, (context, gatt, err) -> {
            if (err == null) {
                asyncTask.complete(null);
            } else {
                asyncTask.completeExceptionally(new GattException(err));
            }
        });

        return asyncTask;
    }

    public void disconnect() {
        LIB_WARBLE.warble_gatt_disconnect(warbleGatt);
    }

    public boolean serviceExists(String uuid) {
        return LIB_WARBLE.warble_gatt_has_service(warbleGatt, uuid) != 0;
    }

    public GattCharacteristic findCharacteristic(String uuid) {
        if (!characteristics.containsKey(uuid)) {
            Pointer warbleGattChar = LIB_WARBLE.warble_gatt_find_characteristic(warbleGatt, uuid);
            
            if (warbleGattChar != null) {
                GattCharacteristic gattChar = new GattCharacteristic(warbleGattChar);
                characteristics.put(uuid, gattChar);
                return gattChar;
            }
            return null;
        }
        return characteristics.get(uuid);
    }
}