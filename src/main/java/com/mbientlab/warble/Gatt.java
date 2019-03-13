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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

/**
 * Wrapper around the WarbleGatt C struct
 */
public class Gatt {
    /**
     * GAP address types.
     * See this <a href='https://devzone.nordicsemi.com/f/nordic-q-a/2084/gap-address-types'>post </a>
     * from the Nordic Dev Zone for more details on what each type means
     */
    public enum AddressType {
        /** Public address */
        PUBLIC,
        /** Random address */
        RANDOM,
        /** Unspecified, only supported on Windows 10 */
        UNSPECIFIED
    }
    /**
     * Builder class to construct a {@link Gatt} object.
     * Only the remote device's MAC address is required,
     */
    public static class Builder {
        private final String mac;
        private String hci = null;
        private AddressType addrType = null;

        /**
         * Instantiates a builder
         * @param mac MAC address of the remote device as a hex string
         */
        public Builder(String mac) {
            this.mac = mac;
        }

        /**
         * Sets the HCI mac address
         * @param hci Hex string representation of the MAC address
         * @return Calling object
         */
        public Builder withHci(String hci) {
            if (!Library.isWindows()) {
                this.hci = hci;
            }
            return this;
        }

        /**
         * Sets the Bluetooth address type
         * @param addrType Device's address type
         * @return Calling object
         */
        public Builder withAddressType(AddressType addrType) {
            this.addrType = addrType;
            return this;
        }

        /**
         * Build the Gatt object with the specified paramters
         * @return Gatt object
         */
        public Gatt build() {
            final Native.Option[] opts = Struct.arrayOf(Runtime.getRuntime(Library.WARBLE), Native.Option.class, 3);
            int i = 0;

            opts[i++].set("mac", mac);
            if (hci != null) {
                opts[i++].set("hci", hci);
            }
            if (addrType != null) {
                opts[i++].set("address-type", addrType.name().toLowerCase(Locale.US));
            }
            return new Gatt(Library.WARBLE.warble_gatt_create_with_options(i, opts), mac);
        }
    }

    /** MAC address of the remote Gatt device */
    public final String mac;
    /** Handler that listens for disconnect events */
    public Consumer<Integer> onDisconnect = (status) -> { };

    private final Pointer warbleGatt;
    private final Map<String, GattCharacteristic> characteristics = new HashMap<>();

    private Gatt(Pointer warbleGatt, String mac) {
        this.warbleGatt = warbleGatt;
        this.mac = mac;

        Library.WARBLE.warble_gatt_on_disconnect(warbleGatt, null, (ctx, caller, status) -> onDisconnect.accept(status));
    }

    @Override
    protected void finalize() {
        characteristics.clear();
        Library.WARBLE.warble_gatt_delete(warbleGatt);
    }

    /**
     * Establishes a connection to the remote device
     * @return Null when task completes, {@link GattException} if task fails
     */
    public CompletableFuture<Void> connectAsync() {
        final CompletableFuture<Void> asyncTask = new CompletableFuture<>();

        Library.WARBLE.warble_gatt_connect_async(warbleGatt, null, (context, gatt, err) -> {
            if (err == null) {
                asyncTask.complete(null);
            } else {
                asyncTask.completeExceptionally(new GattException(err));
            }
        });

        return asyncTask;
    }

    /**
     * Checks if currently connected to the remote device
     * @return True if connected, false otherwise
     */
    public boolean isConnected() {
        return Library.WARBLE.warble_gatt_is_connected(warbleGatt) != 0;
    }

    /**
     * Disconnects from the remote device
     */
    public void disconnect() {
        Library.WARBLE.warble_gatt_disconnect(warbleGatt);
    }

    /**
     * Checks if the GATT services exists on the remote device
     * @param uuid 128-bit UUID string to lookup
     * @return True if service exists, false otherwise
     */
    public boolean serviceExists(String uuid) {
        return Library.WARBLE.warble_gatt_has_service(warbleGatt, uuid) != 0;
    }

    /**
     * Find the GATT characteristic corresponding to the uuid string
     * @param uuid 128-bit UUID string to lookup
     * @return Object representing the GATT characteristic, null if it does not exist
     */
    public GattCharacteristic findCharacteristic(String uuid) {
        if (!characteristics.containsKey(uuid)) {
            Pointer warbleGattChar = Library.WARBLE.warble_gatt_find_characteristic(warbleGatt, uuid);
            
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