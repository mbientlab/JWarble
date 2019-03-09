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

import jnr.ffi.Pointer;

public class GattCharacteristic {
    private final Pointer warbleGattChar;

    public final String uuid;
    public Consumer<byte[]> onNotificationReceived = (value) -> { };

    GattCharacteristic(Pointer warbleGattChar) {
        this.warbleGattChar = warbleGattChar;
        this.uuid = Library.WARBLE.warble_gattchar_get_uuid(warbleGattChar);
        
        Library.WARBLE.warble_gattchar_on_notification_received(warbleGattChar, null, (context, caller, value, length) -> {
            byte[] jvm_array = new byte[length];
            for(byte i = 0; i < length; i++) {
                jvm_array[i] = value.getByte(i);
            }

            onNotificationReceived.accept(jvm_array);
        });
    }

    private CompletableFuture<Void> writeAsync(Consumer<Native.FnVoid_VoidP_WarbleGattCharP_CharP> fn) {
        final CompletableFuture<Void> asyncTask = new CompletableFuture<>();

        fn.accept((ctx, caller, err) -> {
            if (err != null) {
                asyncTask.completeExceptionally(new GattCharacteristicException(err));
            } else {
                asyncTask.complete(null);
            }
        });

        return asyncTask;
    }

    public CompletableFuture<Void> writeAsync(byte[] value) {
        return writeAsync(handler -> Library.WARBLE.warble_gattchar_write_async(warbleGattChar, value, (byte) value.length, null, handler));
    }

    public CompletableFuture<Void> writeWithoutResponseAsync(byte[] value) {
        return writeAsync(handler -> Library.WARBLE.warble_gattchar_write_without_resp_async(warbleGattChar, value, (byte) value.length, null, handler));
    }

    public CompletableFuture<byte[]> readAsync() {
        final CompletableFuture<byte[]> asyncTask = new CompletableFuture<>();

        Library.WARBLE.warble_gattchar_read_async(warbleGattChar, null, (ctx, caller, value, length, err) -> {
            if (err != null) {
                asyncTask.completeExceptionally(new GattCharacteristicException(err));
            } else {
                byte[] jvm_array = new byte[length];
                for(byte i = 0; i < length; i++) {
                    jvm_array[i] = value.getByte(i);
                }

                asyncTask.complete(jvm_array);
            }
        });

        return asyncTask;
    }

    private CompletableFuture<Void> editNotification(Consumer<Native.FnVoid_VoidP_WarbleGattCharP_CharP> fn) {
        final CompletableFuture<Void> asyncTask = new CompletableFuture<>();

        fn.accept((ctx, caller, err) -> {
            if (err != null) {
                asyncTask.completeExceptionally(new GattCharacteristicException(err));
            } else {
                asyncTask.complete(null);
            }
        });

        return asyncTask;
    }

    public CompletableFuture<Void> enableNotificationsAsync() {
        return editNotification(handler -> Library.WARBLE.warble_gattchar_enable_notifications_async(warbleGattChar, null, handler));
    }

    public CompletableFuture<Void> disableNotificationsAsync() {
        return editNotification(handler -> Library.WARBLE.warble_gattchar_disable_notifications_async(warbleGattChar, null, handler));
    }
}